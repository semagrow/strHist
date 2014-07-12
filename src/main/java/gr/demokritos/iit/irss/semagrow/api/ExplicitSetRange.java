package gr.demokritos.iit.irss.semagrow.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Defines a set of T explicitly by defining each member of the set.
 * Created by angel on 7/12/14.
 */
public class ExplicitSetRange<T> implements Range<T> {

    private Set<T> items;

    public ExplicitSetRange(Collection<T> items) {
        items = new HashSet<T>();
        items.addAll(items);
    }

    @Override
    public boolean contains(T item) {
        return items.contains(item);
    }

    @Override
    public boolean contains(Range<T> range) {
        if (range instanceof ExplicitSetRange)
            return items.containsAll(((ExplicitSetRange)range).items);

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
