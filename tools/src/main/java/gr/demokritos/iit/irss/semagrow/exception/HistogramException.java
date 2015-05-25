package gr.demokritos.iit.irss.semagrow.exception;

import java.io.IOException;

/**
 * Created by kzam on 5/22/15.
 */
public class HistogramException extends Exception {

    public HistogramException(Exception e) {
        super(e);

        if(e instanceof IOException) {
            System.err.println("Error in handling histogram files");
            e.printStackTrace();

            System.exit(6);
        }
    }

    public HistogramException(String msg) {
        super(msg);

        System.exit(6);
    }
}
