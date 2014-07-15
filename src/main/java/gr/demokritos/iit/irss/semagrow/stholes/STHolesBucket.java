package gr.demokritos.iit.irss.semagrow.stholes;


import gr.demokritos.iit.irss.semagrow.api.Rectangle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

/**
 * Created by angel on 7/11/14.
 */
public class STHolesBucket {

    private Rectangle box ;

    private long frequency;

    private Collection<STHolesBucket> children;

    private STHolesBucket parent;

    private List<Long> distinct;

    public STHolesBucket(Rectangle box, long frequency, List<Long> distinct) {
        this.box = box;
        this.frequency = frequency;
        this.distinct = distinct;
    }

    public STHolesBucket(Rectangle box, long frequency,
                         Collection<STHolesBucket> children,
                         STHolesBucket parent, List<Long> distinct) {
        this.box = box;
        this.frequency = frequency;
        this.children = children;
        this.parent = parent;
        this.distinct = distinct;
    }

    public Rectangle getBox() {
        return box;
    }

    public long getFrequency() {
        return frequency;
    }

    public Collection<STHolesBucket> getChildren() {
        return children;
    }

    public STHolesBucket getParent() {
        return parent;
    }

    public List<Long> getDistinct() {
        return distinct;
    }

    public void addChild(STHolesBucket bucket) {
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

    public static STHolesBucket
        parentChildMerge(STHolesBucket bp, STHolesBucket bc) {

        Rectangle newBox = bp.getBox();
        long newFreq = bp.getFrequency();

        List<Long> newDistinct = bp.getDistinct();
        STHolesBucket newParent = bp.getParent();

        STHolesBucket bn = new STHolesBucket(newBox, newFreq, null, newParent, newDistinct);

        for (STHolesBucket bi : bc.getChildren())
            bi.setParent(bn);

        return bn;
    }

    public static STHolesBucket siblingSiblingMerge(STHolesBucket b1,
                                                    STHolesBucket b2) {
        return null;
    }

    public static STHolesBucket shrink() {
        return null;
    }

    public void setFrequency(long frequency) {
        this.frequency = frequency;
    }

    public void setDistinct(List<Long> distinct) {
        this.distinct = new ArrayList(distinct);
    }

    public void setParent(STHolesBucket parent) {
        this.parent = parent;
    }

    public long getEstimate(Rectangle rec) {

        long estimate = frequency;

        for (int i=0; i< rec.getDimensionality(); i++) {

            if ((rec.getRange(i)).getLength() == 1)
                estimate *= 1 /  distinct.get(i);
        }

        return estimate;
    }

}
