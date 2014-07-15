package gr.demokritos.iit.irss.semagrow.api;

/**
 * Self Tuning Histogram
 * Created by angel on 7/11/14.
 */
public interface STHistogram<R extends Rectangle<R>> extends Histogram<R> {

    void refine(Iterable<? extends QueryRecord<R>> workload);

}
