package gr.demokritos.iit.irss.semagrow.api;

/**
 * Basic interface of a (multidimensional) histogram
 * Created by angel on 7/11/14.
 */
public interface Histogram<R extends Rectangle<R>> {


    long estimate(R rectangle);

}
