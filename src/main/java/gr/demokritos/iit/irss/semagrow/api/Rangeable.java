package gr.demokritos.iit.irss.semagrow.api;

/**
 * Created by angel on 7/15/14.
 */
public interface Rangeable<R> {

    R intersection(R rect);

    boolean contains(R rect);

    boolean intersects(R rect);

}
