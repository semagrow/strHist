package gr.demokritos.iit.irss.semagrow.rdf;

import gr.demokritos.iit.irss.semagrow.api.*;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.XMLSchema;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;

/**
 * Created by angel on 7/15/14.
 */
public class RDFLiteralRange
        implements RangeLength<Value>, Rangeable<RDFLiteralRange>
{

    private URI valueType;
    private RangeLength<?> range;


    public RDFLiteralRange(URI valueType, RangeLength<?> range) {

        this.valueType = valueType;
        this.range = range;
    }

    public RDFLiteralRange(int low, int high) {
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

    public RDFLiteralRange(XMLGregorianCalendar begin, XMLGregorianCalendar end) {
        this(XMLSchema.DATETIME, new CalendarRange(begin, end));
    }


    public boolean isUnit() {
        return range.isUnit();
    }

    public RDFLiteralRange intersection(RDFLiteralRange literalRange) {


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


    public RDFLiteralRange minus(RDFLiteralRange rdfLiteralRange) {
        return null;
    }

    public boolean contains(RDFLiteralRange literalRange) {


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

    
    public boolean intersects(RDFLiteralRange literalRange) {

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

    @Override
    public long getLength() {

        return range.getLength();
    }
}
