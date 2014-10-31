package gr.demokritos.iit.irss.semagrow.base;

/**
 * Created by angel on 10/31/14.
 */
public interface Experiment extends Runnable {

    void initialize();

    void shutdown();
}
