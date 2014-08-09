package gr.demokritos.iit.irss.semagrow.api;

import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;

/**
 * Self Tuning Histogram
 * Created by angel on 7/11/14.
 */
public interface STHistogram<R extends Rectangle<R>,S> extends Histogram<R> {

    void refine(Iterable<? extends QueryRecord<R,S>> workload);

    void setMaxBucketsNum(long maxBucketsNum);

    long getBucketsNum();

}
