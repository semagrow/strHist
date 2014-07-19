package gr.demokritos.iit.irss.semagrow.api;

/**
 * a query feedback record is essentially a query together with its resultset
 * Created by angel on 7/11/14.
 */
public interface QueryRecord<R extends Rectangle<R>> {

    String getQuery();

    R getRectangle();

    QueryResult<R> getResultSet();
}
