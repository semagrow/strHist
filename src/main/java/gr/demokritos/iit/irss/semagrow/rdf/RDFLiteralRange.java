package gr.demokritos.iit.irss.semagrow.rdf;


import gr.demokritos.iit.irss.semagrow.api.*;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.XMLSchema;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by angel on 7/15/14.
 */
public class RDFLiteralRange
        implements RangeLength<Value>, Rangeable<RDFLiteralRange>
{

    private URI valueType;
    private RangeLength<?> range;
    private boolean infinite = false;


    public RDFLiteralRange() {

        infinite = true;
    }
    public RDFLiteralRange(URI valueType, RangeLength<?> range) {

		this.valueType = valueType;
		this.range = range;
	}


    public RDFLiteralRange(int low, int high)
    {
        this(XMLSchema.INTEGER, new IntervalRange<Integer>(low, high));
    }

    public RDFLiteralRange(long low, long high) {
        this(XMLSchema.INTEGER, new IntervalRange<Integer>((int) low, (int) high));
    }

    public RDFLiteralRange(String range) {

        // TODO: maybe change it to take the list as a parameter
        ArrayList<String> stringList = new ArrayList<String>();
        stringList.add(range);
        this.valueType = XMLSchema.STRING;
        this.range = new PrefixRange(stringList);
    }

    public RDFLiteralRange(Date begin, Date end) {

        this(XMLSchema.DATETIME, new CalendarRange(begin, end));
    }


    public boolean isUnit() {

        if (infinite) return false;

        return range.isUnit();
    }

    public String toString() {

        //uriprefixes for PrefixRange
        //uris for ExplicitSetRange
        //intervalRange for IntervalRange
        //calendarRange for CalendarRange
        return range.toString();
    }


	public RDFLiteralRange intersection(RDFLiteralRange literalRange) {

        if (infinite) return literalRange;

        RDFLiteralRange res = null;
        if (valueType.equals(literalRange.getValueType()))
        {

            if (valueType.equals(XMLSchema.INTEGER)) {
                res = new RDFLiteralRange(valueType,
                        ((IntervalRange<Integer>) range).intersection(
                                (IntervalRange<Integer>) literalRange.getRange()));
            } else if (valueType.equals(XMLSchema.LONG)) {

                res = new RDFLiteralRange(valueType,
                        ((IntervalRange<Integer>) range).intersection(
                                (IntervalRange<Integer>) literalRange.getRange()));
            } else if (valueType.equals(XMLSchema.STRING)) {

                res = new RDFLiteralRange(valueType,
                        ((PrefixRange) range).intersection(
                                (PrefixRange) literalRange.getRange()));
            } else if (valueType.equals(XMLSchema.DATETIME)) {

                res = new RDFLiteralRange(valueType,
                        ((CalendarRange) range).intersection(
                                (CalendarRange) literalRange.getRange()));
            }
        }



        return res;
    }


    //Tested (only interval range)
	public RDFLiteralRange minus(RDFLiteralRange literalRange) {

        if (valueType.equals(literalRange.getValueType()))
        {

            if (valueType.equals(XMLSchema.INTEGER)) {

                IntervalRange res = ((IntervalRange<Integer>) range).minus(
                        (IntervalRange<Integer>) literalRange.getRange());
                return new RDFLiteralRange(XMLSchema.INTEGER, res);

            } else if (valueType.equals(XMLSchema.LONG)) {

                IntervalRange res = ((IntervalRange<Integer>) range).minus(
                        (IntervalRange<Integer>) literalRange.getRange());
                return new RDFLiteralRange(XMLSchema.INTEGER, res);
            } else if (valueType.equals(XMLSchema.STRING)) {

                PrefixRange res = ((PrefixRange) range).minus(
                        (PrefixRange) literalRange.getRange());
                return new RDFLiteralRange(XMLSchema.STRING, res);
            } else if (valueType.equals(XMLSchema.DATETIME)) {
                CalendarRange res = ((CalendarRange) range).minus(
                        (CalendarRange) literalRange.getRange());
                return new RDFLiteralRange(XMLSchema.DATETIME, res);
            }
        }
		return null;
	}



	public boolean contains(Value value) {

        if (infinite) return true;
        
    	if (value instanceof Literal) {
    		Literal literal = (Literal)value;
    		
    		if (literal.getDatatype() == valueType) { 
    			if (valueType.equals(XMLSchema.INTEGER)) {    				
    				return ((IntervalRange<Integer>) range).contains(literal.intValue());
    				
    			} else if (valueType.equals(XMLSchema.LONG)) {    				
    				return ((IntervalRange<Integer>) range).contains(literal.intValue());
    				
    			} else if (valueType.equals(XMLSchema.STRING)) {            	 
    				return ((PrefixRange) range).contains(literal.stringValue());
                 
    			} else if (valueType.equals(XMLSchema.DATETIME)) {           	 
    				return ((CalendarRange) range).
    						contains(literal.calendarValue().
    								toGregorianCalendar().getTime());
    			}
    		}
    	} else if (value instanceof URI) {  
    		if (range instanceof PrefixRange)
    			return ((PrefixRange) range).contains(((URI)value).stringValue());   		 
    	}
    		
    	return false;
    }

    @SuppressWarnings("unchecked")
	public boolean contains(RDFLiteralRange literalRange) {

        if (infinite) return true;

        if (valueType.equals(literalRange.getValueType()))
        {

            if (valueType.equals(XMLSchema.INTEGER)) {

               return ((IntervalRange<Integer>) range).contains(
                                (IntervalRange<Integer>) literalRange.getRange());

            } else if (valueType.equals(XMLSchema.LONG)) {

                return ((IntervalRange<Integer>) range).contains(
                        (IntervalRange<Integer>) literalRange.getRange());
            } else if (valueType.equals(XMLSchema.STRING)) {

                return ((PrefixRange) range).contains(
                        (PrefixRange) literalRange.getRange());
            } else if (valueType.equals(XMLSchema.DATETIME)) {            	
                return ((CalendarRange) range).contains(
                        (CalendarRange) literalRange.getRange());
            }
        }

        return false;
    }

    
    @SuppressWarnings("unchecked")
	public boolean intersects(RDFLiteralRange literalRange) {

        if (infinite) return true;

        if (valueType.equals(literalRange.getValueType()))
        {

            if (valueType.equals(XMLSchema.INTEGER)) {

                return ((IntervalRange<Integer>) range).intersects(
                        (IntervalRange<Integer>) literalRange.getRange());

            } else if (valueType.equals(XMLSchema.LONG)) {

                return ((IntervalRange<Integer>) range).intersects(
                        (IntervalRange<Integer>) literalRange.getRange());
            } else if (valueType.equals(XMLSchema.STRING)) {

                return ((PrefixRange) range).intersects(
                        (PrefixRange) literalRange.getRange());
            } else if (valueType.equals(XMLSchema.DATETIME)) {

                return ((CalendarRange) range).intersects(
                        (CalendarRange) literalRange.getRange());
            }
        }

        return false;
    }

    public RDFLiteralRange tightRange(RDFLiteralRange rdfLiteralRange) {

        if (infinite) return new RDFLiteralRange();
        return null;
    }

    public URI getValueType() {
        return valueType;
    }

    public void setValueType(URI valueType) {
        this.valueType = valueType;
    }

    public Range<?> getRange() {
        return range;
    }

    public void setRange(RangeLength<?> range) {
        this.range = range;
    }

    
    public long getLength() {

        if (infinite) return Integer.MAX_VALUE;

        return range.getLength();
    }
}
