package gr.demokritos.iit.irss.semagrow.stholes;

import gr.demokritos.iit.irss.semagrow.api.QueryRecord;
import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.api.STHistogram;

import java.util.ArrayList;

/**
 * Created by angel on 7/11/14.
 */
public class STHolesHistogram implements STHistogram {

    private STHolesBucket root;


    public void refine(Iterable<QueryRecord> workload) {

        for (QueryRecord qfr : workload)
            refine(qfr);
    }

    public long estimate(Rectangle rec) {

        //return estimateAux(rec,root);
        return 0;
    }

    private long estimateAux(Rectangle rec, STHolesBucket b) {

       /* boolean isEnclosingBucket = false;
        long est = 0;

        if ((b.getBox()).contains(rec)) { //unnecessary
            isEnclosingBucket = true;
            for (STHolesBucket bc : b.getChildren()) {
                if ((bc.getBox()).contains(rec)) {
                    isEnclosingBucket = false;
                    est = estimateAux(rec, bc);
                    break;
                }
            }
        }
        if (isEnclosingBucket) return b.getEstimate(rec);
        return est; */
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

        //TODO:expand root so that it contains q if necessary

        for (STHolesBucket b : root.getChildren() ) {

        }

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
    private void drillHole(STHolesBucket parentBucket, Rectangle holeBoundaries, long holeFrequency,
                           ArrayList<Long> distinct) {
        /*
        if (parentBucket.getBox().equals(holeBoundaries)){
            parentBucket.setFrequency(holeFrequency);
            parentBucket.setDistinct(distinct);
        }
        else {
            STHolesBucket bn = new STHolesBucket(holeBoundaries,holeFrequency,null,parentBucket,distinct);
            parentBucket.addChild(bn);
            for (STHolesBucket bc : parentBucket.getChildren()) {
                if (bn.getBox().contains(bc.getBox())){
                    bc.setParent(bn);
                }
            }
        }*/
        
    }

    private void compact() {
        // while too many buckets merge buckets with lowest penalty
    }
}
