package gr.demokritos.iit.irss.semagrow.qfr;

import java.io.OutputStream;

/**
 * Created by angel on 10/24/14.
 */
public class SerialQueryLogFactory implements QueryLogFactory {

    @Override
    public QueryLogHandler getQueryRecordLogger(OutputStream out) {
        QueryLogHandler factory = new SerialQueryLogHandler(out);
        try {
            factory.startQueryLog();
        } catch (QueryLogException e) {
            e.printStackTrace();
        }
        return factory;
    }
}
