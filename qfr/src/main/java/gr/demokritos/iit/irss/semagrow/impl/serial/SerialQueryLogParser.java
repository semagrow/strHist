package gr.demokritos.iit.irss.semagrow.impl.serial;

import eu.semagrow.querylog.api.QueryLogException;
import eu.semagrow.querylog.api.QueryLogHandler;
import eu.semagrow.querylog.api.QueryLogParser;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * Created by angel on 10/24/14.
 */
public class SerialQueryLogParser implements QueryLogParser {

    private QueryLogHandler handler;

    @Override
    public void setQueryRecordHandler(QueryLogHandler handler) {
        this.handler = handler;
    }

    @Override
    public void parseQueryLog(InputStream in) throws IOException, QueryLogException {
        ObjectInputStream oin = new ObjectInputStream(in);

        SerialQueryLogRecord serialRecord;
        try {

            while (true) {

                try {
                    serialRecord = (SerialQueryLogRecord) oin.readObject();

                    if (handler != null)
                        handler.handleQueryRecord(serialRecord);
                } catch (EOFException e) {
                    return;
                }
            }

        } catch (ClassNotFoundException e) {
            throw new QueryLogException(e);
        }
    }
}
