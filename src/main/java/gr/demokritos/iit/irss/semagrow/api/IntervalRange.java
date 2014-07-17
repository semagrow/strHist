package gr.demokritos.iit.irss.semagrow.api;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Defines a set of numbers that lies on an interval
 * Created by angel on 7/12/14.
 */
public class IntervalRange<Integer> implements RangeLength<Integer>, Rangeable<IntervalRange> {

    private int low;

    private int high;

    public IntervalRange(int low, int high) {
        this.low = low;
        this.high = high;
    }

    public boolean contains(int item) {
        return (item >= low) && (item <= high);
    }

    public boolean contains(IntervalRange range) {

        return (range.getLow() >= low) &
                (range.getHigh() <= high);

    }

    
    public boolean intersects(IntervalRange range) {

        int nLow = max(low, range.getLow());
        int nHigh = min(high, range.getHigh());

        if (nLow > nHigh) {

            return false;
        }

        return true;
    }

    public IntervalRange intersection(IntervalRange range) {

            IntervalRange res;

            int nLow = max(low, range.getLow());
            int nHigh = min(high, range.getHigh());

            if (nLow <= nHigh) {

                res = new IntervalRange(nLow, nHigh);
            } else {

                res = null;
            }

            return res;
    }

   
    public IntervalRange minus(IntervalRange intervalRange) {
        int lowN = low;
        int highN = high;

        if (intervalRange.low <= this.low)
            lowN = intervalRange.high;

        if (intervalRange.high >= this.high)
            highN = intervalRange.low;

        return new IntervalRange(lowN, highN);
    }


    public boolean isUnit() { return (high == low); }

    public long getLength() { return high - low; }

    public int getLow() { return low; }

    public int getHigh() { return high; }

    public String toString() {
        return "(" + low + "," + high + ")";
    }

    public static void main(String [] args){
        IntervalRange myRange = new IntervalRange(1,6);
        int item1 = 3;
        int item2 = 7;
        boolean res1 = myRange.contains(item1);
        boolean res2 = myRange.contains(item2);

        // Test getters
        System.out.println("My range is (" + myRange.getLow() + "," +
                myRange.getHigh() + ")");
        //Test contains item method
        if (res1)
            System.out.println("My range contains " + item1);
        else
            System.out.println("Test failed");

        if (!res2)
            System.out.println("My range does not contain " + item2);
        else
            System.out.println("Test failed");
        //Test contains range method
        IntervalRange testRange1 = new IntervalRange(3,5);
        IntervalRange testRange2 = new IntervalRange(1,8);

        res1 = myRange.contains(testRange1);
        res2 = myRange.contains(testRange2);

        System.out.println("Range " + myRange.toString() + " contains range "
                + testRange1.toString() + ":" + res1);
        System.out.println("Range " + myRange.toString() + " contains range "
                + testRange2.toString() + ":" + res2);

        //Test intersection method
        testRange1 = new IntervalRange(2,7);
        testRange2 = new IntervalRange(0,8);
        IntervalRange testRange3 = new IntervalRange(8,10);


        IntervalRange intersection1 = (IntervalRange) myRange.intersection(
                testRange1);
        IntervalRange intersection2 = (IntervalRange) myRange.intersection(
                testRange2);
        IntervalRange intersection3 = (IntervalRange) myRange.intersection(
                testRange3);

        System.out.println("Intersection of range " + myRange.toString() +
                " and range " + testRange1.toString() + ":" + intersection1);
        System.out.println("Intersection of range " + myRange.toString() +
                " and range " + testRange2.toString() + ":" + intersection2);
        System.out.println("Intersection of range " + myRange.toString() +
                " and range " + testRange3.toString() + ":" + intersection3);

    }
}
