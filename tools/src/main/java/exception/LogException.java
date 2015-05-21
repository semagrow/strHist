package exception;

import java.io.IOException;

/**
 * Created by kzam on 5/21/15.
 */
public class LogException extends Exception {

    public LogException(Exception e) {
        super(e);

        if(e instanceof IOException) {
            System.err.println("Error in log file");
            e.printStackTrace();

            System.exit(5);
        }
    }
}
