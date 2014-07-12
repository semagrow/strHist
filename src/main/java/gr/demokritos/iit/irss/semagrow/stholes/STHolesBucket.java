package gr.demokritos.iit.irss.semagrow.stholes;


import gr.demokritos.iit.irss.semagrow.api.Rectangle;

import java.util.Collection;

/**
 * Created by angel on 7/11/14.
 */
public class STHolesBucket {

    private Rectangle box ;
    private long frequency;
    private Collection<STHolesBucket> children;
    private STHolesBucket parent;

    public STHolesBucket (Rectangle box, long frequency,
                          Collection<STHolesBucket> children,
                          STHolesBucket parent) {
        this.box = box;
        this.frequency = frequency;
        this.children = children;
        this.parent = parent;
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

}
