package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.qfr.QueryLogException;
import gr.demokritos.iit.irss.semagrow.qfr.QueryLogRecord;
import gr.demokritos.iit.irss.semagrow.qfr.QueryLogRecordCollector;
import gr.demokritos.iit.irss.semagrow.qfr.RDFQueryLogParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by nickozoulis on 23/10/2014.
 */
public class TestLogParser {

    static final Logger logger = LoggerFactory.getLogger(TestLogParser.class);

    public static void main(String[] args) throws FileNotFoundException, IOException, QueryLogException {

        Collection<QueryLogRecord> logs = new LinkedList<QueryLogRecord>();
        QueryLogRecordCollector handler = new QueryLogRecordCollector(logs);

        RDFQueryLogParser parser = new RDFQueryLogParser(handler);


            File f = new File("/home/nickozoulis/semagrow/test_rdf_log");

            logger.info("Parsing file : " + f.getName());

            parser.parseQueryLog(new FileInputStream(f));

            logger.info("Number of parsed query logs: " + logs.size());

            for (QueryLogRecord queryRecord : logs) {
                logger.info(queryRecord.getQuery().toString());
                logger.info("Endpoint: " + queryRecord.getEndpoint());
                logger.info("Cardinality: " + queryRecord.getCardinality());
                logger.info("Duration: " + queryRecord.getDuration());
                logger.info("Binding names: " + queryRecord.getBindingNames().toString());
            }



    }

}
