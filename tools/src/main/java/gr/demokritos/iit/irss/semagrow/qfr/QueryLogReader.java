package gr.demokritos.iit.irss.semagrow.qfr;

import gr.demokritos.iit.irss.semagrow.api.QueryLogException;
import gr.demokritos.iit.irss.semagrow.api.QueryLogRecord;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.impl.QueryLogRecordCollector;
import gr.demokritos.iit.irss.semagrow.impl.rdf.RDFQueryLogParser;
import gr.demokritos.iit.irss.semagrow.histogram.HistogramUtils;
import gr.demokritos.iit.irss.semagrow.log.LogWriterImpl;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by kzam on 5/21/15.
 */
public class QueryLogReader {

    private Collection<QueryLogRecord> logCollection = new LinkedList<QueryLogRecord>();
    private QueryLogRecordCollector handler;
    private File logFile;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private RDFQueryLogParser parser;


    static final Logger logger = LoggerFactory.getLogger(QueryLogReader.class);


    public QueryLogReader(File file) {
        this.logFile = file;

        this.handler = new QueryLogRecordCollector(logCollection);
        this.parser = new RDFQueryLogParser(handler);
    }

    /**
     * gets the metadata from the log file
     * @throws java.io.IOException
     */
    public void readData() throws IOException, QueryLogException {

        InputStream inputstream = new FileInputStream(logFile);

        try {

            this.parser.parseQueryLog(inputstream);

        } catch (QueryLogException e) {
            logger.error("Error in parsing "+logFile.getName());
            LogWriterImpl.getInstance().write("Error in parsing "+logFile.getName());

            throw new QueryLogException(e);
        }
        logger.info("Parsed "+logFile.getName());
        System.out.println("Parsed "+logFile.getName());

        LogWriterImpl.getInstance().write("Parsed "+logFile.getName());
    }

    public Collection<QueryRecord> adaption(String logDir) {
        HistogramUtils utils = new HistogramUtils(executor, logDir);
        Collection<QueryRecord> qr = utils.adaptLogs(this.logCollection);

        logger.info("Successful adapt of logs");
        System.out.println("Successful adapt of logs");

        return qr;
    }

    /**
     * should be called after refinement
     * deletes result files, closes handler, shutDown executor
     */
    public void shutdown(boolean delFlag)
    {
        if(delFlag)
            deleteResults();

        try {
            this.handler.endQueryLog();
        } catch (QueryLogException e) {
            e.printStackTrace();
        }

        this.executor.shutdown();

        writeTimestamp();
    }

    private void deleteResults()
    {
        Iterator<QueryLogRecord> iter = logCollection.iterator();
        QueryLogRemover remover = QueryLogRemover.getInstance();

        while(iter.hasNext()) {
            URI uri = iter.next().getResults();
            try {
                remover.deleteResults(uri);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                continue;
            }

            LogWriterImpl.getInstance().write(uri.toString()+" deleted");
        }
    }

    private void writeTimestamp() {
        QfrLastWriter lastqfr = new QfrLastWriter(logFile.getParent());

        lastqfr.write(this.logFile.lastModified());
    }
}
