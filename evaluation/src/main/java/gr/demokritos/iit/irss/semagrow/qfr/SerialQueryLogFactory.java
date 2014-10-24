package gr.demokritos.iit.irss.semagrow.qfr;

import java.io.OutputStream;

/**
 * Created by angel on 10/24/14.
 */
public class SerialQueryLogFactory implements QueryLogFactory {

    @Override
    public QueryLogHandler getQueryRecordLogger(OutputStream out) {
        return new SerialQueryLogHandler(out);
    }
}
