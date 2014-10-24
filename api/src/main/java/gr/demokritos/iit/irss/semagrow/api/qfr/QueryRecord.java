package gr.demokritos.iit.irss.semagrow.api.qfr;

import gr.demokritos.iit.irss.semagrow.api.Rectangle;

/**
 * a query feedback record is essentially a query together with its resultset
 * Created by angel on 7/11/14.
 */
public interface QueryRecord<R extends Rectangle<R>,S> {

    @Deprecated
    String getQuery();

    R getRectangle();

    QueryResult<R,S> getResultSet();
}
