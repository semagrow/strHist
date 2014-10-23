package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.qfr.QueryLogException;
import gr.demokritos.iit.irss.semagrow.qfr.QueryLogRecord;
import gr.demokritos.iit.irss.semagrow.qfr.QueryLogRecordCollector;
import gr.demokritos.iit.irss.semagrow.qfr.RDFQueryLogParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by nickozoulis on 23/10/2014.
 */
public class TestLogParser {

    public static void main(String[] args) throws FileNotFoundException, IOException, QueryLogException {

        Collection<QueryLogRecord> logs;
        QueryLogRecordCollector handler = new QueryLogRecordCollector();

        RDFQueryLogParser parser = new RDFQueryLogParser(handler);

        File[] files = new File("/var/tmp/").listFiles();

        for (File f : files) {

            handler.startQueryLog();

            parser.parseQueryLog(new FileInputStream(f));

            logs = handler.getCollection();

            for (QueryLogRecord queryRecord : logs) {
                System.out.println(queryRecord);
            }

        }

    }
}
