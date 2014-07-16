package gr.demokritos.iit.irss.semagrow.api;

/**
 * Created by angel on 7/15/14.
 */
public interface Rangeable<R> {

    R intersection(R r);

    R minus(R r);

    boolean contains(R r);

    boolean intersects(R r);

}
