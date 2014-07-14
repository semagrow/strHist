package gr.demokritos.iit.irss.semagrow.stholes;


import gr.demokritos.iit.irss.semagrow.api.Rectangle;

import java.util.ArrayList;
import java.util.Collection;

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

    public void addChild(STHolesBucket bucket) {
        children.add(bucket);
        bucket.parent = this;
    }

    public static STHolesBucket merge(STHolesBucket bucket1,
                                      STHolesBucket bucket2) {
        return null;
    }

    public static STHolesBucket shrink() {
        return null;
    }

    public void setFrequency(long frequency) {
        this.frequency = frequency;
    }

    public void setDistinct(ArrayList<Long> distinct) {
        this.distinct = distinct;
    }

    public void setParent(STHolesBucket parent) {
        this.parent = parent;
    }

 /*   public long getEstimate(Rectangle rec) {

        long estimate = frequency;
        for (int i=0; i< rec.getDimensionality(); i++) {
            if ((rec.getRange(i)).getLength() == 1) estimate *= 1 / distinct.get(i);
        }

        return estimate;
    } */

}
