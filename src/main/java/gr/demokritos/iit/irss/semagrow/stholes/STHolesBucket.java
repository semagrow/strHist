package gr.demokritos.iit.irss.semagrow.stholes;


import gr.demokritos.iit.irss.semagrow.api.Rectangle;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

/**
 * Created by angel on 7/11/14.
 */
public class STHolesBucket {

    private Rectangle box ;
    private long frequency;
    private Collection<STHolesBucket> children;
    private STHolesBucket parent;
    private ArrayList<Long> distinct;


    public STHolesBucket(Rectangle box, long frequency,
                         Collection<STHolesBucket> children,
                         STHolesBucket parent, ArrayList<Long> distinct) {
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

    public ArrayList<Long> getDistinct() {
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

        ArrayList<Long> newDistinct = bp.getDistinct();
        STHolesBucket newParent = bp.getParent();

        STHolesBucket bn = new STHolesBucket(newBox, newFreq, null, newParent, newDistinct);

        for (STHolesBucket bi : bc.getChildren())
            bi.setParent(bn);

        return bn;
    }

    public static STHolesBucket siblingSiblingMerge(STHolesBucket b1,
                                                    STHolesBucket b2) {

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

        // parent(bn) = bp;
        STHolesBucket newParent = bp;

        // Set statistics
        long newFrequency = b1.getFrequency() + b2.getFrequency();
        ArrayList<Long> newDistinct = b1.getDistinct();
        ArrayList<Long> curDistinct = b2.getDistinct();

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

        Collection<STHolesBucket> newChildren = new ArrayList<STHolesBucket>();
        I.addAll(b1.getChildren());
        I.addAll(b2.getChildren());

        for (STHolesBucket bi : I) {

            newChildren.add(bi);
        }

        STHolesBucket bn = new STHolesBucket(newBox, newFrequency, newChildren, newParent, newDistinct);

        return bn;
    }

    public static STHolesBucket shrink() {
        return null;
    }

    public void setFrequency(long frequency) {
        this.frequency = frequency;
    }

    public void setDistinct(Collection<Long> distinct) {
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
