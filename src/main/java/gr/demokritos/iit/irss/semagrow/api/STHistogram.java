package gr.demokritos.iit.irss.semagrow.api;

/**
 * Self Tuning Histogram
 * Created by angel on 7/11/14.
 */
public interface STHistogram extends Histogram {


    void refine(Iterable<QueryRecord> workload);

}
