package gr.demokritos.iit.irss.semagrow.stholes;

import gr.demokritos.iit.irss.semagrow.api.Rectangle;

/**
 * Created by efi on 17/7/2014.
 */
public class MergeInfo<R extends Rectangle<R>> {

    private STHolesBucket<R> b1;
    private STHolesBucket<R> b2;
    private STHolesBucket<R> bn;
    private double penalty;

    public MergeInfo(STHolesBucket<R> b1, STHolesBucket<R> b2, STHolesBucket<R> bn, double penalty) {
        this.b1 = b1;
        this.b2 = b2;
        this.bn = bn;
        this.penalty = penalty;
    }

    public  STHolesBucket<R> getB1() {
        return b1;
    }

    public STHolesBucket<R> getB2() {
        return b2;
    }

    public STHolesBucket<R> getBn() {
        return bn;
    }

    public void setB1(STHolesBucket<R> b1) {
        this.b1 = b1;
    }

    public void setB2(STHolesBucket<R> b2) {
        this.b2 = b2;
    }

    public void setBn(STHolesBucket<R> bn) {
        this.bn = bn;
    }

    public double getPenalty() {
        return penalty;
    }

    public void setPenalty(double penalty) {
        this.penalty = penalty;
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

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (object instanceof MergeInfo) {
            MergeInfo merge = (MergeInfo) object;

            return merge.getB1().getBox().equals(this.getB1().getBox()) &&
                    merge.getB2().getBox().equals(this.getB2().getBox()) &&
                    merge.getBn().getBox().equals(this.getBn().getBox());
        }

        return false;

    }

}
