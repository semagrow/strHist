package gr.demokritos.iit.irss.semagrow.stholes;

import gr.demokritos.iit.irss.semagrow.api.QueryRecord;
import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.api.STHistogram;
import gr.demokritos.iit.irss.semagrow.rdf.Stat;

import java.util.*;


/**
 * Created by angel on 7/11/14.
 */
public class STHolesHistogram<R extends Rectangle<R>> implements STHistogram<R> {


    private STHolesBucket<R> root;
    private Long maxBucketsNum;
    private Long bucketsNum;

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

            for (STHolesBucket<R> bc : b.getChildren()) {

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

    public void refine(Iterable<? extends QueryRecord<R>> workload) {

        for (QueryRecord qfr : workload)
            refine(qfr);
    }

    public void refine(QueryRecord<R> queryRecord) {

        // get all c
        Iterable<STHolesBucket<R>> candidates = getCandidateBuckets(queryRecord);

        for (STHolesBucket bucket : candidates) {

            STHolesBucket hole = shrink(bucket, queryRecord); //calculate intersection and shrink it

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

        // Find candidate hole
        R c = bucket.getBox().intersection(queryRecord.getRectangle());

        // Shrink candidate hole
        List<STHolesBucket<R>> participants = new LinkedList<STHolesBucket<R>>();


            for (STHolesBucket<R> participant : participants) {

                c.shrink(participant.getBox());
                updateParticipants(participants, bucket, c);

                if (participants.isEmpty()) {

                    break;
                }
            }


        //TODO: create a new rectangle / this is not the way to do it!
        //R r = bucket.getBox();

        //TODO: shrink in such a way that b does not intersect with the rectangles of bucket.getChildren();

        // Collect candidate hole statistics
        Stat stats= countMatchingTuples(c, queryRecord);

        // Create candidate hole bucket
        STHolesBucket<R> b = new STHolesBucket<R>(c, stats, null, null);

        return b;
    }

    private void updateParticipants(List<STHolesBucket<R>> participants,
                                    STHolesBucket<R> bucket, R c) {

        participants.clear();

        for (STHolesBucket<R> bi : bucket.getChildren()) {
            if ((c.intersects(bi.getBox())) && (!c.contains(bi.getBox()))) {

                participants.add(bi);
            }
        }
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


        if (!c.intersects(queryBox)) {
            candidates.add(b);
        }


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
    private Stat countMatchingTuples(R rectangle, QueryRecord<R> queryRecord) {

        //return queryRecord.getResultSet().getCardinality(rectangle);
        return null;
    }

    /**
     * Create a hole (i.e. a child STHolesBucket) inside an existing bucket
     */
    private void drillHole(STHolesBucket<R> parentBucket, STHolesBucket<R> candidateHole)
    {

        if (parentBucket.getBox().equals(candidateHole.getBox())) {

            Stat parentStats = new Stat(candidateHole.getStatistics().getFrequency(),
                    candidateHole.getStatistics().getDistinctCount());
            parentBucket.setStatistics(parentStats);
           // parentBucket.setFrequency(candidateHole.getFrequency());
           // parentBucket.setDistinct(candidateHole.getDistinct());
        }
        else {

            //STHolesBucket bn = new STHolesBucket(holeBoundaries, holeFrequency,null,parentBucket,distinct);
            STHolesBucket<R> bn = candidateHole;
            bn.setParent(parentBucket);

            parentBucket.addChild(bn);

            bucketsNum += 1;

            for (STHolesBucket<R> bc : parentBucket.getChildren()) {

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
        while (bucketsNum > maxBucketsNum) {

             MergeInfo bestMerge = findBestMerge(root);
             STHolesBucket<R> b1 = bestMerge.getB1();
             STHolesBucket<R> b2 = bestMerge.getB2();
             STHolesBucket<R> bn = bestMerge.getBn();

            (new STHolesBucket<R>()).merge(b1, b2, bn);
        }
    }

    private MergeInfo<R> findBestMerge(STHolesBucket<R> b) {

        MergeInfo<R> bestMerge;
        MergeInfo<R> candidateMerge;
        long minimumPenalty = Integer.MAX_VALUE;
        long penalty;
        Map.Entry<STHolesBucket<R>, Long> candidateMergedBucket;

        // Initialize buckets to be merged and resulting bucket
        STHolesBucket<R> b1 = b;
        STHolesBucket<R> b2 = b;
        STHolesBucket<R> bn = b;

        for (STHolesBucket<R> bi : b.getChildren()) {

            // Candidate parent-child merges
            candidateMergedBucket = getPCMergePenalty(b, bi);
            penalty = candidateMergedBucket.getValue();

            if (penalty  <= minimumPenalty) {

                minimumPenalty = penalty;
                b1 = b;
                b2 = bi;
                bn = candidateMergedBucket.getKey();
            }

            // Candidate sibling-sibling merges
            for (STHolesBucket<R> bj : b.getChildren()) {

                if (!bj.equals(bi)) {

                    candidateMergedBucket = getSSMergePenalty(b, bi);
                    penalty = candidateMergedBucket.getValue();

                    if (penalty  <= minimumPenalty) {

                        minimumPenalty = penalty;
                        b1 = bi;
                        b2 = bj;
                        bn = candidateMergedBucket.getKey();
                    }
                }
            }
        }

        // local best merge
        bestMerge = new MergeInfo<R>(b1, b2, bn, minimumPenalty);

        for (STHolesBucket<R> bc : b.getChildren()) {

            candidateMerge = findBestMerge(bc);

            if (candidateMerge.getPenalty() <= minimumPenalty) {

                bestMerge = candidateMerge;
            }
        }

        return bestMerge;
    }

    private Map.Entry<STHolesBucket<R>, Long>
            getPCMergePenalty(STHolesBucket<R> bp, STHolesBucket<R> bc) {

        R newBox = bp.getBox();
        long newFreq = bp.getStatistics().getFrequency();
        List<Long> newDistinct = bp.getStatistics().getDistinctCount();
        STHolesBucket<R> newParent = bp.getParent();
        Stat newStatistics = new Stat(newFreq, newDistinct);

        STHolesBucket<R> bn = new STHolesBucket<R>(newBox, newStatistics, null, newParent);
        long penalty = Math.abs(estimate(bc.getBox()) - estimate(bp.getBox()));

        AbstractMap.SimpleEntry<STHolesBucket<R>, Long> res = new AbstractMap.SimpleEntry(bn, penalty);

        return res;
    }

    private Map.Entry<STHolesBucket<R>, Long>
        getSSMergePenalty(STHolesBucket<R> b1, STHolesBucket<R> b2) {

        //TODO: Rectangle newBox = getSiblingSiblingBox(b1,b2);
        // the smallest box that encloses both b1 and b2 but does not
        // intersect partially with any other of bp
        R newBox = b1.getBox(); //just temporary

        // I contains bp's children which are enclosed by bn box
        Collection<STHolesBucket<R>> I = new ArrayList<STHolesBucket<R>>();
        STHolesBucket<R> bp = b1.getParent();

        for (STHolesBucket<R> bi : bp.getChildren() ) {

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

        for (STHolesBucket<R> bi : I) {

            curDistinct = bi.getStatistics().getDistinctCount();
            newFrequency += bi.getStatistics().getFrequency() ;

            for (int i = 0; i < newDistinct.size(); i++) {

                newDistinct.set(i,  Math.max(newDistinct.get(i), curDistinct.get(i)));
            }
        }

        //Add children
        Collection<STHolesBucket<R>> newChildren = new ArrayList<STHolesBucket<R>>();
        I.addAll(b1.getChildren());
        I.addAll(b2.getChildren());



        for (STHolesBucket<R> bi : I) {

            newChildren.add(bi);
        }


        // Create bn
        Stat newStatistics = new Stat(newFrequency, newDistinct);
        STHolesBucket<R> bn = new STHolesBucket<R>(newBox, newStatistics, newChildren, null);

        long penalty = 0;
        penalty = Math.abs(b1.getEstimate(b1.getBox()) - bn.getEstimate(b1.getBox()))
        + Math.abs(b2.getEstimate(b2.getBox())-bn.getEstimate(b2.getBox()))
        + Math.abs(bp.getEstimate(bn.getBox()) - bn.getEstimate(bn.getBox()));

        AbstractMap.SimpleEntry<STHolesBucket<R>, Long> res =
                new AbstractMap.SimpleEntry(bn, penalty);

        return res;
    }
}
