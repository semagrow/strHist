package gr.demokritos.iit.irss.semagrow.api.qfr;

import gr.demokritos.iit.irss.semagrow.api.Rectangle;

import java.util.List;

/**
 * Created by angel on 7/11/14.
 */
public interface QueryResult<R extends Rectangle<R>, S> {

    //long getCardinality();

    S getCardinality(R rect);

    List<R> getRectangles(R rect);
}
