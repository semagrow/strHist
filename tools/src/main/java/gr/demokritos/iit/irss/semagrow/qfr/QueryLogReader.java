package gr.demokritos.iit.irss.semagrow.qfr;

import eu.semagrow.querylog.api.QueryLogException;
import eu.semagrow.querylog.api.QueryLogRecord;
import eu.semagrow.querylog.QueryLogCollector;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import eu.semagrow.querylog.impl.rdf.RDFQueryLogParser;

import gr.demokritos.iit.irss.semagrow.histogram.HistogramUtils;

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
    private QueryLogCollector handler;
    private File logFile;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private RDFQueryLogParser parser;


    static final Logger logger = LoggerFactory.getLogger(QueryLogReader.class);


    public QueryLogReader(File file) {
        this.logFile = file;

        this.handler = new QueryLogCollector(logCollection);
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
            logger.error("Error in parsing {}", logFile);

            throw new QueryLogException(e);
        }
        logger.info("Parsed {}", logFile);
    }

    public Collection<QueryRecord> adaption(String logDir) {
        HistogramUtils utils = new HistogramUtils(executor, logDir);
        Collection<QueryRecord> qr = HistogramUtils.adaptLogs(this.logCollection);

        logger.info("Successful adapt of logs");

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
            logger.warn("QueryLogException", e);
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

            logger.info("{} deleted", uri);
        }
    }

    private void writeTimestamp() {
        QfrLastWriter lastqfr = new QfrLastWriter(logFile.getParent());

        lastqfr.write(this.logFile.lastModified());
    }
}
