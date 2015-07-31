package gr.demokritos.iit.irss.semagrow.impl.serial;

import eu.semagrow.querylog.api.*;
import eu.semagrow.querylog.config.QueryLogConfig;
import eu.semagrow.querylog.config.QueryLogFactory;
import gr.demokritos.iit.irss.semagrow.impl.serial.config.SerialQueryLogConfig;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by angel on 10/24/14.
 */
public class SerialQueryLogFactory implements QueryLogFactory {


    public QueryLogWriter getQueryRecordLogger(QueryLogConfig config) throws QueryLogException {
        if (config instanceof SerialQueryLogConfig) {
            SerialQueryLogConfig serialConfig = (SerialQueryLogConfig)config;
            try {
                return getQueryRecordLogger(new FileOutputStream(serialConfig.getFilename()));
            } catch (FileNotFoundException e) {
                throw new QueryLogException(e);
            }
        }
        else
            throw new QueryLogException("cannot create querylog writer");
    }

    public QueryLogWriter getQueryRecordLogger(OutputStream out) {
        QueryLogWriter factory = new SerialQueryLogWriter(out);

        return factory;
    }
}
