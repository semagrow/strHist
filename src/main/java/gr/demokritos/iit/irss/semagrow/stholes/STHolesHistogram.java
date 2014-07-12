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

        // get all c
        Iterable<STHolesBucket> candidateBuckets = getCandidateBuckets(queryRecord);

        for (STHolesBucket b : candidateBuckets) {

            STHolesBucket c = STHolesBucket.shrink();
            //long tc = countMatchingTuples(c, queryRecord);
            //long tb = countMatchingTuples(b, queryRecord);

            //if (inaccurateEstimation())
            //    drillHole(b, c, tc);
        }

        // check if histogram must be compacted after refinement
        compact();
    }

    /**
     * get STHolesBuckets that have nonempty intersection with a queryrecord
     * @param queryRecord
     * @return
     */
    private Iterable<STHolesBucket> getCandidateBuckets(QueryRecord queryRecord) {
        Rectangle queryBox = queryRecord.getRectangle();

        // check if there are bucket with boxes that intersect with the rectangle of the query

        return null;
    }

    /**
     * Count the tuples of the query result set that match the criteria of the given bucket.
     * @param bucket
     * @param queryRecord
     * @return
     */
    private long countMatchingTuples(STHolesBucket bucket, QueryRecord queryRecord) {
        return 0;
    }

    /**
     * Create a hole (i.e. a child STHolesBucket) inside an existing bucket
     */
    private void drillHole(STHolesBucket parentBucket, Rectangle holeBoundaries, int holeFrequency) {
        
    }

    private void compact() {
        // while too many buckets merge buckets with lowest penalty
    }
}
