package gr.demokritos.iit.irss.semagrow.qfr;

import org.openrdf.model.URI;
import org.openrdf.query.algebra.TupleExpr;

import java.util.Date;
import java.util.List;

/**
 * Created by angel on 10/20/14.
 */
public interface QueryLogRecord {
    URI getEndpoint();

    TupleExpr getQuery();

    java.util.UUID getSession();

    List<String> getBindingNames();

    void setCardinality(long card);

    long getCardinality();

    void setDuration(long start, long end);

    void setResults(URI handle);

    Date getStartTime();

    Date getEndTime();

    long getDuration();

    URI getResults();
}
