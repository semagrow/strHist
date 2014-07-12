package gr.demokritos.iit.irss.semagrow.api;

/**
 * Defines a set of numbers that lies on an interval
 * Created by angel on 7/12/14.
 */
public class IntervalRange implements Range<Integer> {

    private int low;
    private int high;

    public IntervalRange(int low, int high) {
        this.low = low;
        this.high = high;
    }

    @Override
    public boolean contains(Integer item) {
        return false;
    }

    @Override
    public boolean contains(Range<Integer> range) {
        return false;
    }

    @Override
    public Range<Integer> intersect(Range<Integer> range) {
        return null;
    }

    @Override
    public Range<Integer> union(Range<Integer> range) {
        return null;
    }
}
