package gr.demokritos.iit.irss.semagrow.stholes;

import gr.demokritos.iit.irss.semagrow.api.QueryRecord;
import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.api.STHistogram;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;


/**
 * Created by angel on 7/11/14.
 */
public class STHolesHistogram implements STHistogram {

    private STHolesBucket root;

    public STHolesHistogram() {
        root = null;
    }

    public STHolesHistogram(Iterable<QueryRecord> workload) {
        this();
        refine(workload);
    }

    public long estimate(Rectangle rec) {
        if (root != null)
            return estimateAux(rec, root);
        else
            return 0;
    }

    private long estimateAux(Rectangle rec, STHolesBucket b) {

        boolean isEnclosingBucket = false;
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

        if (isEnclosingBucket)
            return b.getEstimate(rec);
        else
            return est;
    }

    public void refine(Iterable<QueryRecord> workload) {

        for (QueryRecord qfr : workload)
            refine(qfr);
    }

    public void refine(QueryRecord queryRecord) {

        // get all c
        Iterable<STHolesBucket> candidates = getCandidateBuckets(queryRecord);

        for (STHolesBucket bucket : candidates) {

            STHolesBucket hole = shrink(bucket, queryRecord);

            //if (inaccurateEstimation())
            if (true)
                drillHole(bucket, hole);
        }

        // check if histogram must be compacted after refinement
        compact();
    }

    /**
     * creates a new bucket that has a rectangle that does not intersect with the children of {bucket}
     * and contains the number of tuples that matches the queryRecord
     * @param bucket
     * @param queryRecord
     * @return
     */
    private STHolesBucket shrink(STHolesBucket bucket, QueryRecord queryRecord) {

        //TODO: create a new rectangle / this is not the way to do it!
        Rectangle r = bucket.getBox();

        //TODO: shrink in such a way that b does not intersect with the rectangles of bucket.getChildren();

        long freq = countMatchingTuples(r, queryRecord);

        STHolesBucket b = new STHolesBucket(r,freq,null,null,null);

        return b;
    }

    /**
     * get STHolesBuckets that have nonempty intersection with a queryrecord
     * @param queryRecord
     * @return
     */
    private Iterable<STHolesBucket> getCandidateBuckets(QueryRecord queryRecord) {

        Rectangle queryBox = queryRecord.getRectangle();

        Collection<STHolesBucket> candidates = new LinkedList<STHolesBucket>();

        // check if there are bucket with boxes that intersect with the rectangle of the query

        //TODO:expand root so that it contains q if necessary

        candidates = getCandidateBucketsAux(root, candidates, queryBox);

        return candidates;
    }

    private Collection<STHolesBucket> getCandidateBucketsAux(
            STHolesBucket b, Collection<STHolesBucket> candidates,
            Rectangle queryBox) {

        Rectangle c = b.getBox();

        c = c.intersection(queryBox);

        if (c != null)
            candidates.add(b);

        for (STHolesBucket bc : b.getChildren())
            getCandidateBucketsAux(bc,candidates,queryBox);

        return candidates;
    }


    /**
     * Count the tuples of the query result set that match the criteria of the given bucket.
     * @param rectangle
     * @param queryRecord
     * @return
     */
    private long countMatchingTuples(Rectangle rectangle,
                                     QueryRecord queryRecord) {
        return 0;
    }

    /**
     * Create a hole (i.e. a child STHolesBucket) inside an existing bucket
     */
    private void drillHole(STHolesBucket parentBucket, STHolesBucket candidateHole)
    {

        if (parentBucket.getBox().equals(candidateHole.getBox())) {

            parentBucket.setFrequency(candidateHole.getFrequency());
            parentBucket.setDistinct(candidateHole.getDistinct());

        }
        else {

            //STHolesBucket bn = new STHolesBucket(holeBoundaries, holeFrequency,null,parentBucket,distinct);
            STHolesBucket bn = candidateHole;
            bn.setParent(parentBucket);

            parentBucket.addChild(bn);

            for (STHolesBucket bc : parentBucket.getChildren()) {

                if (bn.getBox().contains(bc.getBox())){
                    bc.setParent(bn);
                }
            }
        }
    }

    private void compact() {
        // while too many buckets merge buckets with lowest penalty
    }

    private long getPCMergePenalty(STHolesBucket bp, STHolesBucket bc) {
        return Math.abs(estimate(bc.getBox()) - estimate(bp.getBox()));
    }
}
