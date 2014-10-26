package gr.demokritos.iit.irss.semagrow.base.range;

import gr.demokritos.iit.irss.semagrow.base.range.ExplicitSetRange;
import junit.framework.*;

import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

public class ExplicitSetRangeTest extends TestCase {

    protected ExplicitSetRange<String> predicates;
    protected String s1, s2, s3, s4;

    @Override
    protected void setUp() {
        Set<String> set = new HashSet<String>();
        s1 = "http://www.semagrow.eu/rdf/year";
        s2 = "http://purl.org/dc/terms/subject";
        s3 = "http://purl.org/dc/terms/author";
        s4 = "http://www.semagrow.eu/rdf/issuedby";
        set.add(s1);
        set.add(s2);
        set.add(s3);
        set.add(s4);
        predicates = new ExplicitSetRange<String>(set);
    }

    public void testIncludes() throws Exception {
        assertTrue(predicates.includes("http://www.semagrow.eu/rdf/year"));
    }

    public void testIncludesNot() throws Exception {
        assertFalse(predicates.includes("http://www.semagrow.eu/rdf/exampleissuedby"));
    }

    public void testContains() throws Exception {
        Set<String> set2 = new HashSet<String>();
        set2.add(s1);
        set2.add(s4);
        ExplicitSetRange<String> predicates2 = new ExplicitSetRange<String>(set2);
        assertTrue(predicates.contains(predicates2));
    }

    public void testContainsNot() throws Exception {
        Set<String> set2 = new HashSet<String>();
        set2.add(s1);
        set2.add(s4);
        ExplicitSetRange<String> predicates2 = new ExplicitSetRange<String>(set2);
        assertFalse(predicates2.contains(predicates));
    }

    public void testIntersects() throws Exception {
        Set<String> set2 = new HashSet<String>();
        set2.add(s1);
        set2.add(s4);
        ExplicitSetRange<String> predicates2 = new ExplicitSetRange<String>(set2);
        assertTrue(predicates.intersects(predicates2));
    }

    public void testIntersectsNot() throws Exception {
        Set<String> set2 = new HashSet<String>();
        set2.add("examplestring");
        ExplicitSetRange<String> predicates2 = new ExplicitSetRange<String>(set2);
        assertFalse(predicates.intersects(predicates2));
    }

    public void testTightRange() throws Exception {
        Set<String> set2 = new HashSet<String>();
        set2.add(s1);
        set2.add(s4);
        set2.add("examplestring");
        ExplicitSetRange<String> predicates2 = new ExplicitSetRange<String>(set2);

        ExplicitSetRange<String> tightRange = predicates.tightRange(predicates2);
        assertTrue(tightRange.getItems().size() == 5);
    }

    public void testExpand() throws Exception {
        // expand() just adds the String into the set.
    }

    public void testIntersection() throws Exception {
        Set<String> set2 = new HashSet<String>();
        set2.add(s1);
        set2.add(s4);
        ExplicitSetRange<String> predicates2 = new ExplicitSetRange<String>(set2);

        ExplicitSetRange<String> intersection = predicates.intersection(predicates2);

        assertTrue(intersection.getItems().size() == 2);
    }

    public void testMinusSize() throws Exception {
        Set<String> set2 = new HashSet<String>();
        set2.add(s1);
        set2.add(s4);
        set2.add("examplestring");
        ExplicitSetRange<String> predicates2 = new ExplicitSetRange<String>(set2);

        ExplicitSetRange<String> minus = predicates.minus(predicates2);

        assertTrue(minus.getItems().size() == 2);
    }

    public void testMinusContents() throws Exception {
        Set<String> set2 = new HashSet<String>();
        set2.add(s1);
        set2.add(s4);
        set2.add("examplestring");
        ExplicitSetRange<String> predicates2 = new ExplicitSetRange<String>(set2);

        ExplicitSetRange<String> minus = predicates.minus(predicates2);

        assertTrue(minus.getItems().contains(s2));
        assertTrue(minus.getItems().contains(s3));
    }

    public void testIsUnit() throws Exception {
        // isUnit just true if the size of the set is equals to 1.
    }

    public void testIsInfinite() throws Exception {
        // isInfinite just returns the value of a boolean variable.
    }

    public void testIsEmpty1() throws Exception {
        assertFalse(predicates.isEmpty());
    }

    public void testIsEmpty2() throws Exception {
        ExplicitSetRange<String> setRange = new ExplicitSetRange<String>();
        assertFalse(setRange.isEmpty());
    }

    public void testEquals() throws Exception {
        assertTrue(predicates.equals(predicates));
    }

    public void testEqualsNot() throws Exception {
        ExplicitSetRange setRange = new ExplicitSetRange(predicates.getItems());
        setRange.getItems().add("examplestring");
        assertFalse(predicates.equals(setRange));
    }

}