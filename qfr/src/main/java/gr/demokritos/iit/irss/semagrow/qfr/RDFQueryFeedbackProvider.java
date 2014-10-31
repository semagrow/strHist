package gr.demokritos.iit.irss.semagrow.qfr;

import gr.demokritos.iit.irss.semagrow.api.QueryLogParser;
import gr.demokritos.iit.irss.semagrow.api.QueryLogRecord;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryFeedbackProvider;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.base.Stat;
import gr.demokritos.iit.irss.semagrow.file.ResultMaterializationManager;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

/**
 * Created by angel on 10/31/14.
 */
public class RDFQueryFeedbackProvider implements QueryFeedbackProvider<RDFRectangle, Stat> {

    private QueryLogParser parser;

    private ExecutorService executorService;

    private ResultMaterializationManager materializationManager;

    public RDFQueryFeedbackProvider(QueryLogParser queryLogParser,
                                    ResultMaterializationManager materializationManager,
                                    ExecutorService executorService) {
        this.parser = queryLogParser;
        this.executorService = executorService;
    }


    public ResultMaterializationManager getMaterializationManager() {
        return materializationManager;
    }

    public InputStream getInputStream() throws IOException { return null; }

    @Override
    public Iterator<QueryRecord<RDFRectangle, Stat>> getQueryRecordIterator()
    {
        try {
            InputStream in = getInputStream();

            BackgroundParserIteration queryLogIter = new BackgroundParserIteration(parser, in);

            CloseableIteration<QueryRecord<RDFRectangle, Stat>, Exception> queryRecordIter =
                    new ConvertingIteration<QueryLogRecord, QueryRecord<RDFRectangle, Stat>, Exception>(queryLogIter) {
                        @Override
                        protected QueryRecord<RDFRectangle, Stat> convert(QueryLogRecord queryLogRecord) throws Exception {
                            return new QueryRecordAdapter(queryLogRecord, getMaterializationManager());
                        }
                    };

            execute(queryLogIter);
            return new IterationIterator<QueryRecord<RDFRectangle, Stat>>(queryRecordIter);
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void execute(Runnable runnable) {
        executorService.execute(runnable);
    }
}
