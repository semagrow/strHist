package gr.demokritos.iit.irss.semagrow.stholes;


import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.base.Stat;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by angel on 7/11/14.
 */
public class STHolesBucket<R extends Rectangle> {

    private R box;
    private Collection<STHolesBucket<R>> children;
    private Stat statistics;
    private STHolesBucket<R> parent;

    public STHolesBucket() {
        this.children = new ArrayList<STHolesBucket<R>>();
    }

    public STHolesBucket(R box, Stat statistics) {
        this.box = box;
        this.statistics = statistics;
        this.children = new ArrayList<STHolesBucket<R>>();
    }

    public STHolesBucket(R box, Stat statistics,
                         Collection<STHolesBucket<R>> children,
                         STHolesBucket<R> parent) {
        this.box = box;
        this.statistics = statistics;

        if (children == null) {
            this.children = new ArrayList<STHolesBucket<R>>();
        } else {
            this.children = children;
        }
        setParent(parent);
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

    public STHolesBucket<R> getParent() {
        return parent;
    }

    public void addChild(STHolesBucket<R> bucket) {
        children.add(bucket);
        bucket.parent = this;
    }

    public void removeChild(STHolesBucket<R> bucket) {
        if (!children.isEmpty()) {
            children.remove(bucket);
        }
    }

    public static <R extends Rectangle<R>> void merge(STHolesBucket<R> bucket1,
                                                      STHolesBucket<R> bucket2,
                                                      STHolesBucket<R> mergeBucket,
                                                      STHolesHistogram<R> h) {
        if (bucket2.getParent() == bucket1) { //or equals
            parentChildMerge(bucket1, bucket2, mergeBucket, h);
        }
        else if (bucket2.getParent() == bucket1.getParent()) {
            siblingSiblingMerge(bucket1, bucket2, mergeBucket, h);
        }
    }

    public static <R extends Rectangle<R>> void parentChildMerge(STHolesBucket<R> bp,
                                                                 STHolesBucket<R> bc,
                                                                 STHolesBucket<R> bn,
                                                                 STHolesHistogram<R> h) {

        //Merge buckets b1, b2 into bn
        STHolesBucket<R> bpp = bp.getParent();

        //Children of both buckets bc and bp
        //become children of the new bucket
        for (STHolesBucket<R> bi : bc.getChildren())
            bn.addChild(bi);

        for (STHolesBucket<R> bchild : bp.children) {
            if (!bchild.equals(bc)) {
                bn.addChild(bchild);
            }
        }

        if (bpp == null) {
            //bp is root
            h.setRoot(bn);
        } else {
            bpp.removeChild(bp);
            bpp.addChild(bn);
        }

        h.setPcMergesNum(h.getPcMergesNum() + 1);
    }

    public static <R extends Rectangle<R>> void siblingSiblingMerge(STHolesBucket<R> b1,
                                                                    STHolesBucket<R> b2,
                                                                    STHolesBucket<R> bn,
                                                                    STHolesHistogram<R> h) {
        //todo: throw exception if they are not siblings
        STHolesBucket<R> newParent = b1.getParent();

        // Merge buckets b1, b2 into bn
        newParent.addChild(bn);
        newParent.removeChild(b1);
        newParent.removeChild(b2);

        for (STHolesBucket<R> bi : bn.getChildren()) {

            bi.setParent(bn);
            newParent.removeChild(bi);
        }

        h.setSsMergesNum(h.getSsMergesNum() + 1);
    }

    public void setStatistics(Stat statistics) {
        this.statistics = statistics;
    }


    public void setParent(STHolesBucket<R> parent) {
        this.parent = parent;
    }

    public void setBox(R box) {
        this.box = box;
    }

    public long getEstimate(R rec) {
        long dvc = 1;
        
        // If no argument, return myself's estimate
        if (rec == null) { rec = this.box; }

        for (int i=0; i< rec.getDimensionality(); i++) {
            if ((rec.getRange(i)).isUnit()) {
            	dvc *= this.statistics.getDistinctCount().get(i);
            }
        }
        
        float estimate = ((float)this.statistics.getFrequency().longValue()) / ((float)dvc);
        return Math.round( estimate );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof STHolesBucket)) return false;

        STHolesBucket that = (STHolesBucket) o;

        if (!box.equals(that.box)) return false;
        if (children != null ? !children.equals(that.children) : that.children != null) return false;
        if (!parent.equals(that.parent)) return false;
        if (!statistics.equals(that.statistics)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = box.hashCode();
        result = 31 * result + (children != null ? children.hashCode() : 0);
        result = 31 * result + statistics.hashCode();
        result = 31 * result + parent.hashCode();
        return result;
    }

    public String toString() {
        String res = "bucket: \n" + box + statistics;
        int childrenNum = children.size();
        res += "childrenNum: \n\t" + childrenNum + "\n";
        return res;
    }

}
