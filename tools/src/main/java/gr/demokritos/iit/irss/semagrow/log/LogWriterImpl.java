package gr.demokritos.iit.irss.semagrow.log;

import gr.demokritos.iit.irss.semagrow.exception.LogException;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kzam on 5/21/15.
 */
public class LogWriterImpl implements LogWriter{

    private static final String logFile = "/var/tmp/strhistLog.txt";
    private static FileWriter out = null;

    private static final LogWriterImpl instance = new LogWriterImpl();


    public static LogWriterImpl getInstance() {
        return instance;
    }

    @Override
    public void start() {
        try {
            out = new FileWriter(logFile, true);
        } catch (IOException e) {
            new LogException(e);
        }
    }

    @Override
    public void write(Object msg)
    {
        try {
            if(out != null)
                out.append(getCurrentDate() +" : " + msg.toString()+ "\n");
        } catch (IOException e) {
            new LogException(e);
        }

    }

    @Override
    public void close() {
        try {
            if(out != null)
                out.close();
        } catch (IOException e) {
            new LogException(e);
        }
    }

    private String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH.mm.ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
}

