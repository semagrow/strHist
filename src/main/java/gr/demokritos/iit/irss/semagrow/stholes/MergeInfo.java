package gr.demokritos.iit.irss.semagrow.stholes;

import gr.demokritos.iit.irss.semagrow.api.Rectangle;

/**
 * Created by efi on 17/7/2014.
 */
public class MergeInfo<R extends Rectangle<R>> {

    private STHolesBucket<R> b1;
    private STHolesBucket<R> b2;
    private STHolesBucket<R> bn;
    private long penalty;

    MergeInfo(STHolesBucket<R> b1, STHolesBucket<R> b2, STHolesBucket<R> bn, long penalty) {
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

    public long getPenalty() {
        return penalty;
    }
}
