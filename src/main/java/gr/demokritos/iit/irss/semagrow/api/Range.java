package gr.demokritos.iit.irss.semagrow.api;

/**
 * Created by angel on 7/12/14.
 */
public interface Range<T> {

    boolean contains(T item);

    long getLength();

    boolean isUnit();
}
