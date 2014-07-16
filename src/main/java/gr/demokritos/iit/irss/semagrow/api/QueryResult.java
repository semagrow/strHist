package gr.demokritos.iit.irss.semagrow.api;

/**
 * Created by angel on 7/11/14.
 */
public interface QueryResult<R extends Rectangle<R>> {

    long getCardinality();

    long getCardinality(R rect);

}
