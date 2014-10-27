package gr.demokritos.iit.irss.semagrow.rdf;

import junit.framework.TestCase;

public class RDFRectangleTest extends TestCase {


    public static void main(String args[] ) {
        /*
        ArrayList<String> myRangePrefixList = new ArrayList<String>();
        myRangePrefixList.add("http://a/");
        PrefixRange subjectRange = new PrefixRange(myRangePrefixList);

        HashSet<String> s1 = new HashSet<String>();
        s1.add("a");
        s1.add("b");
        s1.add("c");
        ExplicitSetRange<String> predicateRange = new ExplicitSetRange<String>(s1);

        int low = 0;
        int high = 10;
        RDFLiteralRange objectRange = new RDFLiteralRange(low, high);
        //RDFLiteralRange objectRange2  = new RDFLiteralRange();

        RDFRectangle rect = new RDFRectangle(subjectRange, predicateRange, objectRange);

        RDFLiteralRange objectRange2 = new RDFLiteralRange(low + 1, high);
        RDFRectangle rect2 = new RDFRectangle(subjectRange, predicateRange, objectRange2);
        System.out.println(rect + " and " + rect2 + " are mergeable: " + rect.isMergeable(rect2));

        RDFLiteralRange objectRange3 = new RDFLiteralRange("http://a");
        RDFRectangle rect3 = new RDFRectangle(subjectRange, predicateRange, objectRange3);
        System.out.println(rect + " and " + rect3 + " are mergeable: " + rect.isMergeable(rect3));

        System.out.println(rect.isInfinite());
        */
    }

    public void testIntersection() throws Exception {

    }

    public void testContains() throws Exception {

    }

    public void testEquals() throws Exception {

    }

    public void testIntersects() throws Exception {

    }

    public void testShrink() throws Exception {

    }

    public void testComputeTightBox() throws Exception {

    }

    public void testIsInfinite() throws Exception {

    }

    public void testIsMergeable() throws Exception {

    }

    public void testIsEnclosing() throws Exception {

    }
}