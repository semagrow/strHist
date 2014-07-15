package gr.demokritos.iit.irss.semagrow.stholes;


import gr.demokritos.iit.irss.semagrow.api.Rectangle;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

/**
 * Created by angel on 7/11/14.
 */
public class STHolesBucket<R extends Rectangle> {

    private R box;

    private long frequency;

    private Collection<STHolesBucket<R>> children;

    private STHolesBucket<R> parent;

    private List<Long> distinct;

    public STHolesBucket(R box, long frequency, List<Long> distinct) {
        this.box = box;
        this.frequency = frequency;
        this.distinct = distinct;
    }

    public STHolesBucket(R box, long frequency,
                         Collection<STHolesBucket<R>> children,
                         STHolesBucket parent, List<Long> distinct) {
        this.box = box;
        this.frequency = frequency;
        this.children = children;
        this.parent = parent;
        this.distinct = distinct;
    }

    public R getBox() {
        return box;
    }

    public long getFrequency() {
        return frequency;
    }

    public Collection<STHolesBucket<R>> getChildren() {
        return children;
    }

    public STHolesBucket getParent() {
        return parent;
    }

    public List<Long> getDistinct() {
        return distinct;
    }

    public void addChild(STHolesBucket<R> bucket) {
        children.add(bucket);
        bucket.parent = this;
    }

    public static STHolesBucket merge(STHolesBucket bucket1,
                                      STHolesBucket bucket2) {
        if (bucket2.getParent() == bucket1) { //or equals
            return parentChildMerge(bucket1,bucket2);
        }
        else if (bucket2.getParent() == bucket1.getParent()) {
            return siblingSiblingMerge(bucket1,bucket2);
        }
        return null;
    }

    public static <R extends Rectangle> STHolesBucket<R>
        parentChildMerge(STHolesBucket<R> bp, STHolesBucket<R> bc) {

        R newBox = bp.getBox();
        long newFreq = bp.getFrequency();

        List<Long> newDistinct = bp.getDistinct();
        STHolesBucket<R> newParent = bp.getParent();

        STHolesBucket<R> bn = new STHolesBucket<R>(newBox, newFreq, null, newParent, newDistinct);

        for (STHolesBucket<R> bi : bc.getChildren())
            bi.setParent(bn);

        return bn;
    }

    public static <R extends Rectangle> STHolesBucket <R>
        siblingSiblingMerge(STHolesBucket<R> b1, STHolesBucket<R> b2) {

        //TODO: Rectangle newBox = getSiblingSiblingBox(b1,b2);
        // the smallest box that encloses both b1 and b2 but does not
        // intersect partially with any other of bp
        R newBox = b1.getBox(); //just temporary

        // I contains bp's children which are enclosed by bn box
        Collection<STHolesBucket> I = new ArrayList<STHolesBucket>();
        STHolesBucket<R> bp = b1.getParent();

        for (STHolesBucket<R> bi : bp.getChildren() ) {

            if (bi.getBox().contains(newBox)) {
                I.add(bi);
            }
        }

        // parent(bn) = bp;
        STHolesBucket<R> newParent = bp;

        // Set statistics
        long newFrequency = b1.getFrequency() + b2.getFrequency();
        List<Long> newDistinct = b1.getDistinct();
        List<Long> curDistinct = b2.getDistinct();

        for (int i = 0; i < newDistinct.size(); i++) {

            newDistinct.set(i, Math.max(newDistinct.get(i), curDistinct.get(i)));
        }

        for (STHolesBucket bi : I) {

            curDistinct = bi.getDistinct();
            newFrequency += bi.getFrequency() ;

            for (int i = 0; i < newDistinct.size(); i++) {

                newDistinct.set(i,  Math.max(newDistinct.get(i), curDistinct.get(i)));
            }
        }

        Collection<STHolesBucket<R>> newChildren = new ArrayList<STHolesBucket<R>>();
        I.addAll(b1.getChildren());
        I.addAll(b2.getChildren());

        for (STHolesBucket bi : I) {

            newChildren.add(bi);
        }

        STHolesBucket<R> bn = new STHolesBucket<R>(newBox, newFrequency, newChildren, newParent, newDistinct);

        return bn;
    }

    public static <R extends Rectangle> STHolesBucket<R> shrink() {
        return null;
    }

    public void setFrequency(long frequency) {
        this.frequency = frequency;
    }

    public void setDistinct(List<Long> distinct) {
        this.distinct = new ArrayList(distinct);
    }

    public void setParent(STHolesBucket<R> parent) {
        this.parent = parent;
    }

    public long getEstimate(R rec) {

        long estimate = frequency;

        for (int i=0; i< rec.getDimensionality(); i++) {

            if (rec.getRange(i).isUnit())
                estimate *= 1 /  distinct.get(i);
        }

        return estimate;
    }

}
