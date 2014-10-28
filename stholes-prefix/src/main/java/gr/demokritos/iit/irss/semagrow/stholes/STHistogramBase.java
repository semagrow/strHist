package gr.demokritos.iit.irss.semagrow.stholes;

import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.api.STHistogram;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.base.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Created by angel on 10/24/14.
 */
public abstract class STHistogramBase<R extends Rectangle<R>, S> implements STHistogram<R,S> {

    static final Logger logger = LoggerFactory.getLogger(STHolesHistogram.class);

    public STHistogramBase() { }

    public STHistogramBase(Iterator<? extends QueryRecord<R,S>> workload) {
        refine(workload);
    }

    public STHistogramBase(Iterable<? extends QueryRecord<R,S>> workload) {
        this(workload.iterator());
    }

    public void refine(Iterator<? extends QueryRecord<R,S>> workload) {

        //logger.debug("Number of buckets before refine: " + bucketsNum);
        int i = 1;
        while (workload.hasNext()) {
            logger.info("Refining query no"+i);
            i++;
            refine(workload.next());
        }

        //logger.debug("Number of buckets after refine: " + bucketsNum);
    }

    public abstract void refine(QueryRecord<R,S> queryRecord);


}
