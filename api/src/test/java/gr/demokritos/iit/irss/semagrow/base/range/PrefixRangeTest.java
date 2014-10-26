package gr.demokritos.iit.irss.semagrow.base.range;

import junit.framework.TestCase;

import java.util.ArrayList;

public class PrefixRangeTest extends TestCase {

    public void testIncludesIncludedSubstring() throws Exception {

        ArrayList<String> myRangePrefixList = new ArrayList<String>();
        myRangePrefixList.add("http://a/");
        PrefixRange myRange = new PrefixRange(myRangePrefixList);
        String item1 = "http://a/b/c/d";
        assertTrue(myRange.includes(item1));
    }

    public void testIncludesIncludedIdentity() throws Exception {

        ArrayList<String> myRangePrefixList = new ArrayList<String>();
        String item1 = "http://a/";
        myRangePrefixList.add(item1);
        PrefixRange myRange = new PrefixRange(myRangePrefixList);
        assertTrue(myRange.includes(item1));
    }

    public void testIncludesNotIncluded() throws Exception {

        ArrayList<String> myRangePrefixList = new ArrayList<String>();
        myRangePrefixList.add("http://a/");
        PrefixRange myRange = new PrefixRange(myRangePrefixList);
        String item2 = "http://b/c";
        assertFalse(myRange.includes(item2));
    }

    public void testContains() throws Exception {
        ArrayList<String> myRangePrefixList = new ArrayList<String>();
        String item1 = "http://a/";
        myRangePrefixList.add(item1);
        PrefixRange myRange = new PrefixRange(myRangePrefixList);

        ArrayList<String> testRange1PrefixList = new ArrayList<String>();
        testRange1PrefixList.add("http://a/b");
        PrefixRange testRange1 = new PrefixRange(testRange1PrefixList);

        assertTrue(myRange.contains(testRange1));
    }

    public void testContainsNotContained() throws Exception {
        ArrayList<String> myRangePrefixList = new ArrayList<String>();
        String item1 = "http://a/";
        myRangePrefixList.add(item1);
        PrefixRange myRange = new PrefixRange(myRangePrefixList);

        ArrayList<String> testRange2PrefixList = new ArrayList<String>();
        testRange2PrefixList.add("http://b");

        PrefixRange testRange2 = new PrefixRange(testRange2PrefixList);

        assertFalse(myRange.contains(testRange2));
    }

    public void testIntersection() throws Exception {

        ArrayList<String> myRangePrefixList = new ArrayList<String>();
        String item1 = "http://a/";
        myRangePrefixList.add(item1);
        PrefixRange myRange = new PrefixRange(myRangePrefixList);

        ArrayList<String> testRange1PrefixList = new ArrayList<String>();
        testRange1PrefixList.add("http://a/b");
        PrefixRange testRange1 = new PrefixRange(testRange1PrefixList);

        PrefixRange result = myRange.intersection(testRange1);
        assertTrue(myRange.intersects(testRange1));
        assertTrue(result.equals(testRange1));
    }

    public void testIntersection2() throws Exception {
        ArrayList<String> myRangePrefixList = new ArrayList<String>();
        String item1 = "http://a/";
        myRangePrefixList.add(item1);
        PrefixRange myRange = new PrefixRange(myRangePrefixList);

        ArrayList<String> testRange2PrefixList = new ArrayList<String>();
        testRange2PrefixList.add("http://");

        PrefixRange testRange2 = new PrefixRange(testRange2PrefixList);

        PrefixRange result = myRange.intersection(testRange2);
        assertTrue(myRange.intersects(testRange2));
        assertTrue(result.equals(myRange));
    }

    public void testIntersection3() throws Exception {

        ArrayList<String> myRangePrefixList = new ArrayList<String>();
        String item1 = "http://a/";
        myRangePrefixList.add(item1);
        PrefixRange myRange = new PrefixRange(myRangePrefixList);

        ArrayList<String> testRange3PrefixList = new ArrayList<String>();
        testRange3PrefixList.add("http://b");
        PrefixRange testRange3 = new PrefixRange(testRange3PrefixList);

        assertFalse(myRange.intersects(testRange3));

        PrefixRange result = myRange.intersection(testRange3);
        assertTrue(result.isEmpty());
        assertFalse(result.isInfinite());
    }

    public void testTightRange() throws Exception {

        ArrayList<String> myRangePrefixList = new ArrayList<String>();
        myRangePrefixList.add("http://a/");
        PrefixRange myRange = new PrefixRange(myRangePrefixList);

        ArrayList<String> testRange3PrefixList = new ArrayList<String>();
        testRange3PrefixList.add("http://b");
        testRange3PrefixList.add("http://c");
        PrefixRange testRange3 = new PrefixRange(testRange3PrefixList);

        PrefixRange result = myRange.tightRange(testRange3);

        assertTrue(result.contains(myRange));
        assertTrue(result.contains(testRange3));
    }

    public void testExpand() throws Exception {

    }

    public void testMinus() throws Exception {
        ArrayList<String> myRangePrefixList = new ArrayList<String>();
        myRangePrefixList.add("http://a/");
        PrefixRange myRange = new PrefixRange(myRangePrefixList);

        ArrayList<String> testRange3PrefixList = new ArrayList<String>();
        testRange3PrefixList.add("http://b");
        testRange3PrefixList.add("http://c");
        PrefixRange testRange3 = new PrefixRange(testRange3PrefixList);

        PrefixRange result = myRange.tightRange(testRange3);

        assertTrue(result.contains(myRange));
        assertTrue(result.contains(testRange3));

        //result.minus(testRange1);
    }

    public void testIsUnit() throws Exception {

    }

    public void testIsEmpty() throws Exception {

    }

    public void testIsInfinite() throws Exception {

    }
}