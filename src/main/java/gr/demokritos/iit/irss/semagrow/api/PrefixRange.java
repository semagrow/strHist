package gr.demokritos.iit.irss.semagrow.api;

import java.util.ArrayList;

/**
 * Defines a subset of all the strings that have a common prefix.
 * Created by angel on 7/12/14.
 */
public class PrefixRange
        implements RangeLength<String>, Rangeable<PrefixRange> {

    private ArrayList<String> prefixList;
    private boolean infinite = false;

    public PrefixRange(ArrayList<String> prefix) {
        this.prefixList = prefix;
    }

    // Construct an infinite prefix range
    public PrefixRange() {

        this.prefixList = new ArrayList<String>();
		infinite = true;
	}

    //Tested
	public boolean contains(String item) {

        if (infinite) return true;

        for (String p : prefixList) {

            if (item.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    //Tested
    public boolean contains(PrefixRange range) {

        if (infinite) return true;

        for (String p : prefixList) {

            for (String otherP : range.getPrefixList()) {

                if (otherP.startsWith(p)) {
                    return true;
                }
            }
        }

        return false;
    }

   //Tested
    public boolean intersects(PrefixRange range) {

        if (infinite) return true;

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

    //Tested
    public PrefixRange tightRange(PrefixRange prefixRange) {

        if (infinite) return new PrefixRange();

        ArrayList<String> prefixListN = new ArrayList<String>();
        prefixListN.addAll(prefixList);

        for (String p : prefixRange.prefixList ) {

            if (!this.contains(p)) {

                prefixListN.add(p);
            }
        }

        return new PrefixRange(prefixListN);
    }


    public void expand(String v) {

        ArrayList<String> prefixN = new ArrayList<String>();
        int cnt = 0;

        // find longest common prefix with
        // each string in prefix range
        for (String p : prefixList) {

             prefixN.add(p);

            int j = 0;
            for(; j < Math.min(prefixN.get(cnt).length(), v.length()); ++j) {
                if(prefixN.get(cnt).charAt(j) != v.charAt(j)) {
                    break;
                }
            }
            prefixN.set(cnt, (prefixN.get(cnt)).substring(0, j));
        }

        //find the longest of these common prefixes
        String largest = prefixN.get(0);
        int d = 0; //prefix idx
        for (int i = 0; i < prefixN.size(); i++) {

            if ( prefixN.get(i).length() > largest.length() ) {

                largest = prefixN.get(i);
                d = i;
            }
        }

        // and update corresponding prefix
        prefixList.set(d,prefixN.get(d));
    }

    //Tested
    public PrefixRange intersection(PrefixRange range) {

        if (infinite) return range;

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

    //Tested
    public PrefixRange minus(PrefixRange prefixRange) {

        //todo: infinite?

        ArrayList<String> prefixN = new ArrayList<String>(prefixList);


        if (prefixList.contains(this)) {

            return new PrefixRange(new ArrayList<String>());
        }

        for (String myP : prefixList) {

            for (String otherP : prefixRange.prefixList) {

                if (otherP.startsWith(myP)) {

                    prefixN.remove(myP);
                }
            }
        }


        return new PrefixRange(prefixN);
    }

    public boolean isUnit() {

        if (infinite) return false;

        return (prefixList.size() == 1);
    }

    public ArrayList<String> getPrefixList() {
        return prefixList;
    }

    public boolean isInfinite() {

        return infinite;
    }

    //Tested
    public String toString() {

        String res;

        res = "uriprefixes: { ";

        for (String p : prefixList) {

            res = res + p + " ";
        }

        res += "}";
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
        if (myRange.intersects(testRange3)) {
            System.out.println("Intersection of range " + myRange.toString() +
                    " and range " + testRange3.toString() + ": " + intersection3);
        }

        testRange3PrefixList.add("http://c");
        System.out.println(new PrefixRange(testRange3PrefixList));

        //Test tight range
        PrefixRange testRange4 = new PrefixRange(testRange3PrefixList);
        System.out.println("Tight range of " + myRange + " and " + testRange4 +
                " = " + myRange.tightRange(testRange4));

        //Test minus
        PrefixRange testRange5 = myRange.tightRange(testRange4);
        System.out.println(testRange5 + " minus " + testRange1 + " = " +
                testRange5.minus(testRange1));
    }

    public long getLength() {

        if (infinite) return Integer.MAX_VALUE;
        return prefixList.size();
    }
}
