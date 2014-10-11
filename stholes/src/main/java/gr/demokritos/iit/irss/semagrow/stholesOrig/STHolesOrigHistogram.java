package gr.demokritos.iit.irss.semagrow.stholesOrig;

import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryResult;
import gr.demokritos.iit.irss.semagrow.api.RectangleWithVolume;
import gr.demokritos.iit.irss.semagrow.api.STHistogram;
import org.json.simple.JSONObject;

import java.util.*;

/**
 * Created by efi on 6/8/2014.
 */
public class STHolesOrigHistogram<R extends RectangleWithVolume<R>> implements STHistogram<R,Long> {

    private STHolesOrigBucket<R> root;
    public long maxBucketsNum;
    public int epsilon = 0;
    private long bucketsNum = 0;


    public long pcMergesNum = 0;
    public long ssMergesNum = 0;



    public STHolesOrigHistogram() {
        //todo: choose a constant
        maxBucketsNum = 1000;
        root = null;
        bucketsNum += bucketsNum;
    }

    public STHolesOrigHistogram(Iterable<QueryRecord<R,Long>> workload) {
        this();
        refine(workload);
    }

    //Tested
    /**
     * estimates the number of tuples
     * that match rectangle {rec}
     * @param rec rectangle
     * @return number of tuples
     */
    public long estimate(R rec) {

        if (root != null)
            return (long)Math.ceil(root.getEstimate(rec));
        else
            return 0;
    }



    public void refine(Iterable<? extends QueryRecord<R,Long>> workload) {

        for (QueryRecord<R,Long> qfr : workload)
            refine(qfr);

        System.out.println(bucketsNum);
        for (STHolesOrigBucket<R> bc : root.getChildren()) {
            System.out.println(bc);
            for (STHolesOrigBucket<R> bcc : bc.getChildren()) {
                System.out.println(bcc);
            }
        }
    }


    public void setMaxBucketsNum(long maxBucketsNum) {
        this.maxBucketsNum = maxBucketsNum;
    }


    public long getBucketsNum() {
        return bucketsNum;
    }

    /**
     * refines histogram using query feedback
     * @param queryRecord query feedback
     */
    public void refine(QueryRecord<R,Long> queryRecord) {


        List<R> rects = new ArrayList<R>();
        if (queryRecord.getRectangle().isInfinite()) {
            rects.addAll( queryRecord.getResultSet().getRectangles(queryRecord.getRectangle()));
        } else {
            rects.add(queryRecord.getRectangle());
        }

        for (R rect : rects) {
            // check if root is null
            if (root == null) {

                root = new STHolesOrigBucket<R>(rect, 0, null, null);
                bucketsNum += 1;
            } else {

                // expand root
                if (!root.getBox().contains(rect)) {

                    // expand root box so that it contains q
                    R boxN = root.getBox().computeTightBox(rect);
                    //     System.out.println("Rectangle: " + queryRecord.getRectangle());
                    //     System.out.println("Box: " + root.getBox());

                    long freqRect = countMatchingTuples(rect, queryRecord);
                    // freqN = freq(root) + freq(q)

                    long freqN = freqRect +
                            root.getFrequency();

                    root.setBox(boxN);
                    root.setFrequency(freqN);

                }
            }


            // get all c
            Iterable<STHolesOrigBucket<R>> candidates = getCandidateBuckets(rect);

            for (STHolesOrigBucket<R> bucket : candidates) {
                //System.out.println("<<<>>> Candidate: " + bucket);
                //System.out.println("--------------------------------------------------");
                STHolesOrigBucket<R> hole = shrink(bucket, rect, queryRecord); //calculate intersection and shrink it
                //System.out.println("<<<>>> Hole: " + hole);
                if  (!hole.getBox().isEmpty() && isInaccurateEstimation(hole))
                    drillHole(bucket, hole);
            }
        }

        // check if histogram must be compacted after refinement
        System.out.println("Histogram refined with query: " + queryRecord.getRectangle().getRange(0));
        compact();
    }

    //Tested
    private boolean isInaccurateEstimation(STHolesOrigBucket<R> hole) {

        //int epsilon = 0; //todo: adjust parameter
        long actualStatistics = hole.getFrequency();


        long curEstimation = estimate(hole.getBox());

        return (Math.abs(actualStatistics - curEstimation) > epsilon);
    }

    //Tested
    /**
     * creates a new bucket that has a rectangle that does not intersect with the children of {bucket}
     * and contains the number of tuples that matches the queryRecord
     * @param bucket parent bucket
     * @param queryRecord query feedback
     * @return shrinked bucket
     */
    private STHolesOrigBucket<R> shrink(STHolesOrigBucket<R> bucket, R rect, QueryRecord<R,Long> queryRecord) {



        // Find candidate hole
        R c = bucket.getBox().intersection(rect);

        long Tb = countMatchingTuples(c, queryRecord);

        // Shrink candidate hole in such a way that b does not intersect
        // with the rectangles of bucket.getChildren();
        List<STHolesOrigBucket<R>> participants = new LinkedList<STHolesOrigBucket<R>>();

        updateParticipants(participants, bucket, c);
        //for (STHolesOrigBucket<R> participant : participants) {

        if (!participants.isEmpty()) {
            STHolesOrigBucket<R> bucketForExclusion = participants.get(0);
            Double preserved;
            double maxPreserved = 0;
            int bestDim = 0;
            Map.Entry<Double, Integer> candidateShrink;

            while (!participants.isEmpty()) {

                for (STHolesOrigBucket<R> participant : participants) {
                    candidateShrink = c.getShrinkInfo(participant.getBox());
                    preserved = candidateShrink.getKey();

                    if (preserved >= maxPreserved) {

                        bestDim = candidateShrink.getValue();
                        bucketForExclusion = participant;
                    }
                }

                c.shrink(bucketForExclusion.getBox(), bestDim);
                updateParticipants(participants, bucket, c);
            }

        }

        // Collect candidate hole statistics
        long freq = 0;



        //!!Claim: if v(q intersection b) is 0, then b's children
        //that are enclosed in c, cover all c's space. c will be drilled
        // and will be merged later if needed
        if (bucket.getIntersectionWithRecVolume(rect) == 0) {

             freq= (long)Math.ceil(Tb * ((double)c.getVolume())/
                    bucket.getBox().intersection(rect).getVolume());
        } else {

            freq = (long)Math.ceil(Tb * ((double)c.getVolume())/
                    bucket.getIntersectionWithRecVolume(rect));

            if ((double)c.getVolume() > bucket.getIntersectionWithRecVolume(rect) ) {

                //System.err.println("This should not happen! Original" +
                  //      "frequency: " + Tb + " and new frequency: " + freq);
                //System.err.println(bucket.getIntersectionWithRecVolume(rect));

                freq= (long)Math.ceil(Tb * ((double)c.getVolume())/
                        bucket.getBox().intersection(rect).getVolume());

                if (freq > Tb) {
                    System.err.println("This should not happen! Original" +
                          "frequency: " + Tb + " and new frequency: " + freq);
                }

            }



        }


        // Create candidate hole bucket

        return new STHolesOrigBucket<R>(c, freq, null, null);

    }

    //Tested
    /**
     * finds {bucket}'s children that partially intersect
     * with candidate hole c and stores them
     * in {participants} list
     * @param participants list of participants
     * @param bucket parent bucket
     * @param c candidate hole
     */
    private void updateParticipants(List<STHolesOrigBucket<R>> participants,
                                    STHolesOrigBucket<R> bucket, R c) {

        participants.clear();

        for (STHolesOrigBucket<R> bi : bucket.getChildren()) {

            if ((c.intersects(bi.getBox())) && (!c.contains(bi.getBox()))) {

                participants.add(bi);
            }
        }
    }

    /**
     * finds the smallest box that encloses both {b1} and {b2} and
     * does not intersect partially with any other child of their parent
     * @param b1 sibling 1
     * @param b2 sibling 2
     * @return box after merge
     */
    private R getSiblingSiblingBox(STHolesOrigBucket<R> b1, STHolesOrigBucket<R> b2) {


        // Get parent
        STHolesOrigBucket<R> bp = b1.getParent(); //todo: check if they are siblings
        // Find tightly enclosing box
        R c = b1.getBox().computeTightBox(b2.getBox());


        // Expand tightly enclosing box
        List<STHolesOrigBucket<R>> participants = new LinkedList<STHolesOrigBucket<R>>();

        updateParticipants(participants, bp, c);

        for (STHolesOrigBucket<R> participant : participants) {

            c = c.computeTightBox(participant.getBox());

            updateParticipants(participants, bp, c);

            if (participants.isEmpty()) {

                break;
            }
        }


        return c;
    }

    //Tested
    /**
     * get STHolesBuckets that have nonempty intersection with a queryrecord
     * @param queryBox query feedback
     * @return buckets that intersect with queryRecord
     */
    private Iterable<STHolesOrigBucket<R>> getCandidateBuckets(R queryBox) {



        Collection<STHolesOrigBucket<R>> candidates = new LinkedList<STHolesOrigBucket<R>>();

        // check if there are bucket with boxes that intersect with the rectangle of the query


        candidates = getCandidateBucketsAux(root, candidates, queryBox);

        return candidates;
    }

    //Tested
    private Collection<STHolesOrigBucket<R>> getCandidateBucketsAux(
            STHolesOrigBucket<R> b, Collection<STHolesOrigBucket<R>> candidates,
            R queryBox)
    {

        R c = b.getBox();


        if (c.intersects(queryBox)) {

            candidates.add(b);
        }


        for (STHolesOrigBucket<R> bc : b.getChildren())
            getCandidateBucketsAux(bc,candidates,queryBox);

        return candidates;
    }


    /**
     * Count the tuples of the query result set that match the criteria of the given bucket.
     * @param rectangle rectangle
     * @param queryRecord query feedback
     * @return statistics
     */
    private long countMatchingTuples(R rectangle, QueryRecord<R,Long> queryRecord) {

        QueryResult<R,Long> qr = queryRecord.getResultSet();

        return qr.getCardinality(rectangle);
    }


    //Tested (default case)
    /**
     * Create a hole (i.e. a child STHolesOrigBucket) inside an existing bucket
     * @param parentBucket parent bucket
     * @param candidateHole candidate hole
     */
    private void drillHole(STHolesOrigBucket<R> parentBucket, STHolesOrigBucket<R> candidateHole)
    {

        if (parentBucket.getBox().equals(candidateHole.getBox())) {

            parentBucket.setFrequency(candidateHole.getFrequency());
        }
        else if (parentBucket.getVolume() == parentBucket.
                getIntersectionWithRecVolume(candidateHole.getBox())) {

            //if candidate hole covers all parentBucket's remaining space
            // merge parentBucket with its parent
            STHolesOrigBucket<R> bp = parentBucket.getParent();
            R newBox = bp.getBox();
            long newFreq = bp.getFrequency() + parentBucket.getFrequency();
            STHolesOrigBucket<R> parentN = bp.getParent();
            STHolesOrigBucket<R> bn = new STHolesOrigBucket<R>(newBox, newFreq, null, parentN);

            STHolesOrigBucket<R> mergedBucket = STHolesOrigBucket.getParentChildMerge(bp, parentBucket, bn, this);

            System.out.println("Parent child merge in drillHole!!!");

            drillHole(mergedBucket, candidateHole);
        }
        else {

            Collection<STHolesOrigBucket<R>> toBeRemoved = new ArrayList<STHolesOrigBucket<R>>();



            for (STHolesOrigBucket<R> bc : parentBucket.getChildren()) {

                if (candidateHole.getBox().contains(bc.getBox())){

                    candidateHole.addChild(bc);
                    toBeRemoved.add(bc);
                }
            }

            for (STHolesOrigBucket<R> bc : toBeRemoved) {

                parentBucket.removeChild(bc);
            }

            parentBucket.addChild(candidateHole);

            parentBucket.setFrequency(Math.max(0,parentBucket.getFrequency() -
                    candidateHole.getFrequency()));

            bucketsNum += 1;
        }
    }

    /**
     * merges superfluous buckets
     */
    private void compact() {

        // while too many buckets compute merge penalty for each parent-child
        // and sibling pair, find the one with the minimum penalty and
        // call merge(b1,b2,bn)
        while (bucketsNum > maxBucketsNum) {

            MergeInfo<R> bestMerge = findBestMerge(root);
            STHolesOrigBucket<R> b1 = bestMerge.getB1();
            STHolesOrigBucket<R> b2 = bestMerge.getB2();
            STHolesOrigBucket<R> bn = bestMerge.getBn();

            STHolesOrigBucket.merge(b1, b2, bn, this);
            System.out.println(bestMerge.toString());
            System.out.println("Number of PC merges:" + pcMergesNum);
            System.out.println("Number of SS merges: " + ssMergesNum);
            //bucketsNum -= 1;
        }
    }

    /**
     * identifies the merge with lowest penalty
     * and returns the buckets to be merged and
     * the resulting box
     * @param b bucket
     * @return best merge
     */
    private MergeInfo<R> findBestMerge(STHolesOrigBucket<R> b) {

        MergeInfo<R> bestMerge;
        MergeInfo<R> candidateMerge;
        double minimumPenalty = Integer.MAX_VALUE;
        double penalty;
        Map.Entry<STHolesOrigBucket<R>, Double> candidateMergedBucket;

        // Initialize buckets to be merged and resulting bucket
        STHolesOrigBucket<R> b1 = b;
        STHolesOrigBucket<R> b2 = b;
        STHolesOrigBucket<R> bn = b;

        Collection<STHolesOrigBucket<R>> bcs = b.getChildren();
        ArrayList<STHolesOrigBucket<R>> bChildren = new ArrayList<STHolesOrigBucket<R>>(bcs);

        STHolesOrigBucket<R> bi, bj;

        for (int i = 0; i < bChildren.size(); i++) {
            bi = bChildren.get(i);
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
            for (int j = i + 1; j < bChildren.size(); j++) {

                bj = bChildren.get(j);

                if (bi.getBox().isMergeable(bj.getBox())) {

                    candidateMergedBucket = getSSMergePenalty(bi, bj);
                    penalty = candidateMergedBucket.getValue();

                    if (penalty <= minimumPenalty) {

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

        for (STHolesOrigBucket<R> bc : b.getChildren()) {

            candidateMerge = findBestMerge(bc);

            if (candidateMerge.getPenalty() <= minimumPenalty) {

                bestMerge = candidateMerge;
            }
        }

        return bestMerge;
    }

    /**
     * computes the penalty of merging parent bucket {bp}
     * with child bucket {bp} and the resulting box
     * @param bp parent bucket
     * @param bc child bucket
     * @return pair of merge penalty and resulting box
     */
    private Map.Entry<STHolesOrigBucket<R>, Double>
    getPCMergePenalty(STHolesOrigBucket<R> bp, STHolesOrigBucket<R> bc) {

        if (!bc.getParent().equals(bp)) {
            //todo: throw exception
            return null;
        }
        R newBox = bp.getBox();
        long newFreq = bp.getFrequency() + bc.getFrequency();
        STHolesOrigBucket<R> newParent = bp.getParent();
        STHolesOrigBucket<R> bn = new STHolesOrigBucket<R>(newBox, newFreq, null, newParent);

        double penalty = (long)Math.ceil(Math.abs(bp.getFrequency() - bn.getFrequency() *
                ((double) bp.getVolume()) / bn.getVolume()));

        AbstractMap.SimpleEntry<STHolesOrigBucket<R>, Double> res =
                new AbstractMap.SimpleEntry<STHolesOrigBucket<R>, Double>(bn, penalty);

        return res;
    }

    /**
     * computes the penalty of merging siblings {b1} and {b2}
     * and the resulting box
     * @param b1 sibling 1
     * @param b2 sibling 2
     * @return pair of merge penalty and resulting box
     */
    private Map.Entry<STHolesOrigBucket<R>, Double>
    getSSMergePenalty(STHolesOrigBucket<R> b1, STHolesOrigBucket<R> b2) {

        if (!b1.getParent().equals(b2.getParent())) {
            //todo: throw exception
            return null;
        }
        R newBox = getSiblingSiblingBox(b1,b2);
        // the smallest box that encloses both b1 and b2 but does not
        // intersect partially with any other of bp


        // I contains bp's children which are enclosed by bn box
        Collection<STHolesOrigBucket<R>> I = new ArrayList<STHolesOrigBucket<R>>();
        STHolesOrigBucket<R> bp = b1.getParent();

        for (STHolesOrigBucket<R> bi : bp.getChildren() ) {

            if (!(bi.equals(b1) || bi.equals(b2))) {

                if (newBox.contains(bi.getBox())) {

                    I.add(bi);
                }
            }
        }

       long vold = newBox.getVolume() - b1.getBox().getVolume() -
               b2.getBox().getVolume();

        for (STHolesOrigBucket<R> bi : I) {

            vold -= bi.getBox().getVolume();
        }

        // Set statistics

        long newFrequency = 0;
        try {
             newFrequency = (long) Math.ceil(b1.getFrequency() + b2.getFrequency()
                    + bp.getFrequency() * ((double) vold)/bp.getVolume());

            if (bp.getVolume() == 0) {
                throw new ArithmeticException();
            }


            if (newFrequency > 20000000) {
                System.err.println("SSMerge:This should not happen. New frequency is: " +
                        newFrequency + " and fb1, fb2, fbp are: " + b1.getFrequency() + " " +
                        b2.getFrequency() + bp.getFrequency());
            }
        } catch (ArithmeticException ae) {
            System.out.println("ArithmeticException occured in getSSMergePenalty!");
        }

        //Add children
        Collection<STHolesOrigBucket<R>> newChildren = new ArrayList<STHolesOrigBucket<R>>();
        I.addAll(b1.getChildren());
        I.addAll(b2.getChildren());



        for (STHolesOrigBucket<R> bi : I) {

            newChildren.add(bi);
        }


        // Create bn
        STHolesOrigBucket<R> bn = new STHolesOrigBucket<R>(newBox, newFrequency, newChildren, null);

        double penalty;
        penalty = Math.abs( bn.getFrequency()* ((double) vold)/bn.getVolume() -
        bp.getFrequency()*((double) vold)/bp.getVolume())
                + Math.abs( b1.getFrequency() - bn.getFrequency()*
                        ((double) b1.getVolume())/bn.getVolume())
                + Math.abs(b2.getFrequency() - bn.getFrequency()*
                        ((double) b2.getVolume())/bn.getVolume());

        AbstractMap.SimpleEntry<STHolesOrigBucket<R>, Double> res =
                new AbstractMap.SimpleEntry<STHolesOrigBucket<R>, Double>(bn, penalty);

        return res;
    }


    public JSONObject toJSON() {
        return getRoot().toJSON();
    }


    public STHolesOrigBucket<R> getRoot() {
        return root;
    }


    public void setRoot(STHolesOrigBucket<R> root) {
        this.root = root;
    }

    public void setBucketsNum(long bucketsNum) {

        this.bucketsNum = bucketsNum;
    }

    public long getPcMergesNum() {
        return pcMergesNum;
    }

    public void setPcMergesNum(long pcMergesNum) {
        this.pcMergesNum = pcMergesNum;
    }

    public long getSsMergesNum() {
        return ssMergesNum;
    }

    public void setSsMergesNum(long ssMergesNum) {
        this.ssMergesNum = ssMergesNum;
    }
}
