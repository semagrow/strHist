package gr.demokritos.iit.irss.semagrow.qfr;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Created by angel on 10/24/14.
 */
public class SerialQueryLogHandler implements QueryLogHandler {

    private ObjectOutputStream oout;
    private OutputStream out;

    public SerialQueryLogHandler(OutputStream out) {
        this.out = out;
    }

    @Override
    public void startQueryLog() throws QueryLogException {

        try {
            oout = new ObjectOutputStream(out);
        } catch (IOException e) {
            throw new QueryLogException(e);
        }

    }

    @Override
    public void handleQueryRecord(QueryLogRecord queryLogRecord) throws QueryLogException {
        try {
            oout.writeObject(queryLogRecord);
        } catch (IOException e) {
            throw new QueryLogException(e);
        }
    }

    @Override
    public void endQueryLog() throws QueryLogException {
        try {
            oout.close();
        } catch(IOException e) {
            throw new QueryLogException(e);
        }
    }

}
