package gr.demokritos.iit.irss.semagrow.qfr;

import gr.demokritos.iit.irss.semagrow.exception.IntegrationException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by kzam on 5/21/15.
 */
public class QfrLastWriter {

    private static String logFile;
    private static final String suffix = "/lastQfr.txt";
    private static FileWriter out = null;

    public QfrLastWriter(String logDir) {
        this.logFile = logDir + suffix;
    }

    public QfrLastWriter() {
    }

    private void start() {
        try {
            out = new FileWriter(logFile);
        } catch (IOException e) {
            createFile();
           
        }
    }

    public void write(Object msg) {
        start();
        //message is always a timestamp - long type
        try {
            if(out != null)
                out.append(Long.toString((Long) msg));
        } catch (IOException e) {
            new IntegrationException(e);
        } finally {
            close();
        }


    }

    private void close() {
        try {
            if(out != null)
                out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFile() {
        File file = new File(logFile);

        if(! file.exists()) {
            try {

                if(file.createNewFile()) {
                    write(0);
                }
            } catch (IOException e) {
                new IntegrationException(e);
            }
        }

        new IntegrationException("Error in opening lastqfr file");


    }
}
