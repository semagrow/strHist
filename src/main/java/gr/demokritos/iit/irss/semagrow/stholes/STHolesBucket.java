package gr.demokritos.iit.irss.semagrow.stholes;


import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.rdf.Stat;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

/**
 * Created by angel on 7/11/14.
 */
public class STHolesBucket {

    private Rectangle box ;

    //private long frequency;

    private Collection<STHolesBucket> children;

    private Stat statistics;

    private STHolesBucket parent;

    //private List<Long> distinct;

    public STHolesBucket(Rectangle box, Stat statistics) {
        this.box = box;
        this.statistics = statistics;
    }

    public STHolesBucket(Rectangle box, Stat statistics, Collection<STHolesBucket> children,
                         STHolesBucket parent) {

        this.box = box;
        this.statistics = statistics;
        this.children = children;
        this.parent = parent;
    }

    public Rectangle getBox() {
        return box;
    }

    public Stat getStatistics() {
        return statistics;
    }

    public Collection<STHolesBucket> getChildren() {
        return children;
    }

    public STHolesBucket getParent() {
        return parent;
    }

    public void addChild(STHolesBucket bucket) {
        children.add(bucket);
        bucket.parent = this;
    }

    public void removeChild(STHolesBucket bucket) {
        children.remove(bucket);
    }

    public static void merge(STHolesBucket bucket1,
                                      STHolesBucket bucket2, STHolesBucket mergeBucket) {
        if (bucket2.getParent() == bucket1) { //or equals
            parentChildMerge(bucket1, bucket2, mergeBucket);
        }
        else if (bucket2.getParent() == bucket1.getParent()) {
            siblingSiblingMerge(bucket1, bucket2, mergeBucket);
        }
    }

    public static void parentChildMerge(STHolesBucket bp,
                                                 STHolesBucket bc, STHolesBucket bn) {
        //Merge buckets b1, b2 into bn
        STHolesBucket bpp = bp.getParent();
        bpp.removeChild(bp);
        bpp.addChild(bn);
        bn.setParent(bpp);

        for (STHolesBucket bi : bc.getChildren())
            bi.setParent(bn);

        //return bn;
    }

    public static void siblingSiblingMerge(STHolesBucket b1,
                                                    STHolesBucket b2,
                                                    STHolesBucket bn) {

        STHolesBucket newParent = b1.getParent();

        // Merge buckets b1, b2 into bn
        bn.setParent(newParent);
        newParent.addChild(bn);

        for (STHolesBucket bi : bn.getChildren()) {

            bi.setParent(bn);
            newParent.removeChild(bi);
        }
    }

    public static STHolesBucket shrink() {
        return null;
    }

    public void setStatistics(Stat statistics) {
        this.statistics = statistics;
    }


    public void setParent(STHolesBucket parent) {
        this.parent = parent;
    }

    public long getEstimate(Rectangle rec) {

        long estimate = statistics.getFrequency();

        for (int i=0; i< rec.getDimensionality(); i++) {

            if ((rec.getRange(i)).getLength() == 1)
                estimate *= 1 /  statistics.getDistinctCount().get(i);
        }

        return estimate;
    }

}
