package gr.demokritos.iit.irss.semagrow.base.range;

import gr.demokritos.iit.irss.semagrow.api.range.Range;

import java.util.Collection;

/**
 * A collection of ranges is itself a range
 * Created by angel on 7/12/14.
 */
public class CollectionRange<T> implements Range<T> {

    private Collection<Range<T>> ranges;

    public CollectionRange() { }

    public CollectionRange(Collection<Range<T>> ranges) {
        // do some checking first??
        this.ranges.addAll(ranges);
    }

    public boolean contains(T item) {
        return false;
    }

    public boolean contains(Range<T> range) {
        return false;
    }

    public Range<T> intersect(Range<T> range) {
        return null;
    }

    public Range<T> union(Range<T> range) {
        return null;
    }

    public boolean isUnit() { return (ranges.size() == 1 && ranges.iterator().next().isUnit()); }

}
