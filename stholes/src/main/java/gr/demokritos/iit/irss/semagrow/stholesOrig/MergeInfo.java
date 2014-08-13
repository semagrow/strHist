package gr.demokritos.iit.irss.semagrow.stholesOrig;

import gr.demokritos.iit.irss.semagrow.api.RectangleWithVolume;

/**
 * Created by efi on 6/8/2014.
 */
public class MergeInfo<R extends RectangleWithVolume<R>> {

    private STHolesOrigBucket<R> b1;
    private STHolesOrigBucket<R> b2;
    private STHolesOrigBucket<R> bn;
    private double penalty;

    MergeInfo(STHolesOrigBucket<R> b1, STHolesOrigBucket<R> b2, STHolesOrigBucket<R> bn, double penalty) {
        this.b1 = b1;
        this.b2 = b2;
        this.bn = bn;
        this.penalty = penalty;
    }

    public  STHolesOrigBucket<R> getB1() {
        return b1;
    }

    public STHolesOrigBucket<R> getB2() {
        return b2;
    }

    public STHolesOrigBucket<R> getBn() {
        return bn;
    }

    public double getPenalty() {
        return penalty;
    }

    public String toString() {

        String res;

        res = "Merge: \n" +
                "b1: \n" + b1.toString() +
                "b2: \n" + b2.toString() +
                "into bn: \n" + bn.toString() +
                "with penalty " + penalty;
        return res;
    }
}
