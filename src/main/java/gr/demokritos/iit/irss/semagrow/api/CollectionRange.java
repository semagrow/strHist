package gr.demokritos.iit.irss.semagrow.api;

import java.util.Collection;

/**
 * A collection of ranges is itself a range
 * Created by angel on 7/12/14.
 */
public class CollectionRange<T> implements Range<T> {

    private Collection<Range<T>> ranges;

    public CollectionRange() {

    }

    public CollectionRange(Collection<Range<T>> ranges) {
        // do some checking first??
        this.ranges.addAll(ranges);
    }

    @Override
    public boolean contains(T item) {
        return false;
    }

    @Override
    public boolean contains(Range<T> range) {
        return false;
    }

    @Override
    public Range<T> intersect(Range<T> range) {
        return null;
    }

    @Override
    public Range<T> union(Range<T> range) {
        return null;
    }
}
