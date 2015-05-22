package qfr;

import exception.IntegrationException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by kzam on 5/21/15.
 */
public class QfrLastWriter {

    private static final String logFile = "/var/tmp/log/lastQfr.txt";
    private static FileWriter out = null;

    private static final QfrLastWriter instance = new QfrLastWriter();

    public static QfrLastWriter getInstance() {
        return instance;
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
