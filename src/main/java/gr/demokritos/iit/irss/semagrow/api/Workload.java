package gr.demokritos.iit.irss.semagrow.api;

import gr.demokritos.iit.irss.semagrow.api.QueryRecord;

/**
 * A stream or batch of QFR (query feedback records)
 * Created by angel on 7/11/14.
 */
public interface Workload extends Iterable<QueryRecord> {

}
