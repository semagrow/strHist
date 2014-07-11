package gr.demokritos.iit.irss.semagrow.stholes;

import gr.demokritos.iit.irss.semagrow.api.QueryRecord;
import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.api.STHistogram;

/**
 * Created by angel on 7/11/14.
 */
public class STHolesHistogram implements STHistogram {

    @Override
    public void refine(Iterable<QueryRecord> workload) {

        for (QueryRecord qfr : workload)
            refine(qfr);
    }

    @Override
    public long estimate(Rectangle rec) {
        return 0;
    }

    public void refine(QueryRecord queryRecord) {

    }


}
