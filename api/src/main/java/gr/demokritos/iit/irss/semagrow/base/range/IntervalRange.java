package gr.demokritos.iit.irss.semagrow.base.range;

import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.api.range.Rangeable;

import java.io.Serializable;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Defines a set of numbers that lies on an interval
 * Created by angel on 7/12/14.
 */
public class IntervalRange implements RangeLength<Integer>, Rangeable<IntervalRange>, Serializable {

    //todo: maybe replace int with long
    private int low;

    private int high;

    public IntervalRange(int low, int high) {
        this.low = low;
        this.high = high;
    }

    public boolean includes(Integer item) {
        return (item >= low) && (item <= high);
    }


    //Tested
    public boolean contains(IntervalRange range) {

        return (range.getLow() >= low) &
                (range.getHigh() <= high);

    }

    //Tested
    public boolean intersects(IntervalRange range) {

        int nLow = max(low, range.getLow());
        int nHigh = min(high, range.getHigh());


        return nLow <= nHigh;
    }

    //Tested
    public IntervalRange tightRange(IntervalRange intervalRange) {

        return new IntervalRange(min(low, intervalRange.low), max(high, intervalRange.high));
    }


    public void expand(Integer value) {

        if (value < low )
            low = value;
        else if (value > high)
            high = value;
    }

    //Tested
    public IntervalRange intersection(IntervalRange range) {

            IntervalRange res;

            int nLow = max(low, range.getLow());
            int nHigh = min(high, range.getHigh());

            if (nLow <= nHigh) {

                res = new IntervalRange(nLow, nHigh);
            } else {

                res = new IntervalRange(nLow, nHigh);
            }

            return res;
    }

   // Tested
    public IntervalRange minus(IntervalRange intervalRange) {
        int lowN = low;
        int highN = high;

        //Scenario 1: participant includes bucket
        // in this dimension
        if (intervalRange.contains(this)) {

            return new IntervalRange(1,0);
        }

        //Scenario 2: bucket encloses participant range
        // in this dimension
        if (this.contains(intervalRange)) {

            int candidate1 = intervalRange.low - low;
            int candidate2 = high - intervalRange.high;


            if (candidate1 > candidate2) {

                lowN = low;
                highN = intervalRange.low;
               // return new IntervalRange(low, intervalRange.low);
            } else {
                lowN = intervalRange.high;
                highN = high;
               // return new IntervalRange(intervalRange.high, high);
            }

            //If intervalRange and this are adjacent
            if (lowN == intervalRange.high) {

                lowN += 1;
            }

            if (highN == intervalRange.low) {

                highN -= 1;
            }

            return new IntervalRange(lowN,highN);

        }
        //Scenario 3: default case
        if (intervalRange.low <= this.low)
            lowN = intervalRange.high;

        if (intervalRange.high >= this.high)
            highN = intervalRange.low;

        //If intervalRange and this are adjacent
        if (lowN == intervalRange.high) {

            lowN += 1;
        }

        if (highN == intervalRange.low) {

            highN -= 1;
        }

        return new IntervalRange(lowN, highN);
    }


    public boolean isUnit() { return (high == low); }

    public boolean isEmpty() { return low > high; }

    public long getLength() {
        if (isEmpty()) {
            return 0;
        }
        return high - low; }

    public int getLow() { return low; }

    public int getHigh() { return high; }

    public String toString() {

        return "[" + low + "-" + high + "]";
    }

    public boolean equals(Object o) {
        if (o instanceof IntervalRange) {
            IntervalRange i = (IntervalRange)o;
            return low == i.low && high == i.high;
        }
        return false;
    }
}
