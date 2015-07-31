package gr.demokritos.iit.irss.semagrow.exception;


/**
 * Created by kzam on 5/21/15.
 */
public class IntegrationException extends Exception {

    public IntegrationException(Exception e) {
        super(e);
    }

    public IntegrationException(String msg) { super(msg); }
}

