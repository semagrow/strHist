package gr.demokritos.iit.irss.semagrow.stholesOrig;

import gr.demokritos.iit.irss.semagrow.api.RectangleWithVolume;

/**
 * Created by efi on 6/8/2014.
 */
public class MergeInfo<R extends RectangleWithVolume<R>> {

    private STHolesOrigBucket<R> b1;
    private STHolesOrigBucket<R> b2;
    private STHolesOrigBucket<R> bn;
    private long penalty;

    MergeInfo(STHolesOrigBucket<R> b1, STHolesOrigBucket<R> b2, STHolesOrigBucket<R> bn, long penalty) {
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

    public long getPenalty() {
        return penalty;
    }
}
