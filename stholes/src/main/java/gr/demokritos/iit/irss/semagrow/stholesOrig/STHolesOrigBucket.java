package gr.demokritos.iit.irss.semagrow.stholesOrig;

import gr.demokritos.iit.irss.semagrow.api.RectangleWithVolume;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by efi on 6/8/2014.
 */
public class STHolesOrigBucket<R extends RectangleWithVolume<R>> {

    private R box;

    private Collection<STHolesOrigBucket<R>> children;

    private long frequency;

    private STHolesOrigBucket<R> parent;

    public STHolesOrigBucket() {
    }

    //Tested
    public STHolesOrigBucket(R box, long frequency) {
        this.box = box;
        this.frequency = frequency;

        this.children = new ArrayList<STHolesOrigBucket<R>>();
    }

    //Tested
    public STHolesOrigBucket(R box, long frequency,
                         Collection<STHolesOrigBucket<R>> children,
                         STHolesOrigBucket<R> parent) {
        this.box = box;
        this.frequency = frequency;

        if (children == null) {

            this.children = new ArrayList<STHolesOrigBucket<R>>();
        } else {
            this.children = children;
        }
        setParent(parent);
    }

    //Tested
    public R getBox() {
        return box;
    }

    public long getFrequency() {
        return frequency;
    }

    //Tested
    public Collection<STHolesOrigBucket<R>> getChildren() {
        return children;
    }

    public STHolesOrigBucket<R> getParent() {
        return parent;
    }

    public long getVolume() {

        long v = 0;

        v = box.getVolume();

        for (STHolesOrigBucket<R> bc : children) {

            v -= bc.box.getVolume();
        }

        return v;
    }
    //Tested
    public void addChild(STHolesOrigBucket<R> bucket) {


        children.add(bucket);
        bucket.parent = this;
    }

    public void removeChild(STHolesOrigBucket<R> bucket) {

        if (!children.isEmpty()) {

            children.remove(bucket);
        }
    }

    public static <R extends RectangleWithVolume<R>>
    void merge(STHolesOrigBucket<R> bucket1,
               STHolesOrigBucket<R> bucket2,
               STHolesOrigBucket<R> mergeBucket)
    {
        if (bucket2.getParent() == bucket1) { //or equals
            parentChildMerge(bucket1, bucket2, mergeBucket);
        }
        else if (bucket2.getParent() == bucket1.getParent()) {
            siblingSiblingMerge(bucket1, bucket2, mergeBucket);
        }
    }

    public static <R extends RectangleWithVolume<R>> void
    parentChildMerge(STHolesOrigBucket<R> bp, STHolesOrigBucket<R> bc, STHolesOrigBucket<R> bn) {

        //Merge buckets b1, b2 into bn
        STHolesOrigBucket<R> bpp = bp.getParent();
        bpp.removeChild(bp);
        bpp.addChild(bn);
        bn.setParent(bpp);

        //Children of both buckets bc and bp
        //become children of the new bucket
        for (STHolesOrigBucket<R> bi : bc.getChildren())
            bi.setParent(bn);

        for (STHolesOrigBucket<R> bchild : bp.children) {

            if (!bchild.equals(bc)) {

                bchild.setParent(bn);
            }
        }

    }

    public static <R extends RectangleWithVolume<R>>
    void siblingSiblingMerge(STHolesOrigBucket<R> b1,
                             STHolesOrigBucket<R> b2,
                             STHolesOrigBucket<R> bn)
    {
        //todo: throw exception if they are not siblings

        STHolesOrigBucket<R> newParent = b1.getParent();

        if (bn.box.equals(newParent.box)) {

            //transform the sibling-sibling merge of b1 and b2
            //into two parent-child merges
            //1st merge
            R newBox = newParent.box;
            long newFreq = newParent.frequency + b1.frequency;
            STHolesOrigBucket<R> parentN = newParent.getParent();
            STHolesOrigBucket<R> bn1 = new STHolesOrigBucket<R>(newBox, newFreq, null, parentN);
            merge(newParent, b1, bn1);
            //2nd merge
            STHolesOrigBucket<R> bp = b2.parent;
            newBox = bp.box;
            newFreq = bp.frequency + b2.frequency;
            parentN = bp.getParent();
            STHolesOrigBucket<R> bn2 = new STHolesOrigBucket<R>(newBox, newFreq, null, parentN);
            merge(bp, b2, bn2);

            return;
        }

        Collection<STHolesOrigBucket<R>> I = new ArrayList<STHolesOrigBucket<R>>();


        for (STHolesOrigBucket<R> bi : newParent.getChildren() ) {

            if (bi.getBox().contains(bn.getBox())) {
                I.add(bi);
            }
        }

        long vold = bn.getBox().getVolume() - b1.getBox().getVolume() -
                b2.getBox().getVolume();

        for (STHolesOrigBucket<R> bi : I) {

            vold -= bi.getBox().getVolume();
        }



        // Merge buckets b1, b2 into bn
        bn.setParent(newParent);
        newParent.addChild(bn);

        newParent.setFrequency((long)Math.ceil(newParent.frequency*
                ( 1 - (double)vold/newParent.getVolume())));

        for (STHolesOrigBucket<R> bi : bn.getChildren()) {

            bi.setParent(bn);
            newParent.removeChild(bi);
        }
    }

    public double getEstimate(R rec) {

            if (this.box.intersects(rec)) {

                double estimate;

                estimate = (double)this.getIntersectionWithRecVolume(rec) / this.getVolume() * this.frequency;

                for (STHolesOrigBucket<R> bc : children) {
                    estimate += bc.getEstimate(rec);
                }

                return estimate;
            } else {

                return 0;
            }
    }

    public long getIntersectionWithRecVolume(R rec) {

        long v;

        v = this.box.intersection(rec).getVolume();

        for (STHolesOrigBucket<R> bc : children) {

            v -= bc.box.intersection(rec).getVolume();
        }

        return v;

    }

    public void setFrequency(long frequency) {
        this.frequency = frequency;
    }


    public void setParent(STHolesOrigBucket<R> parent) {

        this.parent = parent;
    }

    public void setBox(R box) {
        this.box = box;
    }



    //Tested
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof STHolesOrigBucket)) return false;

        STHolesOrigBucket that = (STHolesOrigBucket) o;

        if (!box.equals(that.box)) return false;
        if (children != null ? !children.equals(that.children) : that.children != null) return false;
        if (!parent.equals(that.parent)) return false;
        if (frequency != that.frequency) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = box.hashCode();
        result = 31 * result + (children != null ? children.hashCode() : 0);
        result = 31 * result + (int) (frequency ^ (frequency >>> 32));
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        return result;
    }
/*
    @Override
    public int hashCode() {
        int result = box.hashCode();
        result = 31 * result + (children != null ? children.hashCode() : 0);
       //todo: result = 31 * result + frequency.hashCode();
        result = 31 * result + parent.hashCode();
        return result;
    }
    */

    //Tested
    public String toString() {

        String res = "bucket: \n" + box + frequency;

        int childrenNum = children.size();
        res += "childrenNum: \n\t" + childrenNum + "\n";
        return res;
    }

    public JSONObject toJSON() {
        JSONObject jSONObj = new JSONObject();
        //todo
        return jSONObj;
    }


    public static void main(String args[] ) {


    }
}
