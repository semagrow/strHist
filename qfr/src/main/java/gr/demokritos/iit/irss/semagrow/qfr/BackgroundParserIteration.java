package gr.demokritos.iit.irss.semagrow.qfr;

import gr.demokritos.iit.irss.semagrow.api.QueryLogException;
import gr.demokritos.iit.irss.semagrow.api.QueryLogHandler;
import gr.demokritos.iit.irss.semagrow.api.QueryLogParser;
import gr.demokritos.iit.irss.semagrow.api.QueryLogRecord;
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

    @Override
    public void run() {

        try {
            parser.setQueryRecordHandler(this);
            parser.parseQueryLog(in);
        } catch(Exception e) {

        }
    }

    @Override
    public void startQueryLog() throws QueryLogException {

    }

    @Override
    public void handleQueryRecord(QueryLogRecord queryLogRecord) throws QueryLogException {
        try {
            queue.put(queryLogRecord);
        } catch (InterruptedException e) {
            throw new QueryLogException(e);
        }
    }

    @Override
    public void endQueryLog() throws QueryLogException {

    }
}
