package gr.demokritos.iit.irss.semagrow.base.range;

import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import junit.framework.*;

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

    public void testTightRange1() throws Exception {
        IntervalRange ir1 = new IntervalRange(1, 2);
        IntervalRange ir2 = new IntervalRange(3, 4);

        IntervalRange tightRange = ir1.tightRange(ir2);
        assertTrue(tightRange.equals(new IntervalRange(1, 4)));
    }

    public void testTightRange2() throws Exception {
        IntervalRange ir1 = new IntervalRange(1, 1);
        IntervalRange ir2 = new IntervalRange(1, 4);

        IntervalRange tightRange = ir1.tightRange(ir2);
        assertTrue(tightRange.equals(new IntervalRange(1, 4)));
    }

    public void testTightRange3() throws Exception {
        IntervalRange ir1 = new IntervalRange(1, 3);
        IntervalRange ir2 = new IntervalRange(2, 4);

        IntervalRange tightRange = ir1.tightRange(ir2);
        assertTrue(tightRange.equals(new IntervalRange(1, 4)));
    }

    public void testTightRange4() throws Exception {
        IntervalRange ir1 = new IntervalRange(1, 5);
        IntervalRange ir2 = new IntervalRange(3, 4);

        IntervalRange tightRange = ir1.tightRange(ir2);
        assertTrue(tightRange.equals(new IntervalRange(1, 5)));
    }

    public void testTightRange5() throws Exception {
        IntervalRange ir1 = new IntervalRange(1, 4);
        IntervalRange ir2 = new IntervalRange(3, 5);

        IntervalRange tightRange = ir1.tightRange(ir2);
        assertTrue(tightRange.equals(new IntervalRange(1, 5)));
    }

    public void testTightRange6() throws Exception {
        IntervalRange ir1 = new IntervalRange(1, 1);
        IntervalRange ir2 = new IntervalRange(1, 1);

        IntervalRange tightRange = ir1.tightRange(ir2);
        assertTrue(tightRange.equals(new IntervalRange(1, 1)));
    }

    public void testExpand1() throws Exception {
        IntervalRange ir = new IntervalRange(1, 1);
        ir.expand(1);
        assertTrue(ir.equals(new IntervalRange(1, 1)));
    }

    public void testExpand2() throws Exception {
        IntervalRange ir = new IntervalRange(1, 1);
        ir.expand(-1);
        assertTrue(ir.equals(new IntervalRange(-1, 1)));
    }

    public void testExpand3() throws Exception {
        IntervalRange ir = new IntervalRange(1, 2);
        ir.expand(3);
        assertTrue(ir.equals(new IntervalRange(1, 3)));
    }

    /**
     * Scenario 1: Participant includes bucket in this dimension.
     * @throws Exception
     */
    public void testMinus1() throws Exception {
        IntervalRange ir = new IntervalRange(5, 10);
        IntervalRange ir2 = new IntervalRange(4, 11);

        IntervalRange minus = ir.minus(ir2);
        assertTrue(minus.equals(new IntervalRange(1, 0)));
    }

    /**
     * Scenario 2: Bucket encloses participant range in this dimension.
     * @throws Exception
     */
    public void testMinus2() throws Exception {
        IntervalRange ir = new IntervalRange(5, 10);
        IntervalRange ir2 = new IntervalRange(6, 8);

        IntervalRange minus = ir.minus(ir2);
        assertTrue(minus.equals(new IntervalRange(9, 10)));
    }

    /**
     * Scenario 3: Default Case
     * @throws Exception
     */
    public void testMinus3() throws Exception {
        IntervalRange ir = new IntervalRange(5, 10);
        IntervalRange ir2 = new IntervalRange(8, 11);

        IntervalRange minus = ir.minus(ir2);
        assertTrue(minus.equals(new IntervalRange(5, 7)));
    }

    /**
     * Scenario 3: Default Case
     * @throws Exception
     */
    public void testMinus4() throws Exception {
        IntervalRange ir = new IntervalRange(5, 10);
        IntervalRange ir2 = new IntervalRange(4, 6);

        IntervalRange minus = ir.minus(ir2);
        assertTrue(minus.equals(new IntervalRange(7, 10)));
    }



    public void testIsUnit() throws Exception {

    }

    public void testIsEmpty() throws Exception {

    }

    public void testGetLength() throws Exception {

    }
}