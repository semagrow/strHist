package gr.demokritos.iit.irss.semagrow.stholes;

import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.api.STHistogram;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryResult;
import gr.demokritos.iit.irss.semagrow.base.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.simmetrics.metrics.JaroWinkler;

import java.util.*;

/**
 * Created by katerina on 24/11/2015.
 */
public class STHolesCircleHistogram<R extends Rectangle<R>> extends STHistogramBase<R,Stat> implements STHistogram<R,Stat> {

    static final Logger logger = LoggerFactory.getLogger(STHolesCircleHistogram.class);
    private STHolesBucket<R> root;
    public long maxBucketsNum = 50;
    public Double epsilon = 0.0;
    private long bucketsNum = 0;

    public long pcMergesNum = 0;
    public long ssMergesNum = 0;

    static public final int MAX_PENALTY_TYPE = 3;
    public int PENALTY_TYPE = 3;
    static boolean moreSpecific = true;
    private Map<STHolesBucket<R>, List<MergeInfo<R>>> memoMap;

    public STHolesCircleHistogram() {
        //todo: choose a constant
        root = null;
        memoMap = new HashMap<STHolesBucket<R>, List<MergeInfo<R>>>();
        bucketsNum += bucketsNum;
    }

    public STHolesCircleHistogram(STHolesBucket root) {
        this.root = root;
        memoMap = new HashMap<STHolesBucket<R>, List<MergeInfo<R>>>();
        bucketsNum++;
    }

    public STHolesCircleHistogram(Iterator<QueryRecord<R,Stat>> workload) {
        this();
        refine(workload);
    }

    /**
     * estimates the number of tuples
     * that match rectangle {rec}
     * @param rec rectangle
     * @return number of tuples
     */
    public long estimate(R rec) {
        if (root != null) {
            // if rec is larger than our root
            if (rec.isEnclosing(root.getBox())) {
                return root.getEstimate(rec);
            }
            else if (!root.getBox().isEnclosing(rec)) {
                return 0;
            }

            return estimateAux(rec, root);
        }
        else
            return 0;
    }

    /**
     * estimates the number of tuples contained in {rec}
     * by finding the enclosing bucket(s)
     * @param rec rectangle
     * @param b bucket
     * @return estimated number of tuples
     */
    private long estimateAux(R rec, STHolesBucket<R> b) {
        long est = 0;

        List<STHolesBucket<R>> enclosingBuckets = new ArrayList<STHolesBucket<R>>();

        getEnclosingBuckets(rec, b, enclosingBuckets);

        logger.info("Query Rectangle: " + rec.toString());
        logger.info("Number of enclosed buckets: " + enclosingBuckets.size());

        for (STHolesBucket<R> enclosingB : enclosingBuckets) {
            logger.info("Enclosed Bucket Rectangle: " + enclosingB.getBox().toString());
            logger.info("Enclosed Bucket Statistics: " + enclosingB.getStatistics().toString());
            long bucketEstimation = enclosingB.getEstimate(rec);
            est += bucketEstimation;
        }

        logger.info("Estimated triples: " + est);
        return est;
    }

    private void getEnclosingBuckets(R rec, STHolesBucket<R> b, List<STHolesBucket<R>> enclosingBuckets) {
        boolean isEnclosingBucket = false;

        if ((b.getBox()).isEnclosing(rec)) { //unnecessary
            isEnclosingBucket = true;
            for (STHolesBucket<R> bc : b.getChildren()) {
                if ((bc.getBox()).isEnclosing(rec)) {
                    isEnclosingBucket = false;
                    getEnclosingBuckets(rec, bc, enclosingBuckets);
                }
            }
        }

        if (isEnclosingBucket)
            enclosingBuckets.add(b);
    }

    /**
     * refines histogram using query feedback
     * @param queryRecord query feedback
     */
    public void refine(QueryRecord<R,Stat> queryRecord) {
        List<R> rects = new ArrayList<R>();
        boolean flag = false;

        if (queryRecord.getRectangle().isInfinite())
            rects.addAll(queryRecord.getResultSet().getRectangles());
        else
            rects.add(queryRecord.getRectangle());

        for (R rect : rects) {
            flag = false;
            R origRect = rect;



            if (this.getRoot() == null) {
                setRoot(new STHolesBucket<R>(rect, new Stat(), null, null));
                bucketsNum += 1;
                logger.info("Root bucket is created");
            } else {
                if (!root.getBox().contains(rect)) {
                    // Expand root box so that it includes q
                    R boxN = root.getBox().computeTightBox(rect);

                    Stat statsN = countMatchingTuples(rect, queryRecord);
                    Stat rootStatsN = computeRootStats(root.getStatistics(), statsN);

                    root.setBox(boxN);
                    root.setStatistics(rootStatsN);

                    logger.info("Root bucket is expanded");
                }
            }

            moreSpecific = true;
            Iterable<STHolesBucket<R>> candidates = getCandidateBuckets(rect);

            if (candidates == null) {
                logger.info("A query that already has its own bucket... no reason for refinement");
                flag = true;
                continue;
            }


            for (STHolesBucket<R> bucket : candidates) {
                STHolesBucket<R> hole = null;

                hole = shrink(bucket, rect, queryRecord);

                // If bucket is equals to hole, just update the stats.
                if (bucket.getBox().equals(hole.getBox())) {
                    bucket.setStatistics(new Stat(hole.getStatistics()));
                    logger.info("Bucket Update: " + bucket.getBox().toString());
                    logger.info("Updated Stats: " + bucket.getStatistics().toString());
                    continue;
                }


                if (!hole.getBox().isEmpty() && isInaccurateEstimation(bucket, hole)) {
                    logger.info("Drilling");
                    logger.info("Bucket: " + bucket.getBox().toString() + " with stats " + bucket.getStatistics().toString());
                    logger.info("Hole " + hole.getBox().toString() + " with stats " + hole.getStatistics().toString());
                    drillHole(bucket, hole);
                } else {
                    logger.info("Skip drilling");
                    logger.info("Bucket: " + bucket.getBox().toString() + " with stats " + bucket.getStatistics().toString());
                    logger.info("Skipped Hole " + hole.getBox().toString() + " with stats " + hole.getStatistics().toString());
                }
            }
        }
        logger.info("Histogram refined with query: " + queryRecord.getRectangle());

        // Check if histogram must be compacted after refinement
        if(! flag)
            compact();
        logger.info("-------------------------------------------------------------------");
    }

    /////////////////////////////

    protected R getRectangle(R r) { return r; }

    protected String getSubject(R r) { return r.getRange(0).toString(); };

    protected void setSubjLength(R r, long count) {  };

    protected long getSubjLength(R r) {  return 0; };

    protected double getRadius(R r) { return 0; }

    ////////////////////////////////////

    private Stat computeRootStats(Stat oldStats, Stat deltaStats) {
        long freqN = deltaStats.getFrequency() + oldStats.getFrequency();

        List<Long> distinctN = new ArrayList<Long>();

        for (int i = 0; i < deltaStats.getDistinctCount().size(); i++) {
            distinctN.add(Math.max(deltaStats.getDistinctCount().get(i), oldStats.getDistinctCount().get(i)));
        }

        return new Stat(freqN, distinctN);
    }

    private boolean isInaccurateEstimation(STHolesBucket<R> bucket, STHolesBucket<R> hole) {
//        int epsilon = 0; //todo: adjust parameter
        Stat actualStatistics = hole.getStatistics();
        Double actualDensity = actualStatistics.getDensity();

        Stat curStatistics = bucket.getStatistics();
        Double curDensity = curStatistics.getDensity();

        logger.info("Estimation for drilling: actual = " + actualDensity + " current = " + curDensity);


        return (Math.abs(actualDensity - curDensity) > epsilon);
    }

    /**
     * creates a new bucket that has a rectangle that does not intersect with the children of {bucket}
     * and includes the number of tuples that matches the queryRecord
     * @param bucket parent bucket
     * @param queryRecord query feedback
     * @return shrinked bucket
     */
    private STHolesBucket<R> shrink(STHolesBucket<R> bucket, R rect, QueryRecord<R,Stat> queryRecord) {
        // Find candidate hole
        R c = bucket.getBox().intersection(rect);

        // Shrink candidate hole in such a way that b does not intersect
        // with the rectangles of bucket.getChildren();
        List<STHolesBucket<R>> participants = new LinkedList<STHolesBucket<R>>();

        updateParticipants(participants, bucket, c);

        while (!participants.isEmpty()) {
            c.shrink(participants.get(0).getBox());
            updateParticipants(participants, bucket, c);
        }

        //TODO: create a new rectangle / this is not the way to do it!
        //todo: is this still a todo?

        // Collect candidate hole statistics
        Stat stats = countMatchingTuples(c, queryRecord);

        return new STHolesBucket<R>(c, stats, null, null);
    }

    /**
     * finds {bucket}'s children that partially intersect
     * with candidate hole c and stores them
     * in {participants} list
     * @param participants list of participants
     * @param bucket parent bucket
     * @param c candidate hole
     */
    private void  updateParticipants(List<STHolesBucket<R>> participants, STHolesBucket<R> bucket, R c) {
        List<STHolesBucket<R>> participantsNew = new LinkedList<STHolesBucket<R>>();

        for (STHolesBucket<R> bi : bucket.getChildren()) {
            if ((c.intersects(bi.getBox())) && (!c.contains(bi.getBox()))) {
                participantsNew.add(bi);
            }
        }

        participants.retainAll(participantsNew);
    }

    /**
     * finds the smallest box that encloses both {b1} and {b2} and
     * does not intersect partially with any other child of their parent.
     * The box is expanded until all partial intersections are fully enclosed
     * @param b1 sibling 1
     * @param b2 sibling 2
     * @return box after merge
     */
    private R getSiblingSiblingBox(STHolesBucket<R> b1, STHolesBucket<R> b2) {
        // Get parent
        STHolesBucket<R> bp = b1.getParent(); //todo: check if they are siblings
        // Find tightly enclosing box
        R c = b1.getBox().computeTightBox(b2.getBox());

        // Expand tightly enclosing box
        List<STHolesBucket<R>> participants = new LinkedList<STHolesBucket<R>>();

        updateParticipants(participants, bp, c);

        for (STHolesBucket<R> participant : participants) {
            c = c.computeTightBox(participant.getBox());
            updateParticipants(participants, bp, c);
            if (participants.isEmpty()) {
                break;
            }
        }

        return c;
    }


    /**
     * Get STHolesBuckets with the most specific nonempty intersection with a QueryRecord.
     * @param queryBox query feedback rectangle
     * @return buckets that intersect with queryRecord
     */
    private Iterable<STHolesBucket<R>> getCandidateBuckets(R queryBox) {
        return getCandidateBuckets(root, queryBox);
    }

    private Collection<STHolesBucket<R>> getCandidateBuckets(STHolesBucket<R> bucket, R queryBox) {
        Collection<STHolesBucket<R>> candidates = new LinkedList<STHolesBucket<R>>();
        Collection<STHolesBucket<R>> chCand;

        String subj1 = getSubject(queryBox);
        JaroWinkler jw = new JaroWinkler();

        for (STHolesBucket<R> bucketChild : bucket.getChildren()) {
            String subj2 = getSubject(bucketChild.getBox());

            if (jw.distance(subj1, subj2) == 1.0)
                return null;

            chCand = getCandidateBuckets(bucketChild, queryBox);
            if (chCand != null)
                candidates.addAll(chCand);
            else
                return null;
        }


        if (bucket.getBox().contains(queryBox)) {
            //boolean moreSpecific = true;

            // Add a candidate only if there isn't already any more specific candidate.
            for (STHolesBucket<R> candidate : candidates)
                if (candidate.getBox().isEnclosing(queryBox)) {
                    moreSpecific = false;
                }

            if (moreSpecific)
                candidates.add(bucket);
        }

        return candidates;
    }

    /**
     * Count the tuples of the query result set that match the criteria of the given bucket.
     * @param rectangle rectangle
     * @param queryRecord query feedback
     * @return statistics
     */
    private Stat countMatchingTuples(R rectangle, QueryRecord<R,Stat> queryRecord) {
        QueryResult<R,Stat> qr = queryRecord.getResultSet();
        return qr.getCardinality(rectangle);
    }

    /**
     * Create a hole inside an existing bucket.
     * @param bucket parent bucket
     * @param hole candidate hole
     */
    private void drillHole(STHolesBucket<R> bucket, STHolesBucket<R> hole) {
        List<STHolesBucket> toRemove = new ArrayList<>();

        for (STHolesBucket<R> bucketChild : bucket.getChildren())
            if (hole.getBox().contains(bucketChild.getBox()))
                toRemove.add(bucketChild);

        for (STHolesBucket r : toRemove)
            bucket.removeChild(r);

        bucket.addChild(hole);
        bucketsNum++;
    }

    /**
     * merges superfluous buckets
     */
    private void compact() {
        if (bucketsNum > maxBucketsNum)
            logger.info("Compacting histogram.");

        // while too many buckets compute merge penalty for each parent-child
        // and sibling pair, find the one with the minimum penalty and
        // call merge(b1,b2,bn)
        while (bucketsNum > maxBucketsNum) {

            MergeInfo<R> bestMerge = findBestMerge1(root);
            STHolesBucket<R> b1 = bestMerge.getB1();
            STHolesBucket<R> b2 = bestMerge.getB2();
            STHolesBucket<R> bn = bestMerge.getBn();

            STHolesBucket.merge(b1, b2, bn, this);
            logger.info("Best merge info: " + bestMerge.toString() + " *bn =* "+bestMerge.getBn().toString() + " *b1 = *"+bestMerge.getB1().toString());
            logger.info("Number of PC merges: " + pcMergesNum);
            logger.info("Number of SS merges: " + ssMergesNum);
            bucketsNum -= 1;
        }
    }

    private void constructMemoMap(STHolesBucket<R> b) {

        Map.Entry<STHolesBucket<R>, Double> candidateMergedBucket;
        double penalty;
        STHolesBucket<R> bi, bj;


        Collection<STHolesBucket<R>> bcs = b.getChildren();
        ArrayList<STHolesBucket<R>> bChildren = new ArrayList<STHolesBucket<R>>(bcs);

        for (int i = 0; i < bChildren.size(); i++) {
            bi = bChildren.get(i);

            logger.info("Construct memo map .... ");
            for (int j = i + 1; j < bChildren.size(); j++) {

                bj = bChildren.get(j);

                if (bi.getBox().isMergeable(bj.getBox())) {
                    candidateMergedBucket = getSSMergePenalty(bi, bj);

                    if (candidateMergedBucket == null)
                        continue;

                    penalty = candidateMergedBucket.getValue();
                    MergeInfo<R> merge = new MergeInfo<>(bi, bj, candidateMergedBucket.getKey(), penalty);

                    if (this.memoMap.isEmpty()) {

                        addMemoBuckets(merge);
                        logger.info("Add memo map with penalty =  " + penalty + ", bi = " + bi.toString() + " bj= " + bj.toString() + " bn = ");
                    }
                    else {
                        updateMemoBuckets(merge);
                        logger.info("Update memo map with penalty =  " + penalty + ", bi = " + bi.toString() + " bj= " + bj.toString() + " bn = ");
                    }

                }


            }
        }
    }

    /**
     * add the sibling buckets and its penalty to a map (graph representation) in the memory.
     *@param merge
     */
    private void addMemoBuckets(MergeInfo<R> merge) {
        /*Map<STHolesBucket<R>, Double> tempMap1 = new HashMap<>();
        tempMap1.put(bj, penalty);
        memo.put(bi, tempMap1);

        Map<STHolesBucket<R>, Double> tempMap2 = new HashMap<>();
        tempMap2.put(bi, penalty);
        memo.put(bj, tempMap2);*/

        List<MergeInfo<R>> list1 = new ArrayList();
        list1.add(merge);

        if (merge.getB1() == null || merge.getB2() == null) {
            return;
        }

        this.memoMap.put(merge.getB1(), list1);

        List<MergeInfo<R>> list2 = new ArrayList();
        list2.add(merge);
        this.memoMap.put(merge.getB2(), list2);
    }

    private void updateMemoBuckets(MergeInfo<R> merge) {

        if (memoMap.containsKey(merge.getB1())) {
            List<MergeInfo<R>> tempList = memoMap.get(merge.getB1());
            tempList.add(merge);
        }
        else {
            List<MergeInfo<R>> list = new ArrayList();
            list.add(merge);

            this.memoMap.put(merge.getB2(), list);
        }

        if (memoMap.containsKey(merge.getB2())) {
            List<MergeInfo<R>> tempList = memoMap.get(merge.getB2());
            tempList.add(merge);
        }
        else {
            List<MergeInfo<R>> list = new ArrayList();
            list.add(merge);

            this.memoMap.put(merge.getB1(), list);
        }



        /*
        boolean flag1 = false, flag2 = false;

        Iterator it = memoMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            if (pair.getKey().equals((STHolesBucket<R>) merge.getB1())) {
                List<MergeInfo<R>> tempList = (List) pair.getValue();

                tempList.add(merge);
                if (flag1)
                    break;
                else
                    flag1 = true;
            }
            if (pair.getKey().equals((STHolesBucket<R>) merge.getB2())) {
                List<MergeInfo<R>> tempList = (List) pair.getValue();

                tempList.add(merge);
                if (flag2)
                    break;
                else
                    flag2 = true;
            }

            if (flag1 && flag2)
                break;
        }

        // the key bucket doesnt exist -> add a new one in the memoMap
        if (!flag1) {
            List<MergeInfo<R>> list = new ArrayList();
            list.add(merge);

            this.memoMap.put(merge.getB1(), list);
        }

        if (!flag2) {
            List<MergeInfo<R>> list = new ArrayList();
            list.add(merge);

            this.memoMap.put(merge.getB2(), list);
        }
        */


    }

    /**
     * update the memoMap after a merge.
     * @param merge the merge bucket
     */
    private void mergeMemoMap(MergeInfo<R> merge) {

        STHolesBucket<R> b1 = merge.getB1();
        STHolesBucket<R> b2 = merge.getB2();
        STHolesBucket<R> bn = merge.getBn();

        List<MergeInfo<R>> newList = new ArrayList<>();

        /* get the lists of the merged buckets , in order to find which key-buckets need update because of the merge */
        List<MergeInfo<R>> list = new LinkedList<>(memoMap.get(b1));
        list.addAll(memoMap.get(b2));


        for (MergeInfo<R> mergeInfo : list) {

            STHolesBucket<R> refB = mergeInfo.getB1().equals(b1) ? mergeInfo.getB2() : mergeInfo.getB1();

            // in case that there is an mergeinfo between (b1, b2).
            if (refB.equals(b1) || refB.equals(b2))
                continue;

            // compute new merge info between the merged bucket bn and the influenced bucket refB.

            Map.Entry<STHolesBucket<R>, Double> candidate = getSSMergePenalty(PENALTY_TYPE, bn, refB);

            MergeInfo<R> newMergeInfo = new MergeInfo<>(bn, refB, (STHolesBucket<R>) candidate.getKey(), (double) candidate.getValue());

            newList.add(newMergeInfo);

            memoMap.get(refB).removeAll(list);
            memoMap.get(refB).add(newMergeInfo);
        }

        memoMap.put(bn, newList);

        memoMap.remove(b1);
        memoMap.remove(b2);


        /*
        //only for logs!!!!
        it = memoMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            logger.info(" key = "+pair.getKey().toString());

            List<MergeInfo<R>> l = (List<MergeInfo<R>>) pair.getValue();

            for (int i = 0; i< l.size(); i++)
                logger.info(" value "+i+" = " + l.get(i).toString());
        }
        */
    }

    /**
     * identifies the merge with lowest penalty
     * and returns the buckets to be merged and
     * the resulting box
     * @param b bucket
     * @return best merge
     */
    private MergeInfo<R> findBestMerge(STHolesBucket<R> b) {
        MergeInfo<R> bestMerge;
        MergeInfo<R> candidateMerge;
        double minimumPenalty = Integer.MAX_VALUE;
        double penalty;
        Map.Entry<STHolesBucket<R>, Double> candidateMergedBucket;

        // Initialize buckets to be merged and resulting bucket
        STHolesBucket<R> b1 = b;
        STHolesBucket<R> b2 = b;
        STHolesBucket<R> bn = b;

        Collection<STHolesBucket<R>> bcs = b.getChildren();
        ArrayList<STHolesBucket<R>> bChildren = new ArrayList<STHolesBucket<R>>(bcs);

        STHolesBucket<R> bi, bj;

        logger.info("Parent-child merge .... ");
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
                logger.info("PC: penalty = "+penalty+" b1 = "+b1.toString() + " b2= "+b2.toString()+ " bn = "+bn.toString());
            }


            // SS merges are commented out because of time complexity.
            // Candidate sibling-sibling merges
            logger.info("Sibling merging .... ");
            for (int j = i + 1; j < bChildren.size(); j++) {

                bj = bChildren.get(j);

                if (bi.getBox().isMergeable(bj.getBox())) {
                    logger.info(bi.toString()+" is mergeable with " + bj.toString());
                    candidateMergedBucket = getSSMergePenalty(bi, bj);
                    penalty = candidateMergedBucket.getValue();

                    if (penalty <= minimumPenalty) {
                        minimumPenalty = penalty;
                        b1 = bi;
                        b2 = bj;
                        bn = candidateMergedBucket.getKey();
                        logger.info("SS: penalty = " + penalty + " b1 = " + b1.toString() + " b2= " + b2.toString() + " bn = " + bn.toString());
                    }
                }

            }
        }

        // local best merge
        bestMerge = new MergeInfo<R>(b1, b2, bn, minimumPenalty);
        logger.info("Best Merge = \n" + bestMerge.toString());

        for (STHolesBucket<R> bc : b.getChildren()) {
            candidateMerge = findBestMerge(bc);

            if (candidateMerge.getPenalty() <= minimumPenalty) {
                bestMerge = candidateMerge;
            }
        }

        return bestMerge;
    }


    private MergeInfo<R> findBestMerge1(STHolesBucket<R> b) {
        List<MergeInfo<R>> SSrankList = new ArrayList<>();
        MergeInfo<R> bestMerge;

        MergeInfo<R> bestParent = findBestParentMerge(b, Double.MAX_VALUE);

        findBestSiblingMerge1(SSrankList, bestParent.getPenalty(), b);


        if (SSrankList.isEmpty()) {
            return bestParent;
        }
        else {
            updateRankMerge(SSrankList);
            int i = 0;
            for (i = 0; i < SSrankList.size(); i++) {

                if (isMerging(SSrankList.get(i)))
                    break;
            }

            if (i >= SSrankList.size()) {
                return bestParent;
            }
            else {
                MergeInfo<R> merge = SSrankList.get(i);
                bestMerge = new MergeInfo<R>(merge.getB1(), merge.getB2(), merge.getBn(), merge.getPenalty());

                return bestMerge;
            }
        }

    }

    private void findBestSiblingMerge1(List<MergeInfo<R>> SSrankList, double minimumPCPenalty, STHolesBucket<R> b) {
        Map.Entry<STHolesBucket<R>, Double> candidateMergedBucket;
        double penalty;
        STHolesBucket<R> bi, bj;

        // Initialize buckets to be merged and resulting bucket
        STHolesBucket<R> b1 = b;
        STHolesBucket<R> b2 = b;
        STHolesBucket<R> bn = b;

        Collection<STHolesBucket<R>> bcs = b.getChildren();
        ArrayList<STHolesBucket<R>> bChildren = new ArrayList<STHolesBucket<R>>(bcs);

        for (int i = 0; i < bChildren.size(); i++) {
            bi = bChildren.get(i);
            // Candidate sibling-sibling merges
            //logger.info("Sibling merging .... ");
            for (int j = i + 1; j < bChildren.size(); j++) {

                bj = bChildren.get(j);

                if (bi.getBox().isMergeable(bj.getBox())) {
                    candidateMergedBucket = getSSMergePenalty(bi, bj);

                    if (candidateMergedBucket == null)
                        continue;

                    penalty = candidateMergedBucket.getValue();

                    if (penalty <= minimumPCPenalty) {
                        b1 = bi;
                        b2 = bj;
                        bn = candidateMergedBucket.getKey();
                        logger.info("SS: penalty = " + penalty + " b1 = " + b1.toString() + " b2= " + b2.toString() + " bn = " + bn.toString());

                        SSrankList.add(new MergeInfo<R>(b1, b2, bn, penalty));
                    }
                }
                findBestSiblingMerge1(SSrankList, minimumPCPenalty, bj);

            }
        }
    }


    private MergeInfo<R> findBestMergeN(STHolesBucket<R> b) {
        List<MergeInfo<R>> SSrankList = new ArrayList<>();
        MergeInfo<R> bestMerge;

        MergeInfo<R> bestParent = findBestParentMerge(b, Double.MAX_VALUE);

        findBestSiblingMerge(SSrankList, bestParent.getPenalty(), b);


        if (SSrankList.isEmpty()) {
            return bestParent;
        }
        else {
            updateRankMerge(SSrankList);

            int i = 0;
            for (i = 0; i < SSrankList.size(); i++) {

                if (isMerging(SSrankList.get(i)))
                    break;
            }

            if (i >= SSrankList.size()) {
                return bestParent;
            }
            else {
                MergeInfo<R> merge = SSrankList.get(i);
                bestMerge = new MergeInfo<R>(merge.getB1(), merge.getB2(), merge.getBn(), merge.getPenalty());

                mergeMemoMap(bestMerge);
                return bestMerge;
            }
        }

    }

    private MergeInfo<R> findBestParentMerge(STHolesBucket<R> b, double minimumPCPenalty) {
        //double minimumPCPenalty = Integer.MAX_VALUE;
        double penalty;
        MergeInfo<R> best = new MergeInfo<R>(b, b, b, Double.MAX_VALUE);
        Map.Entry<STHolesBucket<R>, Double> candidateMergedBucket;

        Collection<STHolesBucket<R>> bcs = b.getChildren();
        ArrayList<STHolesBucket<R>> bChildren = new ArrayList<STHolesBucket<R>>(bcs);

        STHolesBucket<R> bi;

        // Initialize buckets to be merged and resulting bucket
        STHolesBucket<R> b1 = b;
        STHolesBucket<R> b2 = b;
        STHolesBucket<R> bn = b;

        logger.info("Parent-child merge .... ");
        for (int i = 0; i < bChildren.size(); i++) {
            bi = bChildren.get(i);
            // Candidate parent-child merges
            candidateMergedBucket = getPCMergePenalty(b, bi);
            if (candidateMergedBucket == null)
                continue;

            penalty = candidateMergedBucket.getValue();

            if (penalty <= minimumPCPenalty) {

                minimumPCPenalty = penalty;
                b1 = b;
                b2 = bi;
                bn = candidateMergedBucket.getKey();

                //set the best parent merge
                best.setB1(b1);
                best.setB2(b2);
                best.setBn(bn);
                best.setPenalty(penalty);
                logger.info("PC: penalty = " + penalty + " b1 = " + b1.toString() + " b2= " + b2.toString() + " bn = " + bn.toString());
            }
            if (! bi.getChildren().isEmpty()) {
                MergeInfo<R> temp = findBestParentMerge(bi, minimumPCPenalty);
                if (temp.getPenalty() <= best.getPenalty())
                    best = temp;
            }
        }

        return best;
    }

    private void findBestSiblingMerge(List<MergeInfo<R>> SSrankList, double minimumPCPenalty, STHolesBucket<R> b) {

        if (memoMap.isEmpty()) {
            constructMemoMap(b);
        }

        Iterator it = memoMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            List<MergeInfo<R>> tempList = (List<MergeInfo<R>>) pair.getValue();

            for (MergeInfo merge : tempList) {

                if (SSrankList.contains(merge))
                    continue;

                if (merge.getPenalty() <= minimumPCPenalty) {
                    SSrankList.add(merge);

                }
            }

            STHolesBucket<R> bucket = (STHolesBucket<R>) pair.getKey();
            if (! bucket.getChildren().isEmpty()) {

                findBestSiblingMerge(SSrankList, minimumPCPenalty, bucket);
            }
        }

    }

    /**
     * identifies the merges with lowest SS penalties
     * and returns a list of the buckets to be merged and
     * the resulting box
     * @param b bucket
     * @return best merge
     */
    private MergeInfo<R> findRankMerge(STHolesBucket<R> b) {
        MergeInfo<R> bestMerge;
        MergeInfo<R> candidateMerge;
        double minimumPCPenalty = Integer.MAX_VALUE;
        double penalty;
        Map.Entry<STHolesBucket<R>, Double> candidateMergedBucket;
        List<MergeInfo<R>> SSrankList = new ArrayList<>();

        // Initialize buckets to be merged and resulting bucket
        STHolesBucket<R> b1 = b;
        STHolesBucket<R> b2 = b;
        STHolesBucket<R> bn = b;

        Collection<STHolesBucket<R>> bcs = b.getChildren();
        ArrayList<STHolesBucket<R>> bChildren = new ArrayList<STHolesBucket<R>>(bcs);

        STHolesBucket<R> bi, bj;

        logger.info("Parent-child merge .... ");
        for (int i = 0; i < bChildren.size(); i++) {
            bi = bChildren.get(i);
            // Candidate parent-child merges
            candidateMergedBucket = getPCMergePenalty(b, bi);
            penalty = candidateMergedBucket.getValue();

            if (penalty <= minimumPCPenalty) {

                minimumPCPenalty = penalty;
                b1 = b;
                b2 = bi;
                bn = candidateMergedBucket.getKey();
                logger.info("PC: penalty = " + penalty + " b1 = " + b1.toString() + " b2= " + b2.toString() + " bn = " + bn.toString());
            }
        }

        for (int i = 0; i < bChildren.size(); i++) {
            bi = bChildren.get(i);
            // Candidate sibling-sibling merges
            logger.info("Sibling merging .... ");
            for (int j = i + 1; j < bChildren.size(); j++) {

                bj = bChildren.get(j);

                if (bi.getBox().isMergeable(bj.getBox())) {
                    logger.info(bi.toString()+" is mergeable with " + bj.toString());
                    candidateMergedBucket = getSSMergePenalty(bi, bj);
                    penalty = candidateMergedBucket.getValue();

                    if (penalty <= minimumPCPenalty) {
                        b1 = bi;
                        b2 = bj;
                        bn = candidateMergedBucket.getKey();
                        logger.info("SS: penalty = "+penalty+" b1 = "+b1.toString() + " b2= "+b2.toString()+ " bn = "+bn.toString());

                        SSrankList.add(new MergeInfo<R>(b1, b2, bn, penalty));
                    }


                }

            }
        }

        if (SSrankList.isEmpty()) {
            bestMerge = new MergeInfo<R>(b1, b2, bn, minimumPCPenalty);
        }
        else {
            updateRankMerge(SSrankList);
            int i = 0;
            for (i = 0; i < SSrankList.size(); i++) {

                if (isMerging(SSrankList.get(i)))
                    break;
                else {

                }
            }

            if (i >= SSrankList.size()) {
                bestMerge = new MergeInfo<R>(b1, b2, bn, minimumPCPenalty);
            }
            else {
                MergeInfo<R> merge = SSrankList.get(i);
                bestMerge = new MergeInfo<R>(merge.getB1(), merge.getB2(), merge.getBn(), merge.getPenalty());
            }
        }
        logger.info("Best Merge = \n"+bestMerge.toString());

        /*for (STHolesBucket<R> bc : b.getChildren()) {
            candidateMerge = findBestMerge(bc);

            if (candidateMerge.getPenalty() <= minimumPCPenalty) {
                bestMerge = candidateMerge;
            }
        }*/

        return bestMerge;
    }

    /**
     * checks the constraints and indicates whether two buckets can be merged.
     *
     * @param mergeBox the box that contains the 2 merging buckets and the resulting one
     * @return
     */
    private boolean isMerging(MergeInfo<R> mergeBox) {
        STHolesBucket<R> b1 = mergeBox.getB1();
        STHolesBucket<R> b2 = mergeBox.getB2();
        STHolesBucket<R> bn = mergeBox.getBn();

        Collection<STHolesBucket<R>> I = new ArrayList<STHolesBucket<R>>();
        STHolesBucket<R> bp = b1.getParent();

        double currentRadius = getRadius(bn.getBox());
        double parentRadius = getRadius(bp.getBox());

        if (2 * currentRadius > parentRadius) {
            logger.info("Cannot merge: Very large RDFCircle");
            return false;
        }

        for (STHolesBucket<R> bi : bp.getChildren() ) {

            if (!(bi.equals(b1) || bi.equals(b2))) {

                if (bn.getBox().intersects(bi.getBox())) {

                    if (bn.getBox().contains(bi.getBox())) {
                        logger.info("HOLE?: bn = "+ bn.toString() + "  contains  bi = "+bi.toString());
                        return constructMergingBucket(bn, bi);
                    }
                    if (bi.getBox().contains(bn.getBox())) {
                        logger.info("HOLE?: bi = "+ bi.toString() + "  contains  bn = "+bn.toString());
                        return constructMergingBucket(bi, bn);
                    }

                    logger.info("Cannot merge: bn = " + bn.toString() + " intersects with bi = "+bi.toString());
                    return false;

                }
            }
        }

        return true;
    }

    /**
     * Rearrangement of the suitable buckets after merging.
     * If there is inaccurate estimation in the potential buckets, there is a need of parent child arrangement in the histogram.
     *
     * @param bp the potential parent
     * @param bc the potential child
     */
    private boolean constructMergingBucket(STHolesBucket<R> bp, STHolesBucket<R> bc) {

        if (isInaccurateEstimation(bp, bc)) {
            STHolesBucket<R> hole = bc;

            STHolesBucket<R> par = bc.getParent();

            if(par != null)
                par.removeChild(bc);
            else {
                logger.info("there is no parent ... ");
                return false;
            }

            List<STHolesBucket> toRemove = new ArrayList<>();

            for (STHolesBucket<R> bucketChild : bp.getChildren())
                if (hole.getBox().contains(bucketChild.getBox()))
                    toRemove.add(bucketChild);

            for (STHolesBucket r : toRemove)
                bp.removeChild(r);

            bp.addChild(hole);

            logger.info("Child "+hole.toString()+" is added on "+bp.toString());


        } else {
            setSubjLength(bp.getBox(), getSubjLength(bc.getBox()));
            logger.info("augment the points of bucket : " + bp.toString());
        }

        return true;

    }

    /**
     * updates the penalties of the ranked list, by adding a penalty based on the similarity of their boxes.
     * Sorts the list by penalty in an ascending order.
     *
     * @param SSrankList
     * @return the updated, sorted SSrankList
     */
    private List<MergeInfo<R>> updateRankMerge(List<MergeInfo<R>> SSrankList ) {

        for(int i = 0; i < SSrankList.size(); i++) {
            SSrankList.set(i, updateSSPenalty(SSrankList.get(i)));
        }

        Collections.sort(SSrankList, new Comparator<MergeInfo>() {
            @Override
            public int compare(MergeInfo o1, MergeInfo o2) {
                return Double.compare(o1.getPenalty(), o2.getPenalty());
            }

        });

        return SSrankList;
    }

    private MergeInfo<R> updateSSPenalty(MergeInfo<R> mergeBox) {
        JaroWinkler jw = new JaroWinkler();

        String subj1 = getSubject(mergeBox.getB1().getBox());
        String subj2 = getSubject(mergeBox.getB2().getBox());

        mergeBox.setPenalty((1.0 - jw.distance(subj1, subj2)) + mergeBox.getPenalty());

        return mergeBox;
    }

    /**
     * computes the penalty of merging parent bucket {bp}
     * with child bucket {bp} and the resulting box
     * @param bp parent bucket
     * @param bc child bucket
     * @return pair of merge penalty and resulting box
     */
    private Map.Entry<STHolesBucket<R>, Double> getPCMergePenalty(STHolesBucket<R> bp, STHolesBucket<R> bc) {
        return getPCMergePenalty(PENALTY_TYPE, bp, bc);
    }

    /**
     * computes the penalty of merging parent bucket {bp}
     * with child bucket {bp} and the resulting box
     * @param type penalty formula (0,1,2)
     * @param bp parent bucket
     * @param bc child bucket
     * @return pair of merge penalty and resulting box
     */
    private Map.Entry<STHolesBucket<R>, Double> getPCMergePenalty(int type, STHolesBucket<R> bp, STHolesBucket<R> bc) {
        double penalty;
        double dd, dd2, bn_size, bc_size;
        int dim;

        if (!bc.getParent().equals(bp)) {
            //todo: throw exception
            return null;
        }
        R newBox = bp.getBox();
        long newFreq = bp.getStatistics().getFrequency();
        List<Long> newDistinct = bp.getStatistics().getDistinctCount();

        STHolesBucket<R> newParent = bp.getParent();
        Stat newStatistics = new Stat(newFreq, newDistinct);

        STHolesBucket<R> bn = new STHolesBucket<R>(newBox, newStatistics, null, newParent);

        switch( type ) {
            case 0:
                penalty = Math.abs(estimate(bc.getBox()) - estimate(bp.getBox()));
                break;
            case 1:
                dd = Math.abs( bc.getStatistics().getDensity() - bp.getStatistics().getDensity() );
                penalty = dd;
                logger.info("PC: bc density = "+bc.getStatistics().getDensity()+" bp density = "+ bp.getStatistics().getDensity());
                break;
            case 2:
                dim = bp.getStatistics().getDistinctCount().size();
                penalty = 0.0;
                bn_size = bn.getStatistics().getFrequency();
                bc_size = bc.getStatistics().getFrequency();
                for( int i=0; i<dim; ++i ) {
                    dd = bn_size / (double)bn.getStatistics().getDistinctCount().get(i);
                    dd2 = bc_size / (double)bc.getStatistics().getDistinctCount().get(i);
                    penalty += Math.abs( dd2 - dd );
                }
                logger.info("Case 2: penalty = " + penalty);
                break;
            case 3:
                dim = bp.getStatistics().getDistinctCount().size();
                if(bp.getStatistics().getFrequency() == 0 || bc.getStatistics().getFrequency() == 0) {
                    dd = 1.0;
                } else {
                    dd = (double) ((double) (Math.abs( bp.getStatistics().getDensity() - bn.getStatistics().getDensity() )) /  ( bp.getStatistics().getDensity() + bn.getStatistics().getDensity())+
                            (double) Math.abs( bc.getStatistics().getDensity() - bn.getStatistics().getDensity() ) / (  bc.getStatistics().getDensity() + bn.getStatistics().getDensity() )) / 2;

                }

                double dd3 = 0.0;

                for( int i=0; i<dim; ++i ) {

                    dd3 = (Math.abs(( (double)bn.getStatistics().getDistinctCount().get(i) - (double)bp.getStatistics().getDistinctCount().get(i)) )
                            / ( (double)bn.getStatistics().getDistinctCount().get(i) + (double)bp.getStatistics().getDistinctCount().get(i) ));

                    dd = dd +dd3;

                    dd3 = (Math.abs(( (double)bn.getStatistics().getDistinctCount().get(i) - (double)bc.getStatistics().getDistinctCount().get(i)) )
                            / ( (double)bn.getStatistics().getDistinctCount().get(i) + (double)bc.getStatistics().getDistinctCount().get(i) ));
                    dd = dd + dd3;
                }
                penalty = dd;
                break;
            default:
                throw new IllegalArgumentException( "Type must be 0..3" );
        }

        AbstractMap.SimpleEntry<STHolesBucket<R>, Double> res = new AbstractMap.SimpleEntry<STHolesBucket<R>, Double>(bn, penalty);

        return res;
    }

    /**
     * computes the penalty of merging siblings {b1} and {b2}
     * and the resulting box
     * @param b1 sibling 1
     * @param b2 sibling 2
     * @return pair of merge penalty and resulting box
     */
    private Map.Entry<STHolesBucket<R>, Double> getSSMergePenalty(STHolesBucket<R> b1, STHolesBucket<R> b2) {
        return getSSMergePenalty( PENALTY_TYPE, b1, b2 );
    }

    /**
     * computes the penalty of merging siblings {b1} and {b2}
     * and the resulting box
     * @param type penalty formula (0,1,2)
     * @param b1 sibling 1
     * @param b2 sibling 2
     * @return pair of merge penalty and resulting box
     */
    private Map.Entry<STHolesBucket<R>, Double>
    getSSMergePenalty(int type, STHolesBucket<R> b1, STHolesBucket<R> b2) {

        if (!b1.getParent().equals(b2.getParent())) {
            //todo: throw exception
            return null;
        }
        R newBox = getSiblingSiblingBox(b1,b2);
        // the smallest box that encloses both b1 and b2 and also
        // encloses any other buckets with which it intersects,
        // even partially.


        // I includes bp's children which are enclosed by bn box
        Collection<STHolesBucket<R>> I = new ArrayList<STHolesBucket<R>>();
        STHolesBucket<R> bp = b1.getParent();

        for (STHolesBucket<R> bi : bp.getChildren() ) {

            if (!(bi.equals(b1) || bi.equals(b2))) {

                if (newBox.contains(bi.getBox())) {

                    I.add(bi);
                }
            }
        }

        // Set statistics
        long newFrequency = b1.getStatistics().getFrequency() + b2.getStatistics().getFrequency();
        List<Long> b1Distinct = b1.getStatistics().getDistinctCount();
        List<Long> curDistinct = b2.getStatistics().getDistinctCount();

        List<Long> newDistinct = new LinkedList<>(b1Distinct);

        for (int i = 0; i < b1Distinct.size(); i++) {
            long a;

           if(b1.getBox().getRange(i).intersects(b2.getBox().getRange(i))) {
                a = Math.max(b1Distinct.get(i), curDistinct.get(i));
            }else {
                a = b1Distinct.get(i) + curDistinct.get(i);
            }

            newDistinct.set(i, a);
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
        STHolesBucket<R> bn = new STHolesBucket<R>(newBox, newStatistics, newChildren, bp);

        double penalty;
        double dd, dd2, bn_size, b1_size, b2_size;
        int dim;
        switch( type ) {
            case 0:
                penalty =
                        Math.abs( b1.getEstimate(null) - bn.getEstimate(null) ) +
                                Math.abs( b2.getEstimate(null) - bn.getEstimate(null) );
                break;
            case 1:
                dd =
                        Math.abs( b1.getStatistics().getDensity() - bn.getStatistics().getDensity() ) +
                                Math.abs( b2.getStatistics().getDensity() - bn.getStatistics().getDensity() );
                logger.info("SS: b1 density = "+b1.getStatistics().getDensity()+" b2 density = "+ b2.getStatistics().getDensity()+" bn density = "+bn.getStatistics().getDensity());
                penalty = dd;
                break;
            case 2:
                dim = b1.getStatistics().getDistinctCount().size();
                penalty = 0.0;
                bn_size = bn.getStatistics().getFrequency();
                b1_size = b1.getStatistics().getFrequency();
                b2_size = b2.getStatistics().getFrequency();
                for( int i=0; i<dim; ++i ) {
                    dd = bn_size / (double)bn.getStatistics().getDistinctCount().get(i);
                    dd2 = b1_size / (double)b1.getStatistics().getDistinctCount().get(i);
                    penalty += Math.abs( dd2 - dd );
                    dd2 = b2_size / (double)b2.getStatistics().getDistinctCount().get(i);
                    penalty += Math.abs( dd2 - dd );
                }
                break;
            case 3:
                dim = b1.getStatistics().getDistinctCount().size();
                if(b1.getStatistics().getFrequency() == 0 || b2.getStatistics().getFrequency() == 0) {
                    dd = 1.0;
                } else {
                    dd = (double) ((double) (Math.abs( b1.getStatistics().getDensity() - bn.getStatistics().getDensity() )) /  ( b1.getStatistics().getDensity() + bn.getStatistics().getDensity())+
                            (double) Math.abs( b2.getStatistics().getDensity() - bn.getStatistics().getDensity() ) / (  b2.getStatistics().getDensity() + bn.getStatistics().getDensity() )) / 2;

                }
                double dd3 = 0.0;
                for( int i=0; i<dim; ++i ) {
                    dd3 = (Math.abs(( (double)bn.getStatistics().getDistinctCount().get(i) - (double)b1.getStatistics().getDistinctCount().get(i)) )
                            / ( (double)bn.getStatistics().getDistinctCount().get(i) + (double)b1.getStatistics().getDistinctCount().get(i) ));

                    dd = dd +dd3;

                    dd3 = (Math.abs(( (double)bn.getStatistics().getDistinctCount().get(i) - (double)b2.getStatistics().getDistinctCount().get(i)) )
                            / ( (double)bn.getStatistics().getDistinctCount().get(i) + (double)b2.getStatistics().getDistinctCount().get(i) ));


                    dd = dd + dd3;
                }

                penalty = dd;

                break;
            default:
                throw new IllegalArgumentException( "Type must be 0..3" );
        }

        AbstractMap.SimpleEntry<STHolesBucket<R>, Double> res =
                new AbstractMap.SimpleEntry<STHolesBucket<R>, Double>(bn, penalty);

        return res;
    }


    public STHolesBucket<R> getRoot() {
        return root;
    }

    public void setRoot(STHolesBucket<R> root) {
        this.root = root;
    }

    public long getBucketsNum() { return bucketsNum; }

    public void setBucketNum(long bucketsNum) { this.bucketsNum = bucketsNum; }

    public void setMaxBucketsNum(long maxBucketsNum) {
        this.maxBucketsNum = maxBucketsNum;
    }

    public long getSsMergesNum() {
        return ssMergesNum;
    }

    public void setSsMergesNum(long ssMergesNum) {
        this.ssMergesNum = ssMergesNum;
    }

    public long getPcMergesNum() {
        return pcMergesNum;
    }

    public void setPcMergesNum(long pcMergesNum) {
        this.pcMergesNum = pcMergesNum;
    }
}
