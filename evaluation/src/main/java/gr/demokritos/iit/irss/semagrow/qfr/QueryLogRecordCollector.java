package gr.demokritos.iit.irss.semagrow.qfr;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by angel on 10/22/14.
 */
public class QueryLogRecordCollector implements QueryLogHandler {

    private Collection<QueryLogRecord> collection;

    public QueryLogRecordCollector(Collection<QueryLogRecord> collection) { this.collection = collection; }

    @Override
    public void startQueryLog() throws QueryLogException {
        collection = new LinkedList<QueryLogRecord>();
    }

    @Override
    public void handleQueryRecord(QueryLogRecord queryLogRecord) throws QueryLogException {
        collection.add(queryLogRecord);
    }

    @Override
    public void endQueryLog() throws QueryLogException { }

}