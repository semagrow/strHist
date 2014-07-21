package gr.demokritos.iit.irss.semagrow.rdf;

import gr.demokritos.iit.irss.semagrow.api.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.api.PrefixRange;
import gr.demokritos.iit.irss.semagrow.api.QueryResult;
import gr.demokritos.iit.irss.semagrow.parsing.Binding;
import gr.demokritos.iit.irss.semagrow.parsing.BindingSet;
import gr.demokritos.iit.irss.semagrow.parsing.Utilities;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;

public class RDFQueryResult implements QueryResult<RDFRectangle> {

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
				
				for (Binding b : bs.getBindings()) {
					// Find if the Binding is Subject or Predicate or Object.
					int type = getBindingType(b);
					
					String value = clean(b.getValue());// dn exei idiaiterh xrhsimothta
					
					System.out.println(">>>" + type);
					System.out.println(">>>" + value);
					switch (type) {
					case 1:		// Subjects
						if (((PrefixRange) rect.getRange(type)).contains(value)) 
							frequency++;
						prefixSet.add(value);
						break;
					case 2:		// Predicates
						if (((ExplicitSetRange) rect.getRange(type)).contains(value))
							frequency++;
						predicateSet.add(value);
						break;
					case 3:		// Objects
						// TODO: Change! 		
						
						Value val = null;
						
						if (value.contains("^^") && value.contains("http://")) {// XSD URI
							
							// Get the value of the URI.
							String valueURI = Utilities.getValueFromURI(value);
							// Get the type of the URI.
							String typeURI = Utilities.getTypeFromURI(value);
												
							if (typeURI.equals("int")) {
								val = ValueFactoryImpl.getInstance()
										.createLiteral(valueURI, XMLSchema.INTEGER);	
								
								objectIntegerSet.add(Integer.parseInt(valueURI));
								
							} else if (typeURI.equals("long")) {
								val = ValueFactoryImpl.getInstance()
										.createLiteral(valueURI, XMLSchema.LONG);
								
								objectLongSet.add(Long.parseLong(valueURI));
								
							} else if (typeURI.equals("dateTime")) {
								
								DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
								Date date = null;
								
								try {date = format.parse(valueURI);}
								catch (ParseException e) {e.printStackTrace();}
								
								if (date != null) {
									 if (((RDFLiteralRange) rect.getRange(type))
											.contains(new RDFLiteralRange(date, date))) {
										 frequency++;
									 }
									 
									 objectDateSet.add(date);
									
									break;								
								} else 
									System.err.println("Date Format Error at: " + getClass().getName());
								
							}// if	
							
						} else if (!value.contains("^^") && value.contains("http://")) {// URL							
							val = ValueFactoryImpl.getInstance()
									.createURI(value);
							
							objectStringSet.add(value);
							
						} else {// Plain Literal
							val = ValueFactoryImpl.getInstance()
									.createLiteral(value, XMLSchema.STRING);
							
							objectStringSet.add(value);							
						}					
													
						if (((RDFLiteralRange) rect.getRange(type)).contains(val)) 
							frequency++;
						
					default:
						System.err.println("Not a valid Binding.");
						break;
					}

				}// for
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
				return i + 1;
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
					b = b && ((PrefixRange) rect.getRange(i + 1)).contains(value);					
					break;
				case 1:
					b = b && ((ExplicitSetRange) rect.getRange(i + 1)).contains(value);					
					break;
				case 2:
					// TODO: change!
					
					Value val = null;
					
					if (value.contains("^^") && value.contains("http://")) {// XSD URI
						
						// Get the value of the URI.
						String valueURI = Utilities.getValueFromURI(value);
						// Get the type of the URI.
						String typeURI = Utilities.getTypeFromURI(value);
											
						if (typeURI.equals("int")) {
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
								b = b && ((RDFLiteralRange) rect.getRange(i + 1))
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
												
					b = b && ((RDFLiteralRange) rect.getRange(i + 1)).contains(val);
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

}
