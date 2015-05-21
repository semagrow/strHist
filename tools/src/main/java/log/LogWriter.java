package log;

/**
 * Created by kzam on 5/21/15.
 */
public interface LogWriter {

    void start();

    void write(Object msg);

    void close();
}

