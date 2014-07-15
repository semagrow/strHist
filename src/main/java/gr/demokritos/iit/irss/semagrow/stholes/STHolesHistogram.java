package gr.demokritos.iit.irss.semagrow.stholes;

import gr.demokritos.iit.irss.semagrow.api.QueryRecord;
import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.api.STHistogram;

import java.util.Collection;
import java.util.LinkedList;


/**
 * Created by angel on 7/11/14.
 */
public class STHolesHistogram<R extends Rectangle<R>> implements STHistogram<R> {

    private STHolesBucket<R> root;

    public STHolesHistogram() {
        root = null;
    }

    public STHolesHistogram(Iterable<QueryRecord<R>> workload) {
        this();
        refine(workload);
    }

    public long estimate(R rec) {
        if (root != null)
            return estimateAux(rec, root);
        else
            return 0;
    }

    private long estimateAux(R rec, STHolesBucket<R> b) {

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

    public void refine(Iterable<QueryRecord<R>> workload) {

        for (QueryRecord qfr : workload)
            refine(qfr);
    }

    public void refine(QueryRecord<R> queryRecord) {

        // get all c
        Iterable<STHolesBucket<R>> candidates = getCandidateBuckets(queryRecord);

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
    private STHolesBucket<R> shrink(STHolesBucket<R> bucket, QueryRecord<R> queryRecord) {

        //TODO: create a new rectangle / this is not the way to do it!
        R r = bucket.getBox();

        //TODO: shrink in such a way that b does not intersect with the rectangles of bucket.getChildren();

        long freq = countMatchingTuples(r, queryRecord);

        STHolesBucket<R> b = new STHolesBucket<R>(r, freq,null, null, null);

        return b;
    }

    /**
     * get STHolesBuckets that have nonempty intersection with a queryrecord
     * @param queryRecord
     * @return
     */
    private Iterable<STHolesBucket<R>> getCandidateBuckets(QueryRecord<R> queryRecord) {

        R queryBox = queryRecord.getRectangle();

        Collection<STHolesBucket<R>> candidates = new LinkedList<STHolesBucket<R>>();

        // check if there are bucket with boxes that intersect with the rectangle of the query

        //TODO:expand root so that it contains q if necessary

        candidates = getCandidateBucketsAux(root, candidates, queryBox);

        return candidates;
    }

    private Collection<STHolesBucket<R>> getCandidateBucketsAux(
            STHolesBucket<R> b, Collection<STHolesBucket<R>> candidates,
            R queryBox)
    {

        R c = b.getBox();

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
    private long countMatchingTuples(R rectangle, QueryRecord<R> queryRecord) {

        return queryRecord.getResultSet().getCardinality(rectangle);
    }

    /**
     * Create a hole (i.e. a child STHolesBucket) inside an existing bucket
     */
    private void drillHole(STHolesBucket<R> parentBucket, STHolesBucket<R> candidateHole)
    {

        if (parentBucket.getBox().equals(candidateHole.getBox())) {

            parentBucket.setFrequency(candidateHole.getFrequency());
            parentBucket.setDistinct(candidateHole.getDistinct());

        }
        else {

            //STHolesBucket bn = new STHolesBucket(holeBoundaries, holeFrequency,null,parentBucket,distinct);
            STHolesBucket<R> bn = candidateHole;
            bn.setParent(parentBucket);

            parentBucket.addChild(bn);

            for (STHolesBucket<R> bc : parentBucket.getChildren()) {

                if (bn.getBox().contains(bc.getBox())){
                    bc.setParent(bn);
                }
            }
        }
    }

    private void compact() {
        // while too many buckets merge buckets with lowest penalty
    }

    private long getPCMergePenalty(STHolesBucket<R> bp, STHolesBucket<R> bc) {
        return Math.abs(estimate(bc.getBox()) - estimate(bp.getBox()));
    }
}
