package gr.demokritos.iit.irss.semagrow.api;

import org.json.simple.JSONObject;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Defines a set of numbers that lies on an interval
 * Created by angel on 7/12/14.
 */
public class IntervalRange<Integer> implements RangeLength<Integer>, Rangeable<IntervalRange> {

    //todo: maybe replace int with long
    private int low;

    private int high;

    public IntervalRange(int low, int high) {
        this.low = low;
        this.high = high;
    }

    // Tested
    public boolean contains(int item) {
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

        //todo: check equality
        return nLow < nHigh;
    }

    //Tested
    public IntervalRange tightRange(IntervalRange intervalRange) {

        return new IntervalRange(min(low, intervalRange.low), max(high, intervalRange.high));
    }


    public void expand(String v) {

        int value = java.lang.Integer.parseInt(v);

        if (value < low ) low = value;
        else if (value > high) high = value;

    }

    //Tested
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

   // Tested
    public IntervalRange minus(IntervalRange intervalRange) {
        int lowN = low;
        int highN = high;

        //Scenario 1: participant contains bucket
        // in this dimension
        if (intervalRange.contains(this)) {

            return new IntervalRange(0,0);
        }

        //Scenario 2: bucket encloses participant range
        // in this dimension
        if (this.contains(intervalRange)) {

            int candidate1 = intervalRange.low - low;
            int candidate2 = high - intervalRange.high;

            if (candidate1 > candidate2) {

                return new IntervalRange(low, intervalRange.low);
            } else {

                return new IntervalRange(intervalRange.high, high);
            }

        }
        //Scenario 3: default case
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

        return "intervalRange: " + low + "-" + high + "\n";
    }

    public JSONObject toJSON() {
        JSONObject range = new JSONObject();
        range.put("low", low);
        range.put("high", high);

//        JSONObject intervalRange = new JSONObject();
//        intervalRange.put("type", "intervalRange");
//        intervalRange.put("value", range);

        return range;
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

        System.out.println("Range " + myRange.toString() + "contains range "
                + testRange1.toString() + ":" + res1);
        System.out.println("Range " + myRange.toString() + "contains range "
                + testRange2.toString() + ":" + res2);

        //Test intersection method
        testRange1 = new IntervalRange(2,7);
        testRange2 = new IntervalRange(0,8);
        IntervalRange testRange3 = new IntervalRange(8,10);


        IntervalRange intersection1 = myRange.intersection(
                testRange1);
        IntervalRange intersection2 = myRange.intersection(
                testRange2);
        IntervalRange intersection3 = myRange.intersection(
                testRange3);

        System.out.println("Intersection of range " + myRange.toString() +
                "and range " + testRange1.toString() + ": " + intersection1);
        System.out.println("Intersection of range " + myRange.toString() +
                "and range " + testRange2.toString() + ":" + intersection2);
        System.out.println("Intersection of range " + myRange.toString() +
                "and range " + testRange3.toString() + ":" + intersection3);

        //Test minus
        IntervalRange b = new IntervalRange(5,10);
        IntervalRange p1 = new IntervalRange(4,6);
        IntervalRange p2 = new IntervalRange(8,11);
        IntervalRange p3 = new IntervalRange(7,9);
        IntervalRange p4 = new IntervalRange(4,11);


        System.out.println(b + " minus " + p1 + " = " + b.minus(p1));
        System.out.println(b + " minus " + p2 + " = " + b.minus(p2));
        System.out.println(b + " minus " + p3 + " = " + b.minus(p3));
        System.out.println(b + " minus " + p4 + " = " + b.minus(p4));

        //Test tightRange
        IntervalRange r1 = new IntervalRange(14,20);

        System.out.println("Tight range of " + b + " and " +
                p2 + " is " + b.tightRange(p2));
        System.out.println("Tight range of " + b + " and " +
                r1 + " is " + b.tightRange(r1));


    }
}
