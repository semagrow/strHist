package gr.demokritos.iit.irss.semagrow.rdf;


import gr.demokritos.iit.irss.semagrow.api.range.Range;
import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.api.range.Rangeable;
import gr.demokritos.iit.irss.semagrow.base.range.PrefixRange;
import org.openrdf.model.URI;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by angel on 10/25/14.
 */
public class RDFURIRange implements RangeLength<URI>, Rangeable<RDFURIRange> {

    private PrefixRange range;

    public RDFURIRange() { range = new PrefixRange(); }

    public RDFURIRange(Collection<String> prefixes) {
        range = new PrefixRange(new ArrayList<String>(prefixes));
    }

    private RDFURIRange(PrefixRange range) { this.range = range; }

    public long getLength() { return range.getLength(); }

    public boolean isUnit() { return range.isUnit(); }

    public boolean isEmpty() { return range.isEmpty(); }

    public boolean isInfinite() { return range.isInfinite(); }

    public boolean includes(URI elem) { return range.includes(elem.stringValue()); }

    public void expand(URI v) { range.expand(v.stringValue()); }

    public RDFURIRange intersection(RDFURIRange rdfuriRange) {
        return new RDFURIRange(range.intersection(rdfuriRange.range));
    }

    public RDFURIRange minus(RDFURIRange rdfuriRange) {
        return new RDFURIRange(range.minus(rdfuriRange.range));
    }

    public boolean contains(RDFURIRange rdfuriRange) {
        return range.contains(rdfuriRange.range);
    }

    public boolean intersects(RDFURIRange rdfuriRange) {
        return range.intersects(rdfuriRange.range);
    }

    public RDFURIRange tightRange(RDFURIRange rdfuriRange) {
        return new RDFURIRange(range.tightRange(rdfuriRange.range));
    }

    public ArrayList<String> getPrefixList() {
        return range.getPrefixList();
    }

    @Override
    public String toString() { return range.toString(); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj instanceof RDFURIRange) {
            RDFURIRange rdfuriRange = (RDFURIRange) obj;
            return this.range.equals(rdfuriRange.range);
        }

        return false;
    }



    public boolean intersects(Range<?> r) {
        if (r instanceof RDFURIRange) {
            return intersects((RDFURIRange) r);
        }
        return false;
    }

}
