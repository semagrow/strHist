package gr.demokritos.iit.irss.semagrow.rdf;

import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.api.range.Rangeable;
import gr.demokritos.iit.irss.semagrow.base.range.PrefixRange;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * Created by angel on 10/25/14.
 */
public class RDFValueRange implements RangeLength<Value>, Rangeable<RDFValueRange> {

    private PrefixRange uriRange;

    private RDFLiteralRange literalRange;

    public RDFValueRange() {}

    public long getLength() {
        long length = 0;

        if (literalRange != null)
            length += literalRange.getLength();

        if (uriRange != null)
            length += uriRange.getLength();

        return length;
    }

    public boolean isUnit() {
        return (uriRange != null && literalRange == null && uriRange.isUnit())
            || (uriRange == null && literalRange != null && literalRange.isUnit());
    }

    public boolean isEmpty() {
        return uriRange.isEmpty() && literalRange.isEmpty();
    }

    public boolean includes(Value elem) {
        if (elem instanceof URI)
            return uriRange.includes(elem.stringValue());
        else if (elem instanceof Literal)
            return literalRange.includes((Literal)elem);
        else
            return false;
    }

    public void expand(Value elem)
    {
        if (elem instanceof URI)
            uriRange.expand(elem.stringValue());
        else if (elem instanceof Literal)
            literalRange.expand((Literal) elem);
    }

    public RDFValueRange intersection(RDFValueRange rdfValueRange) {
        uriRange.intersection(rdfValueRange.uriRange);
        return null;
    }

    public RDFValueRange minus(RDFValueRange rdfValueRange) {
        return null;
    }

    public boolean contains(RDFValueRange rdfValueRange) {
        return false;
    }

    public boolean intersects(RDFValueRange rdfValueRange) {
        return false;
    }

    public RDFValueRange tightRange(RDFValueRange rdfValueRange) {
        return null;
    }
}
