package gr.demokritos.iit.irss.semagrow.api;

import java.util.ArrayList;

/**
 * Defines a subset of all the strings that have a common prefix.
 * Created by angel on 7/12/14.
 */
public class PrefixRange
        implements Range<String>, Rangeable<PrefixRange> {

    private ArrayList<String> prefixList;

    public PrefixRange(ArrayList<String> prefix) {
        this.prefixList = prefix;
    }

    public boolean contains(String item) {

        for (String p : prefixList) {

            if (item.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(PrefixRange range) {

        for (String p : prefixList) {

            for (String otherP : range.getPrefixList()) {

                if (otherP.startsWith(p)) {
                    return true;
                }
            }
        }

        return false;
    }

   
    public boolean intersects(PrefixRange range) {

        for (String myP : prefixList) {

            for (String otherP : range.getPrefixList()) {

                if ((myP.startsWith(otherP) ||
                        (otherP.startsWith(myP)))) {

                    return true;
                }
            }
        }

        return false;
    }

    public PrefixRange intersection(PrefixRange range) {

        ArrayList<String> intersectionPrefixList = new ArrayList<String>();

        for (String myP : prefixList) {

            for (String otherP : range.getPrefixList()) {

                if ((myP.startsWith(otherP))) {

                    intersectionPrefixList.add(myP);

                } else if (otherP.startsWith(myP)) {

                    intersectionPrefixList.add(otherP);
                }
            }
        }


        return new PrefixRange(intersectionPrefixList);
    }

    public boolean isUnit() { return (prefixList.size() == 1); }

    public ArrayList<String> getPrefixList() {
        return prefixList;
    }

    public String toString() {

        String res;

        res = "[";

        for (String p : prefixList) {

            res = res + p + " ";
        }

        res += "]";
        return res;
    }

    public static void main(String [] args){

        ArrayList<String> myRangePrefixList = new ArrayList<String>();
        myRangePrefixList.add("http://a/");
        PrefixRange myRange = new PrefixRange(myRangePrefixList);
        String item1 = "http://a/b/c/d";
        String item2 = "http://b/c";
        boolean res1 = myRange.contains(item1);
        boolean res2 = myRange.contains(item2);

        // Test getters
        System.out.println("My range is " + myRange.getPrefixList());
        //Test contains item method
        if (res1)
            System.out.println("My range contains " + item1);
        else
            System.out.println("Test failed");

        if (!res2 )
            System.out.println("My range does not contain " + item2);
        else
            System.out.println("Test failed");
        //Test contains range method
        ArrayList<String> testRange1PrefixList = new ArrayList<String>();
        ArrayList<String> testRange2PrefixList = new ArrayList<String>();
        testRange1PrefixList.add("http://a/b");
        testRange2PrefixList.add("http://b");
        PrefixRange testRange1 = new PrefixRange(testRange1PrefixList);
        PrefixRange testRange2 = new PrefixRange(testRange2PrefixList);

        res1 = myRange.contains(testRange1);
        res2 = myRange.contains(testRange2);

        System.out.println("Range " + myRange.toString() + " contains range "
                + testRange1.toString() + ": " + res1);
        System.out.println("Range " + myRange.toString() + " contains range "
                + testRange2.toString() + ": " + res2);

        //Test intersection method
        testRange2PrefixList.clear();
        testRange2PrefixList.add("http://");
        testRange2 = new PrefixRange(testRange2PrefixList);
        ArrayList<String> testRange3PrefixList = new ArrayList<String>();
        testRange3PrefixList.add("http://b");
        PrefixRange testRange3 = new PrefixRange(testRange3PrefixList);


        PrefixRange intersection1 = myRange.intersection(
                testRange1);
        PrefixRange intersection2 = myRange.intersection(
                testRange2);
        PrefixRange intersection3 = myRange.intersection(
                testRange3);

        System.out.println("Intersection of range " + myRange.toString() +
                " and range " + testRange1.toString() + ": " + intersection1);
        System.out.println("Intersection of range " + myRange.toString() +
                " and range " + testRange2.toString() + ": " + intersection2);
        System.out.println("Intersection of range " + myRange.toString() +
                " and range " + testRange3.toString() + ": "  + intersection3);

    }

	public PrefixRange minus(PrefixRange r) {
		// TODO Auto-generated method stub
		return null;
	}
}
