package gr.demokritos.iit.irss.semagrow.impl.serial;

import eu.semagrow.querylog.api.QueryLogException;
import eu.semagrow.querylog.api.QueryLogRecord;
import eu.semagrow.querylog.api.QueryLogWriter;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Created by angel on 10/24/14.
 */
public class SerialQueryLogWriter implements QueryLogWriter {

    private ObjectOutputStream oout;
    private OutputStream out;

    public SerialQueryLogWriter(OutputStream out) {
        this.out = out;
    }

    public void startQueryLog() throws QueryLogException {

        try {
            oout = new ObjectOutputStream(out);
        } catch (IOException e) {
            throw new QueryLogException(e);
        }

    }

    public void handleQueryRecord(QueryLogRecord queryLogRecord) throws QueryLogException {
        try {
            oout.writeObject(new SerialQueryLogRecord(queryLogRecord));
        } catch (IOException e) {
            throw new QueryLogException(e);
        }
    }


    public void endQueryLog() throws QueryLogException {
        try {
            oout.close();
        } catch(IOException e) {
            throw new QueryLogException(e);
        }
    }

}
