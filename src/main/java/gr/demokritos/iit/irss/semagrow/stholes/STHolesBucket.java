package gr.demokritos.iit.irss.semagrow.stholes;


import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.rdf.Stat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by angel on 7/11/14.
 */
public class STHolesBucket<R extends Rectangle> {

    private R box;

    private long frequency;

    private Collection<STHolesBucket<R>> children;

    private Stat statistics;

    private STHolesBucket parent;

    private List<Long> distinct;

    public STHolesBucket(R box, Stat statistics) {
        this.box = box;
        this.statistics = statistics;
    }

    public STHolesBucket(R box, Stat statistics,
                         Collection<STHolesBucket<R>> children,
                         STHolesBucket parent) {
        this.box = box;
        this.statistics = statistics;
        this.children = children;
        this.parent = parent;
    }

    public R getBox() {
        return box;
    }

    public Stat getStatistics() {
        return statistics;
    }

    public Collection<STHolesBucket<R>> getChildren() {
        return children;
    }

    public STHolesBucket getParent() {
        return parent;
    }

    public void addChild(STHolesBucket<R> bucket) {
        children.add(bucket);
        bucket.parent = this;
    }

    public void removeChild(STHolesBucket<R> bucket) {
        children.remove(bucket);
    }

    public static <R extends Rectangle<R>> 
        void merge(STHolesBucket<R> bucket1,
                   STHolesBucket<R> bucket2, STHolesBucket<R> mergeBucket) {
        if (bucket2.getParent() == bucket1) { //or equals
            parentChildMerge(bucket1, bucket2, mergeBucket);
        }
        else if (bucket2.getParent() == bucket1.getParent()) {
            siblingSiblingMerge(bucket1, bucket2, mergeBucket);
        }
    }

    public static <R extends Rectangle<R>> void
        parentChildMerge(STHolesBucket<R> bp, STHolesBucket<R> bc, STHolesBucket<R> bn) {

        //Merge buckets b1, b2 into bn
        STHolesBucket bpp = bp.getParent();
        bpp.removeChild(bp);
        bpp.addChild(bn);
        bn.setParent(bpp);

        for (STHolesBucket bi : bc.getChildren())
            bi.setParent(bn);

        //return bn;
    }

    public static <R extends Rectangle<R>> 
        void siblingSiblingMerge(STHolesBucket<R> b1,
                                 STHolesBucket<R> b2,
                                 STHolesBucket<R> bn) 
    {
        STHolesBucket newParent = b1.getParent();

        // Merge buckets b1, b2 into bn
        bn.setParent(newParent);
        newParent.addChild(bn);

        for (STHolesBucket bi : bn.getChildren()) {

            bi.setParent(bn);
            newParent.removeChild(bi);
        }
    }

    public static <R extends Rectangle> STHolesBucket<R> shrink() {
        return null;
    }

    public void setStatistics(Stat statistics) {
        this.statistics = statistics;
    }

    public void setDistinct(List<Long> distinct) {
        this.distinct = new ArrayList(distinct);
    }

    public void setParent(STHolesBucket<R> parent) {
        this.parent = parent;
    }

    public long getEstimate(R rec) {

        long estimate = statistics.getFrequency();

        for (int i=0; i< rec.getDimensionality(); i++) {

            if ((rec.getRange(i)).isUnit())
                estimate *= 1 /  statistics.getDistinctCount().get(i);
        }

        return estimate;
    }

}
