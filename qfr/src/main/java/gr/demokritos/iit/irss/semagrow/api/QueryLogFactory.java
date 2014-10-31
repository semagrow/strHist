package gr.demokritos.iit.irss.semagrow.api;

import java.io.OutputStream;

/**
 * Created by angel on 10/21/14.
 */
public interface QueryLogFactory {

    QueryLogHandler getQueryRecordLogger(OutputStream out);

}
