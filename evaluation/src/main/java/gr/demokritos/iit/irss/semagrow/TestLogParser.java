package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.qfr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by nickozoulis on 23/10/2014.
 */
public class TestLogParser {

    static final Logger logger = LoggerFactory.getLogger(TestLogParser.class);

    public static void main(String[] args) throws FileNotFoundException, IOException, QueryLogException, ClassNotFoundException {

        Collection<QueryLogRecord> logs = new LinkedList<QueryLogRecord>();
        Collection<RDFQueryLogRecordSerialWrapper> seriaLogs = new LinkedList<RDFQueryLogRecordSerialWrapper>();
//        QueryLogRecordCollector handler = new QueryLogRecordCollector(logs);
//
//        RDFQueryLogParser parser = new RDFQueryLogParser(handler);
//
//
//            File f = new File("/home/nickozoulis/semagrow/test_rdf_log");
//
//            logger.info("Parsing file : " + f.getName());
//
//            parser.parseQueryLog(new FileInputStream(f));
//
//            logger.info("Number of parsed query logs: " + logs.size());
//
//            for (QueryLogRecord queryRecord : logs) {
//                logger.info(queryRecord.getQuery().toString());
//                logger.info("Endpoint: " + queryRecord.getEndpoint());
//                logger.info("Cardinality: " + queryRecord.getCardinality());
//                logger.info("Duration: " + queryRecord.getDuration());
//                logger.info("Binding names: " + queryRecord.getBindingNames().toString());
//
//                seriaLogs.add(new RDFQueryLogRecordSerialWrapper(queryRecord));
//            }
//
//
//        writeSerialObject(seriaLogs, "/home/nickozoulis/semagrow/log.ser");

        seriaLogs = readSerialObject("/home/nickozoulis/semagrow/log.ser");

        for (RDFQueryLogRecordSerialWrapper seriaLog : seriaLogs) {
            logs.add(seriaLog.getQueryLogRecord());
        }

        for (QueryLogRecord queryRecord : logs) {
            logger.info(queryRecord.getQuery().toString());
            logger.info("Endpoint: " + queryRecord.getEndpoint());
            logger.info("Cardinality: " + queryRecord.getCardinality());
            logger.info("Duration: " + queryRecord.getDuration());
            logger.info("Binding names: " + queryRecord.getBindingNames().toString());
        }


    }

    private static Collection<RDFQueryLogRecordSerialWrapper> readSerialObject(String path) throws IOException, ClassNotFoundException{
        Collection<RDFQueryLogRecordSerialWrapper> seriaLogs;

        ObjectInputStream iis = new ObjectInputStream(new FileInputStream(path));
        seriaLogs = (Collection<RDFQueryLogRecordSerialWrapper>)iis.readObject();

        return seriaLogs;
    }

    private static void writeSerialObject(Collection<RDFQueryLogRecordSerialWrapper> seriaLogs, String path) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
        oos.writeObject(seriaLogs);
    }

}
