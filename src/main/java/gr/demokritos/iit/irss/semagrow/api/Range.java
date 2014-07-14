package gr.demokritos.iit.irss.semagrow.api;

/**
 * Created by angel on 7/12/14.
 */
public interface Range<T> {

    boolean contains(T item);

    boolean contains(Range<T> range);

    Range<T> intersect(Range<T> range);

    Range<T> union(Range<T> range);

    //long getLength();
}
