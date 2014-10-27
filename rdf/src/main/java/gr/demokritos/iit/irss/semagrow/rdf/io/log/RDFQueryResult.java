package gr.demokritos.iit.irss.semagrow.rdf.io.log;

import gr.demokritos.iit.irss.semagrow.api.qfr.QueryResult;
import gr.demokritos.iit.irss.semagrow.base.Stat;
import gr.demokritos.iit.irss.semagrow.base.range.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.base.range.PrefixRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFLiteralRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.RDFURIRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFValueRange;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RDFQueryResult implements QueryResult<RDFRectangle,Stat>, Serializable {

	private ArrayList<BindingSet> bindingSets;
	// Query's statements stored for Cardinality estimation
	private List<Binding> queryStatements;


	public RDFQueryResult(List<Binding> queryStatements) {
		setBindingSets(new ArrayList<BindingSet>());
		this.queryStatements = queryStatements;
	}


	/**
	 * Given a Rectangle it returns a Stat object containing total frequency and
	 * distinct count for each dimension.
	 */
	@SuppressWarnings({ "rawtypes" })
	public Stat getCardinality(RDFRectangle rect) {

		long frequency = 0;
		List<Long> distinctCount = new ArrayList<Long>();
		// Help structures to store distinct items for each dimension
		Set<String> prefixSet = new HashSet<String>(), predicateSet = new HashSet<String>();
		Set<Integer> objectIntegerSet = new HashSet<Integer>();
		Set<Long> objectLongSet = new HashSet<Long>();
		Set<Date> objectDateSet = new HashSet<Date>();
		Set<String> objectStringSet = new HashSet<String>();	
		

		// First check if all const variables of the query exist in rectangle.
		// If not, then cardinality is surely 0.
		if (areConstContained(rect)) {
			System.err.println("BindingSet Size: " + bindingSets.size());
			
			for (BindingSet bs : bindingSets) {				

                boolean contained = true;

                for (Binding b : bs.getBindings()) {
					// Find if the Binding is Subject or Predicate or Object.
					int type = getBindingType(b);
					
					String value = clean(b.getValue());// dn exei idiaiterh xrhsimothta
					
//					System.out.println(">>>" + type);
//					System.out.println(">>>" + value);
					switch (type) {
					case 0:		// Subjects
						if (! ((PrefixRange) rect.getRange(type)).includes(value))
							contained = false;
						else
						    prefixSet.add(value);
						break;
					case 1:		// Predicates
						if (!((ExplicitSetRange<String>) rect.getRange(type)).includes(value))
							contained = false;
						else
						    predicateSet.add(value);
						break;
					case 2:		// Objects
						// TODO: Change! 		
						
						Literal val = null;
						
						if (value.contains("^^") && value.contains("http://")) {// XSD URI
							
							// Get the value of the URI.
							String valueURI = Utilities.getValueFromURI(value);
							// Get the type of the URI.
							String typeURI = Utilities.getTypeFromURI(value);
												
							if (typeURI.equals("int")) {
								val = ValueFactoryImpl.getInstance()
										.createLiteral(valueURI, XMLSchema.INTEGER);

                                if (!((RDFLiteralRange) rect.getRange(type)).includes(val))
                                    contained = false;
                                else
								    objectIntegerSet.add(Integer.parseInt(valueURI));
								
							} else if (typeURI.equals("long")) {
								val = ValueFactoryImpl.getInstance()
										.createLiteral(valueURI, XMLSchema.LONG);
                                if (!((RDFLiteralRange) rect.getRange(type)).includes(val))
                                    contained = false;
								else
                                    objectLongSet.add(Long.parseLong(valueURI));
								
							} else if (typeURI.equals("dateTime")) {
								
//								DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+hh:mm");
								Date date = null;
								
								try {date = format.parse(valueURI);}
								catch (ParseException e) {e.printStackTrace();}
								
								if (date != null) {
									 if (!((RDFLiteralRange) rect.getRange(type))
											.contains(new RDFLiteralRange(date, date))) {
										 contained = false;
									 }
								     else
									    objectDateSet.add(date);
									
									break;								
								} else 
									System.err.println("Date Format Error at: " + getClass().getName());
								
							}// if	
							
						} else if (!value.contains("^^") && value.contains("http://")) {// URL							
							URI valURI = ValueFactoryImpl.getInstance().createURI(value);

                            if (!((RDFLiteralRange) rect.getRange(type)).includes(val))
                                contained = false;
                            else
							    objectStringSet.add(value);
							
						} else {// Plain Literal
							val = ValueFactoryImpl.getInstance()
									.createLiteral(value, XMLSchema.STRING);

                            if (!((RDFLiteralRange) rect.getRange(type)).includes(val))
                                contained = false;
							else
                                objectStringSet.add(value);
						}
						
					default:
//						System.err.println("Not a valid Binding.");
						break;
					}

                    if (!contained)
                        continue;

				}// for

                if (contained)
                    frequency++;

			}// for
		}// if
		else
			System.err.println("Not all query's const variables exist in Rectangle");	
		
		// Subject distinct count				 
		distinctCount.add(prefixSet.isEmpty() ? 1 : (long)prefixSet.size()); 
		// Predicate distinct count
		distinctCount.add(predicateSet.isEmpty() ? 1 : (long)predicateSet.size());
		// Object distinct count		
		distinctCount.add((objectIntegerSet.isEmpty() &&
							objectLongSet.isEmpty() &&
							objectDateSet.isEmpty() &&
							objectStringSet.isEmpty()) 
						  ? 1 
						  : ((long)objectIntegerSet.size() + 
							(long)objectLongSet.size() +
							(long)objectDateSet.size() +
							(long)objectStringSet.size()));
		
		
		return new Stat(frequency, distinctCount);
	}// getCardinality


	/**
	 * Finds if the Binding is Subject or Predicate or Object.
	 * 
	 * @param b
	 *            Binding to be tested.
	 * @return 1 for Object, 2 for Predicate, 3 for Object
	 */
	private int getBindingType(Binding b) {
		String name = b.getName();
		
		for (int i = 0; i < queryStatements.size(); i++) {
			if (queryStatements.get(i).getName().equals(name))
				return i;
		}

		return 0;
	}// getBindingType


	/**
	 * Finds all the const variables of the Query and checks if they exist in
	 * Rectangle.
	 */	
	@SuppressWarnings("rawtypes")
	private boolean areConstContained(RDFRectangle rect) {

		boolean b = true;
		
		// Find the const variables of the query and check them.
		for (int i = 0; i < queryStatements.size(); i++) {
			
			if (!queryStatements.get(i).getValue().equals("")) { // Is a Const Variable
				
				String value = clean(queryStatements.get(i).getValue());				
				
				switch (i) {
				case 0:
					//b = b && ((PrefixRange) rect.getRange(i)).includes(value);
					break;
				case 1:
					b = b && ((ExplicitSetRange<String>) rect.getRange(i)).includes(value);
					break;
				case 2:
					// TODO: change!
					
					Value val = null;
					
					if (value.contains("^^") && value.contains("http://")) {// XSD URI
						
						// Get the value of the URI.
						String valueURI = Utilities.getValueFromURI(value);
						// Get the type of the URI.
						String typeURI = Utilities.getTypeFromURI(value);
											
						if (typeURI.equals("int") || typeURI.equals("integer")) {
							val = ValueFactoryImpl.getInstance()
									.createLiteral(valueURI, XMLSchema.INTEGER);								
						} else if (typeURI.equals("long")) {
							val = ValueFactoryImpl.getInstance()
									.createLiteral(valueURI, XMLSchema.LONG);
						} else if (typeURI.equals("dateTime")) {	
							// Special handling of DateTime because there is
							// a 3 hours error when converting Gregorian to Date
							// literal.calendarValue().toGregorianCalendar().getTime()
							//val = ValueFactoryImpl.getInstance()
							//		.createLiteral(valueURI, XMLSchema.DATETIME);
							
							DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
							Date date = null;
							
							try {date = format.parse(valueURI);}
							catch (ParseException e) {e.printStackTrace();}
							
							if (date != null) {
								b = b && ((RDFLiteralRange) rect.getRange(i))
										.contains(new RDFLiteralRange(date, date));
								
								break;								
							} else 
								System.err.println("Date Format Error at: " + getClass().getName());
							
						}// if	
						
					} else if (!value.contains("^^") && value.contains("http://")) {// URL
						
						val = ValueFactoryImpl.getInstance()
								.createURI(value);
						
					} else {// Plain Literal
						val = ValueFactoryImpl.getInstance()
								.createLiteral(value, XMLSchema.STRING);						
					}					
												
					b = b && ((RDFLiteralRange) rect.getRange(i)).includes((Literal)val);
					break;
				}
				
			}// if
			if (!b)
				return false;
		}// for

		return b;
	}// areConstContained


	/**
	 * Clean the trash from the string.
	 */
	private static String clean(String string) {
		String pattern = "@(.*?)\"";

		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);

		// Now create matcher object.
		Matcher m = r.matcher(string);

		if (m.find()) {
			System.err.println("MATCH");
			string = string.replace(m.group(0), "");
		}

//		string = string.replaceAll("\"", "");

		return string.trim();
	}


	public static void main(String[] args) {
//		System.out.println(clean("Egyptian Division @en"));
		System.out.println(Utilities.cleanURI("\"192\"^^<http://www.w3.org/2001/XMLSchema#int>"));

	}


	public long getCardinality() {
		return bindingSets.size();
	}


	public ArrayList<BindingSet> getBindingSets() {
		return bindingSets;
	}


	public void setBindingSets(ArrayList<BindingSet> bindingSets) {
		this.bindingSets = bindingSets;
	}


    public List<RDFRectangle> getRectangles() {
        return getRectangles(null);
    }
    /**
     *
     * @param queryRect the rectangle extracted from query statements
     *                  (and filters)
     * @return list of rectangles from query results (one rectangle
     * for each predicate)
     */
    public List<RDFRectangle> getRectangles(RDFRectangle queryRect) {

        //initialize structures
        List<RDFURIRange> subjectRanges = new ArrayList<RDFURIRange>();
        List<URI> predicateStrings =
                new ArrayList<URI>();
        List<ExplicitSetRange<URI>> predicateRanges =
                new ArrayList<ExplicitSetRange<URI>>();
        List<RDFValueRange> objectRanges =
                new ArrayList<RDFValueRange>();

        List<RDFRectangle> rectangles = new ArrayList<RDFRectangle>();

        // mappings to bindings
        List<Integer> mappings = new ArrayList<Integer>(3);
        //int[] mappings = new int[3];
        mappings.add(-1);
        mappings.add(-1);
        mappings.add(-1);

        //Get variables
        int cnt = 0;
        List<Integer> types = new ArrayList<Integer>();
        System.out.println("BindingSet size : " + bindingSets.size());
        if (!bindingSets.isEmpty()) {

            for (Binding b : bindingSets.get(0).getBindings()) {

                types.add(getBindingType(b));
                mappings.set(getBindingType(b),cnt);
                cnt += 1;
            }

        }

        //for (int t : types) {
          //  System.out.println(t);
        //}

        //Take constant values
        boolean isConstPredicate = false;
        boolean isConstSubject = false;
        boolean isConstObject = false;
        RDFURIRange constSubject = null;
        ExplicitSetRange<URI> constPredicate = null;
        RDFValueRange constObject = null;


        for (int i = 0; i < 3; i++) {





            switch (i) {
                case 0 :
                    if (!((RDFURIRange) queryRect.getRange(i)).isInfinite()) {
                        isConstSubject = true;
                        constSubject = (RDFURIRange) queryRect.getRange(i);
                    }
                    break;
                case 1 :
                    if (!((ExplicitSetRange<URI>) queryRect.getRange(i)).isInfinite()) {
                        isConstPredicate = true;
                        constPredicate = (ExplicitSetRange<URI>) queryRect.getRange(i);
                        //add it to predicateRanges
                        predicateRanges.add(constPredicate);
                    }
                    break;
                case 2 :
                    if (!((RDFValueRange) queryRect.getRange(i)).isInfinite()) {
                        isConstObject = true;
                        constObject = (RDFValueRange) queryRect.getRange(i);
                    }
                    break;
                default:
                    System.err.println("Not a valid Binding.");
                    break;
            }

        }



        Binding b;
        String value;
        ArrayList<String> prefixList;
        int curRectangleIdx = 0; //rectangle corresponding
        //to current predicate

        //for every binding set
        for (BindingSet bs : bindingSets) {

            cnt = 0;
            mappings.set(0,-1);
            mappings.set(1,-1);
            mappings.set(2,-1);
            for (Binding bi : bs.getBindings()) {

                    types.add(getBindingType(bi));
                    mappings.set(getBindingType(bi),cnt);
                    cnt += 1;
             }




            //get binding
             List<Binding> binding = bs.getBindings();

            if (!isConstPredicate) {

                //get Predicate from current binding
                if (mappings.get(1) != -1) {
                    b = binding.get(mappings.get(1));
                    value = clean(b.getValue());

                    //new predicate, add it to list
                    if (!predicateStrings.contains(ValueFactoryImpl.getInstance().createURI(value))) {
                        predicateStrings.add(ValueFactoryImpl.getInstance().createURI(value));
                        curRectangleIdx = predicateStrings.size() - 1;
                    } else {
                        curRectangleIdx = predicateStrings.indexOf(ValueFactoryImpl.getInstance().createURI(value));
                    }
                }
            }

            if (isConstSubject) {

                //add it to subjectRanges as soon as a new rectangle
                // must be formed
                if (curRectangleIdx != subjectRanges.size() - 1) {
                    subjectRanges.add(constSubject);
                }
            } else {
                //take subject from binding and compute
                // new prefix from this subject and the subjectRange
                // in position curRectangle
                if (mappings.get(0) != -1) {
                    b = binding.get(mappings.get(0));
                    value = clean(b.getValue());


                    if (curRectangleIdx != subjectRanges.size() - 1) {

                        System.out.println("Subject value: " + value);
                        //todo: value is empty
                        prefixList = new ArrayList<String>();
                        prefixList.add(value);
                        subjectRanges.add(new RDFURIRange(prefixList));
                    } else {

                        subjectRanges.get(curRectangleIdx).expand(ValueFactoryImpl.getInstance().createURI(value));
                    }
                }

            }

            if (isConstObject) {
                //add it to objectRanges as soon as a new rectangle
                // must be formed
                if (curRectangleIdx != objectRanges.size() - 1) {
                    objectRanges.add(constObject);
                }
            } else {
                //take object from binding and compute
                // new interval/calendarRange/prefix from this
                // object and the objectRange is position curObject
                if (mappings.get(2) != -1) {
                    b = binding.get(mappings.get(2));
                    value = clean(b.getValue());

                    // Find the type(Integer,Long,Date) of the URIS.
                    String type = Utilities.getTypeFromURI(value);
                    String v = Utilities.getValueFromURI(value);

                    if (curRectangleIdx != objectRanges.size() - 1) {

                        if (value.contains("^^") && value.contains("http://")) {
                            //todo: check if it is xsd uri
                            if (type.equals("int") || type.equals("integer")) {
                                objectRanges.add(new RDFValueRange(null,new RDFLiteralRange(Integer.parseInt(v), Integer.parseInt(v))));

                            } else if (type.equals("long")) {
                                objectRanges.add(new RDFValueRange(null,new RDFLiteralRange(Long.parseLong(v), Long.parseLong(v))));

                            } else if (type.equals("dateTime")) {

//                            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+hh:mm");
                                Date dateLow = null, dateHigh = null;

                                try {
                                    dateLow = format.parse(v);
                                    dateHigh = format.parse(v);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                if (dateLow != null && dateHigh != null)
                                    objectRanges.add(new RDFValueRange(null,new RDFLiteralRange(dateLow, dateHigh)));
                                else
                                    System.err.println("Date Format Error.");
                            }
                        } else if (!value.contains("^^") && value.contains("http://")) {// URL
                            //todo: do i need this check above as well?
                            objectRanges.add(new RDFValueRange(null,new RDFLiteralRange(value)));

                        } else {// Plain Literal
                            objectRanges.add(new RDFValueRange(null,new RDFLiteralRange(value)));
                        }

                    } else {
                        //objectRanges.get(curRectangleIdx).expand(v);
                        //?
                    }
                }
            }


        }


        //Create predicateRanges from predicates
        Set<URI> items = new HashSet<URI>();
        for (URI p : predicateStrings) {
            //make an explicitRange and add it
            // to predicateRanges list
            items = new HashSet<URI>();
            items.add(p);
            if (predicateRanges.isEmpty()) {
                predicateRanges.add(new ExplicitSetRange<URI>(items));
            }

        }

        //Create rectangles from ranges
        for (int i = 0; i < predicateRanges.size(); i++) {

            RDFURIRange subjectR = subjectRanges.get(i);
            ExplicitSetRange<URI> predicateR = predicateRanges.get(i);
            RDFValueRange objectR = objectRanges.get(i);
            rectangles.add(new RDFRectangle(subjectRanges.get(i),
                    predicateRanges.get(i), objectRanges.get(i)));
        }
        return rectangles;
    }

}
