package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.base.Stat;
import gr.demokritos.iit.irss.semagrow.file.FileManager;
import gr.demokritos.iit.irss.semagrow.file.ResultMaterializationManager;
import gr.demokritos.iit.irss.semagrow.qfr.*;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nickozoulis on 25/10/2014.
 */
public class TestQueryRecordAdapter {

    static final Logger logger = LoggerFactory.getLogger(TestQueryRecordAdapter.class);
    static ExecutorService executors;

    public static void main(String[] args) throws IOException, QueryLogException {

        Collection<QueryLogRecord> logs = new LinkedList<QueryLogRecord>();
        QueryLogRecordCollector handler = new QueryLogRecordCollector(logs);

        RDFQueryLogParser parser = new RDFQueryLogParser(handler);

        File f = new File("/home/nickozoulis/semagrow/test/test_rdf_log");

        logger.info("Parsing file : " + f.getName());

        parser.parseQueryLog(new FileInputStream(f));

        logger.info("Number of parsed query logs: " + logs.size());

        Iterator iter = logs.iterator();
        int i = 0;

        executors = Executors.newCachedThreadPool();

        while (iter.hasNext()) {
            QueryLogRecord queryLogRecord = (QueryLogRecord)iter.next();

            QueryRecord<RDFRectangle, Stat> queryRecord = new QueryRecordAdapter(queryLogRecord, getMateralizationManager());

            RDFRectangle rectangle = queryRecord.getRectangle();

            logger.info(rectangle.toString());

            List<RDFRectangle> rectangles = queryRecord.getResultSet().getRectangles(rectangle);

            logger.info("Numbers of rectangles: " + rectangles.size());

            if (!rectangles.isEmpty()) {
                for (RDFRectangle rect : rectangles)
                    logger.info("getRectanglesss: " + rect.toString());
            }
        }



    }

    private static ResultMaterializationManager getMateralizationManager(){
        File baseDir = new File("/home/nickozoulis/semagrow/test/");
        TupleQueryResultFormat resultFF = TupleQueryResultFormat.TSV;

        TupleQueryResultWriterRegistry registry = TupleQueryResultWriterRegistry.getInstance();
        TupleQueryResultWriterFactory writerFactory = registry.get(resultFF);
        return new FileManager(baseDir, writerFactory, executors);
    }
}
