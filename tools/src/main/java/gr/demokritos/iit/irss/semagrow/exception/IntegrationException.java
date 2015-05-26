package gr.demokritos.iit.irss.semagrow.exception;

import gr.demokritos.iit.irss.semagrow.api.QueryLogException;
import gr.demokritos.iit.irss.semagrow.log.LogWriterImpl;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;

/**
 * Created by kzam on 5/21/15.
 */
public class IntegrationException extends Exception {

    public IntegrationException(Exception e) {
        super(e);

        LogWriterImpl.getInstance().write("ERROR "+e);
        LogWriterImpl.getInstance().close();

        if(e instanceof java.lang.ClassCastException) {
            // problem with bnodes in strhist
            System.err.println("Error with BNodes: cannot be cast to URI");
            System.exit(2);
        } else if(e instanceof ArrayIndexOutOfBoundsException) {
            // problem with objects in strhist
            System.err.println("Error with Object-Range");
            System.exit(3);
        } else if(e instanceof NotImplementedException) {
            // problem with nom implemented methods
            System.err.println("Error with non-implemented parts");
            System.exit(3);
        } else if(e instanceof QueryLogException) {
            // problem in parsing a log file
            System.err.println("Error in parsing a log file");
            System.exit(4);
        } else if (e instanceof IOException) {
            // problem in handling lastQfr file
            System.err.println("Error in handling lastQfr file");
            System.exit(5);
        }
    }

    public IntegrationException(String msg) { super(msg); }
}

