package gr.demokritos.iit.irss.semagrow.api.range;

/**
 * Created by angel on 7/12/14.
 */
public interface Range<T> {

    boolean isUnit();

    boolean isEmpty();

    boolean includes(T elem);

    void expand(T v);
}
