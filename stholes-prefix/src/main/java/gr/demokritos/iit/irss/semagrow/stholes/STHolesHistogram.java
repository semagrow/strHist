package gr.demokritos.iit.irss.semagrow.stholes;

import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.api.STHistogram;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryResult;
import gr.demokritos.iit.irss.semagrow.base.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by angel on 7/11/14.
 * @author efi
 * @author nickozoulis
 */
public class STHolesHistogram<R extends Rectangle<R>> extends STHistogramBase<R,Stat> implements STHistogram<R,Stat> {
    static final Logger logger = LoggerFactory.getLogger(STHolesHistogram.class);
    private STHolesBucket<R> root;
    public long maxBucketsNum = 100;
    public Double epsilon = 0.0;
    private long bucketsNum = 0;

    public long pcMergesNum = 0;
    public long ssMergesNum = 0;

    static public final int MAX_PENALTY_TYPE = 2;
    public int PENALTY_TYPE = 1;

    public STHolesHistogram() {
        //todo: choose a constant
        root = null;
        bucketsNum += bucketsNum;
    }

    public STHolesHistogram(STHolesBucket root) {
        this.root = root;
        bucketsNum++;
    }

    public STHolesHistogram(Iterator<QueryRecord<R,Stat>> workload) {
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

        if (queryRecord.getRectangle().isInfinite()) {
            //rects.addAll( queryRecord.getResultSet().getRectangles(queryRecord.getRectangle()));
            rects.addAll(queryRecord.getResultSet().getRectangles());
        } else {
            rects.add(queryRecord.getRectangle());
        }

        for (R rect : rects) {
            if (this.getRoot() == null) {
                setRoot(new STHolesBucket<R>(rect, new Stat(), null, null));
                bucketsNum += 1;
                logger.info("Root bucket is created");
            } else {
                // expand root
                if (!root.getBox().contains(rect)) {
                    // expand root box so that it includes q
                    R boxN = root.getBox().computeTightBox(rect);

                    Stat statsN = countMatchingTuples(rect, queryRecord);
                    Stat rootStatsN = computeRootStats(root.getStatistics(), statsN);

                    root.setBox(boxN);
                    root.setStatistics(rootStatsN);

                    logger.info("Root bucket is expanded");
                }
            }

            // get all c
            Iterable<STHolesBucket<R>> candidates = getCandidateBuckets(rect);//getCandidateBuckets(rect);

            for (STHolesBucket<R> bucket : candidates) {
                STHolesBucket<R> hole = shrink(bucket, rect, queryRecord); //calculate intersection and shrink it

                if (!hole.getBox().isEmpty() && isInaccurateEstimation(bucket, hole)) {
                    logger.info("Drilling hole " + hole.getBox().toString() + " with statistics " + hole.getStatistics().toString());
                    drillHole(bucket, hole);
                } else {
                    logger.info("Skip drilling for " + bucket.getBox().toString());
                }
            }
        }

        // check if histogram must be compacted after refinement
        logger.debug("Histogram refined with query: " + queryRecord.getRectangle());
        compact();
    }

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

        for (STHolesBucket<R> bucketChild : bucket.getChildren())
            candidates.addAll(getCandidateBuckets(bucketChild, queryBox));

        if (bucket.getBox().intersects(queryBox)) {
            boolean moreSpecific = true;

            // Add a candidate only if there isn't already any more specific candidate.
            for (STHolesBucket<R> candidate : candidates)
                if (candidate.getBox().isEnclosing(queryBox))
                    moreSpecific = false;

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
     * Create a hole (i.e. a child STHolesBucket) inside an existing bucket.
     * @param parentBucket parent bucket
     * @param candidateHole candidate hole
     */
    private void drillHole(STHolesBucket<R> parentBucket, STHolesBucket<R> candidateHole) {
        if (parentBucket.getBox().equals(candidateHole.getBox())) {
            Stat parentStats = new Stat(candidateHole.getStatistics().getFrequency(),
                                        candidateHole.getStatistics().getDistinctCount());
            parentBucket.setStatistics(parentStats);
        }
        else {
            Collection<STHolesBucket<R>> toBeRemoved = new ArrayList<STHolesBucket<R>>();

            for (STHolesBucket<R> bc : parentBucket.getChildren()) {
                if (candidateHole.getBox().contains(bc.getBox())) {
                    // If this candidates's box is equals to the child's box.
                    if (candidateHole.getBox().equals(bc.getBox())) {

                        // Refresh the candidate's statistics and replace this child.
                        candidateHole.getStatistics().setFrequency(candidateHole.getStatistics().getFrequency() +
                                                                   bc.getStatistics().getFrequency());

                        List<Long> distinct = new ArrayList<Long>();
                        for (int i=0; i<bc.getStatistics().getDistinctCount().size(); i++) {
                            distinct.add(Math.max(candidateHole.getStatistics().getDistinctCount().get(i),
                                                  bc.getStatistics().getDistinctCount().get(i)));
                        }
                        candidateHole.getStatistics().setDistinctCount(distinct);

                        toBeRemoved.add(bc);
                        continue;
                    }

                    candidateHole.addChild(bc);
                    toBeRemoved.add(bc);
                } else if (bc.getBox().contains(candidateHole.getBox())) {
                    // If this child's box is equals to the candidate hole's box.
                    if (bc.getBox().equals(candidateHole.getBox())) {
                        // Just refresh the child's statistics and continue loop.
                        bc.getStatistics().setFrequency(candidateHole.getStatistics().getFrequency() +
                                                        bc.getStatistics().getFrequency());

                        List<Long> distinct = new ArrayList<Long>();
                        for (int i=0; i<candidateHole.getStatistics().getDistinctCount().size(); i++) {
                            distinct.add(Math.max(candidateHole.getStatistics().getDistinctCount().get(i),
                                    bc.getStatistics().getDistinctCount().get(i)));
                        }
                        bc.getStatistics().setDistinctCount(distinct);

                        continue;
                    }

                    // Hangs the candidate bucket below the child.
                    bc.addChild(candidateHole);
                }
            }// for

            for (STHolesBucket<R> bc : toBeRemoved)
                parentBucket.removeChild(bc);

            parentBucket.addChild(candidateHole);

            bucketsNum++;
        }// else
    }// drillHole

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

             MergeInfo<R> bestMerge = findBestMerge(root);
             STHolesBucket<R> b1 = bestMerge.getB1();
             STHolesBucket<R> b2 = bestMerge.getB2();
             STHolesBucket<R> bn = bestMerge.getBn();

            STHolesBucket.merge(b1, b2, bn, this);
            logger.info("Best merge info: " + bestMerge.toString());
            logger.info("Number of PC merges: " + pcMergesNum);
            logger.info("Number of SS merges: " + ssMergesNum);
            bucketsNum -= 1;
        }
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


            // SS merges are commented out because of time complexity.
            /*// Candidate sibling-sibling merges
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

            }*/
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

    /**
     * computes the penalty of merging parent bucket {bp}
     * with child bucket {bp} and the resulting box
     * @param bp parent bucket
     * @param bc child bucket
     * @return pair of merge penalty and resulting box
     */
    private Map.Entry<STHolesBucket<R>, Double> getPCMergePenalty(STHolesBucket<R> bp, STHolesBucket<R> bc) {
        return getPCMergePenalty( PENALTY_TYPE, bp, bc );
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
        	break;
        default:
        	throw new IllegalArgumentException( "Type must be 0..2" );
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
        default:
        	throw new IllegalArgumentException( "Type must be 0..2" );
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
