package gr.demokritos.iit.irss.semagrow.rdf;

import gr.demokritos.iit.irss.semagrow.api.range.Range;
import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.api.range.Rangeable;
import gr.demokritos.iit.irss.semagrow.base.range.CircleRange;
import org.openrdf.model.URI;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by katerina on 23/11/2015.
 */
public class RDFStrRange implements RangeLength<URI>, Rangeable<RDFStrRange> {

    private CircleRange range;

    public RDFStrRange(String center) {
        range = new CircleRange(center);
    }

    public RDFStrRange(String center, double radius) {
        range = new CircleRange(center, radius);
    }

    public RDFStrRange() {
        range  = new CircleRange();
    }

    private RDFStrRange(CircleRange range) { this.range = range; }

    public long getLength() { return range.getLength(); }

    public void addLength(long count) { range.addLength(count); }

    public boolean isUnit() { return range.isUnit(); }

    public boolean isEmpty() { return range.isEmpty(); }

    public boolean isInfinite() { return range.isInfinite(); }

    public boolean includes(URI elem) { return range.includes(elem.stringValue()); }

    public void expand(URI v) { range.expand(v.stringValue()); }

    public RDFStrRange intersection(RDFStrRange rdfuriRange) {
        return new RDFStrRange(range.intersection(rdfuriRange.range));
    }

    public RDFStrRange minus(RDFStrRange rdfuriRange) {
        return new RDFStrRange(range.minus(rdfuriRange.range));
    }

    public boolean contains(RDFStrRange rdfuriRange) {
        return range.contains(rdfuriRange.range);
    }

    public boolean intersects(RDFStrRange rdfuriRange) {
        return range.intersects(rdfuriRange.range);
    }


    public boolean intersects(Range<?> r) {
        if (r instanceof RDFStrRange) {
            return intersects((RDFStrRange)r);
        }
        return false;
    }

    public RDFStrRange tightRange(RDFStrRange rdfuriRange) {
        return new RDFStrRange(range.tightRange(rdfuriRange.range));
    }

    public String getCenter() { return range.getCenter(); }

    public double getRadius() {
        return range.getRadius();
    }

    @Override
    public String toString() { return range.toString(); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj instanceof RDFStrRange) {
            RDFStrRange rdfstrRange = (RDFStrRange) obj;
            return this.range.equals(rdfstrRange.range);
        }

        return false;
    }
}
