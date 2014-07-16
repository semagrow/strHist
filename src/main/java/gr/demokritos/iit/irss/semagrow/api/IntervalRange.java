package gr.demokritos.iit.irss.semagrow.api;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Defines a set of numbers that lies on an interval
 * Created by angel on 7/12/14.
 */
public class IntervalRange implements RangeLength<Integer> {

    private int low;

    private int high;

    public IntervalRange(int low, int high) {
        this.low = low;
        this.high = high;
    }

    public boolean contains(Integer item) {
        return (item >= low) && (item <= high);
    }

    public boolean contains(Range<Integer> range) {

        if (range instanceof  IntervalRange) {
            return (((IntervalRange) range).getLow() >= low) &&
                    (((IntervalRange) range).getHigh() <= high);
        }
        return false;
    }

    public Range<Integer> intersect(Range<Integer> range) {

        if (range instanceof IntervalRange) {
            IntervalRange res;

            int nLow = max(low, ((IntervalRange) range).getLow());
            int nHigh = min(high, ((IntervalRange) range).getHigh());

            if (nLow <= nHigh) {
                res = new IntervalRange(nLow, nHigh);
            } else {
                res = null;
            }
            return res;
        }
        return range;
    }

    public Range<Integer> union(Range<Integer> range) {

        if (range instanceof IntervalRange) {
            IntervalRange res;

            int nLow = min(low, ((IntervalRange) range).getLow());
            int nHigh = max(high, ((IntervalRange) range).getHigh());
            res = new IntervalRange(nLow, nHigh);
            return res;
        }
        return range;
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


        IntervalRange intersection1 = (IntervalRange) myRange.intersect(
                testRange1);
        IntervalRange intersection2 = (IntervalRange) myRange.intersect(
                testRange2);
        IntervalRange intersection3 = (IntervalRange) myRange.intersect(
                testRange3);

        System.out.println("Intersection of range " + myRange.toString() +
                " and range " + testRange1.toString() + ":" + intersection1);
        System.out.println("Intersection of range " + myRange.toString() +
                " and range " + testRange2.toString() + ":" + intersection2);
        System.out.println("Intersection of range " + myRange.toString() +
                " and range " + testRange3.toString() + ":" + intersection3);

        //Test union method
        IntervalRange union1 = (IntervalRange) myRange.union(testRange1);
        IntervalRange union2 = (IntervalRange) myRange.union(testRange2);
        IntervalRange union3 = (IntervalRange) myRange.union(testRange3);

        System.out.println("Union of range " + myRange.toString() + " and " +
                "range " + testRange1.toString() + ":" + union1);
        System.out.println("Union of range " + myRange.toString() + " and " +
                "range " + testRange2.toString() + ":" + union2);
        System.out.println("Union of range " + myRange.toString() + " and " +
                "range " + testRange3.toString() + ":" + union3);
    }
}
