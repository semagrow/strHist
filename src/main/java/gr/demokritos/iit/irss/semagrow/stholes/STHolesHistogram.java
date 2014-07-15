package gr.demokritos.iit.irss.semagrow.stholes;

import gr.demokritos.iit.irss.semagrow.api.QueryRecord;
import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.api.STHistogram;
import gr.demokritos.iit.irss.semagrow.rdf.Stat;

import java.util.*;


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

        Stat stats= countMatchingTuples(r, queryRecord);

        STHolesBucket b = new STHolesBucket(r, stats, null, null);

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
    private Stat countMatchingTuples(Rectangle rectangle,
                                     QueryRecord queryRecord) {
        return null;
    }

    /**
     * Create a hole (i.e. a child STHolesBucket) inside an existing bucket
     */
    private void drillHole(STHolesBucket parentBucket, STHolesBucket candidateHole)
    {

        if (parentBucket.getBox().equals(candidateHole.getBox())) {

            Stat parentStats = new Stat(candidateHole.getStatistics().getFrequency(),
                    candidateHole.getStatistics().getDistinctCount());
           // parentBucket.setFrequency(candidateHole.getFrequency());
           // parentBucket.setDistinct(candidateHole.getDistinct());

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

        // while too many buckets compute merge penalty for each parent-child
        // and sibling pair, find the one with the minimum penalty and
        // call merge(b1,b2,bn)
    }

    private AbstractMap.SimpleEntry<STHolesBucket, Long>
            getPCMergePenalty(STHolesBucket bp, STHolesBucket bc) {

        Rectangle newBox = bp.getBox();
        long newFreq = bp.getStatistics().getFrequency();
        List<Long> newDistinct = bp.getStatistics().getDistinctCount();
        STHolesBucket newParent = bp.getParent();
        Stat newStatistics = new Stat(newFreq, newDistinct);

        STHolesBucket bn = new STHolesBucket(newBox, newStatistics, null, newParent);
        long penalty = Math.abs(estimate(bc.getBox()) - estimate(bp.getBox()));

        AbstractMap.SimpleEntry<STHolesBucket, Long> res = new AbstractMap.SimpleEntry(bn, penalty);

        return res;
    }

    private AbstractMap.SimpleEntry<STHolesBucket, Long>
    getSSMergePenalty(STHolesBucket b1, STHolesBucket b2) {

        //TODO: Rectangle newBox = getSiblingSiblingBox(b1,b2);
        // the smallest box that encloses both b1 and b2 but does not
        // intersect partially with any other of bp
        Rectangle newBox = b1.getBox(); //just temporary

        // I contains bp's children which are enclosed by bn box
        Collection<STHolesBucket> I = new ArrayList<STHolesBucket>();
        STHolesBucket bp = b1.getParent();

        for (STHolesBucket bi : bp.getChildren() ) {

            if (bi.getBox().contains(newBox)) {
                I.add(bi);
            }
        }

        // Set statistics
        long newFrequency = b1.getStatistics().getFrequency() + b2.getStatistics().getFrequency();
        List<Long> newDistinct = b1.getStatistics().getDistinctCount();
        List<Long> curDistinct = b2.getStatistics().getDistinctCount();

        for (int i = 0; i < newDistinct.size(); i++) {

            newDistinct.set(i, Math.max(newDistinct.get(i), curDistinct.get(i)));
        }

        for (STHolesBucket bi : I) {

            curDistinct = bi.getStatistics().getDistinctCount();
            newFrequency += bi.getStatistics().getFrequency() ;

            for (int i = 0; i < newDistinct.size(); i++) {

                newDistinct.set(i,  Math.max(newDistinct.get(i), curDistinct.get(i)));
            }
        }

        //Add children
        Collection<STHolesBucket> newChildren = new ArrayList<STHolesBucket>();
        I.addAll(b1.getChildren());
        I.addAll(b2.getChildren());



        for (STHolesBucket bi : I) {

            newChildren.add(bi);
        }


        // Create bn
        Stat newStatistics = new Stat(newFrequency, newDistinct);
        STHolesBucket bn = new STHolesBucket(newBox, newStatistics, newChildren,
                null);

        long penalty = 0;
        penalty = Math.abs(b1.getEstimate(b1.getBox()) - bn.getEstimate(b1.getBox()))
        + Math.abs(b2.getEstimate(b2.getBox())-bn.getEstimate(b2.getBox()))
        + Math.abs(bp.getEstimate(bn.getBox()) - bn.getEstimate(bn.getBox()));

        AbstractMap.SimpleEntry<STHolesBucket, Long> res =
                new AbstractMap.SimpleEntry(bn, penalty);

        return res;
    }
}
