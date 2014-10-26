package gr.demokritos.iit.irss.semagrow.base.range;

import junit.framework.TestCase;

public class IntervalRangeTest extends TestCase {

    public void testIncludes() throws Exception {
        IntervalRange myRange = new IntervalRange(1,6);
        assertTrue(myRange.includes(2));
    }

    public void testIncludes2() throws Exception {
        IntervalRange myRange = new IntervalRange(1,6);
        assertFalse(myRange.includes(7));
    }

    public void testContains() throws Exception {
        IntervalRange myRange = new IntervalRange(1,6);
        IntervalRange testRange1 = new IntervalRange(3,5);
        assertTrue(myRange.contains(testRange1));
    }

    public void testContains2() throws Exception {
        IntervalRange myRange = new IntervalRange(1,6);
        IntervalRange testRange2 = new IntervalRange(1,8);
        assertFalse(myRange.contains(testRange2));
    }

    public void testIntersection1() throws Exception {
        IntervalRange myRange = new IntervalRange(1,6);
        IntervalRange testRange1 = new IntervalRange(2,7);

        IntervalRange intersection1 = myRange.intersection(testRange1);

        assertTrue(myRange.intersects(testRange1));
        assertTrue(intersection1.equals(new IntervalRange(2,6)));

        /*
        IntervalRange testRange2 = new IntervalRange(0,8);

        IntervalRange intersection2 = myRange.intersection(testRange2);

        IntervalRange testRange3 = new IntervalRange(8,10);
        IntervalRange intersection3 = myRange.intersection(testRange3);
        */
    }

    public void testIntersection2() throws Exception {

        IntervalRange myRange = new IntervalRange(1,6);
        IntervalRange testRange2 = new IntervalRange(0,8);

        IntervalRange intersection2 = myRange.intersection(testRange2);

        assertTrue(myRange.intersects(testRange2));
        assertTrue(intersection2.equals(new IntervalRange(1,6)));
    }

    public void testIntersection3() throws Exception {

        IntervalRange myRange = new IntervalRange(1,6);
        IntervalRange testRange3 = new IntervalRange(8,10);
        IntervalRange intersection3 = myRange.intersection(testRange3);

        assertFalse(myRange.intersects(testRange3));
        assertTrue(intersection3.isEmpty());
    }

    public void testTightRange() throws Exception {

    }

    public void testExpand() throws Exception {

    }


    public void testMinus() throws Exception {
        /*
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
        */
    }

    public void testIsUnit() throws Exception {

    }

    public void testIsEmpty() throws Exception {

    }

    public void testGetLength() throws Exception {

    }
}