package gr.demokritos.iit.irss.semagrow.rdf;

import gr.demokritos.iit.irss.semagrow.api.*;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * Created by angel on 7/15/14.
 */
public class RDFLiteralRange
        implements Range<Value>, Rangeable<RDFLiteralRange>
{

    private URI valueType;
    private Range<?> range;

    public RDFLiteralRange(URI valueType, Range<?> range) {
        this.valueType = valueType;
        this.range = range;
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

    public boolean contains(RDFLiteralRange rect) {
        return false;
    }

    @Override
    public boolean intersects(RDFLiteralRange rect) {
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

    public void setRange(Range<?> range) {
        this.range = range;
    }
}
