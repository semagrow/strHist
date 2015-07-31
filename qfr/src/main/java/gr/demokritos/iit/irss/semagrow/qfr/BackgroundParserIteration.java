package gr.demokritos.iit.irss.semagrow.qfr;

import eu.semagrow.querylog.api.QueryLogException;
import eu.semagrow.querylog.api.QueryLogHandler;
import eu.semagrow.querylog.api.QueryLogParser;
import eu.semagrow.querylog.api.QueryLogRecord;
import info.aduna.iteration.IterationWrapper;

import java.io.InputStream;

/**
 * Created by angel on 10/31/14.
 */
public class BackgroundParserIteration
        extends IterationWrapper<QueryLogRecord, Exception>
        implements Runnable, QueryLogHandler {

    private QueryLogParser parser;

    private InputStream in;

    private org.openrdf.http.client.QueueCursor<QueryLogRecord> queue;

    protected BackgroundParserIteration(org.openrdf.http.client.QueueCursor<QueryLogRecord> queue, QueryLogParser parser, InputStream in) {
        super(queue);
        this.queue = queue;
        this.parser = parser;
        this.in = in;
    }

    public BackgroundParserIteration(QueryLogParser parser, InputStream in) {
        this(new org.openrdf.http.client.QueueCursor<QueryLogRecord>(10), parser, in);
    }

    public void run() {

        try {
            parser.setQueryRecordHandler(this);
            parser.parseQueryLog(in);
        } catch(Exception e) {

        }
    }

    public void startQueryLog() throws QueryLogException {

    }

    public void handleQueryRecord(QueryLogRecord queryLogRecord) throws QueryLogException {
        try {
            queue.put(queryLogRecord);
        } catch (InterruptedException e) {
            throw new QueryLogException(e);
        }
    }


    public void endQueryLog() throws QueryLogException {

    }
}
