package gr.demokritos.iit.irss.semagrow.base.range;

import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.api.range.Rangeable;

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
	public boolean includes(String item) {

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

        if (isInfinite())
            return true;

        //for estimation
        if (range.isInfinite())
            return false;

        boolean contained = true;

        for (String p : range.getPrefixList()) {

            boolean b = false;
            for (String otherP : prefixList) {
                if (p.startsWith(otherP)) {
                    b = true;
                    continue;
                }
            }

            if (!b)
                return false;
            else
                contained = contained && b;
        }

        return contained;
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

        // If any of the 2 lists is empty, just return a new Range containing the other one.
        if (this.getPrefixList().isEmpty())
            return new PrefixRange(prefixRange.getPrefixList());
        else if (prefixRange.getPrefixList().isEmpty())
            return new PrefixRange(this.getPrefixList());

        ArrayList<String> prefixListN = new ArrayList<String>();

        for (String myP : prefixList) {

            for (String otherP : prefixRange.getPrefixList()) {



                if ((myP.startsWith(otherP))) {

                    if (!prefixListN.contains(otherP) ) {

                        prefixListN.add(otherP);
                    }
                } else if (otherP.startsWith(myP)) {

                    if (!prefixListN.contains(myP) ) {

                        prefixListN.add(myP);
                    }
                } else {
                    if (!prefixListN.contains(otherP) ) {

                        prefixListN.add(otherP);
                    }
                    if (!prefixListN.contains(myP) ) {

                        prefixListN.add(myP);
                    }
                }
            }
        }
       /* prefixListN.addAll(prefixList);

        for (String p : prefixRange.prefixList ) {

            if (!this.includes(p)) {

                prefixListN.add(p);
            }
        } */

        return new PrefixRange(prefixListN);
    }


    public void expand(String v) {

        ArrayList<String> prefixN = new ArrayList<String>();
        int cnt = 0;

        if (prefixList.isEmpty() && isInfinite()) {
            prefixList.add(v);
            infinite = false;
        }

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

        boolean contained = false;

        ArrayList<String> intersectionPrefixList = new ArrayList<String>();

        for (String myP : prefixList) {

            for (String otherP : range.getPrefixList()) {

                if ((myP.startsWith(otherP))) {

                    for (String p : intersectionPrefixList) {

                        if (myP.startsWith(p)) {

                            contained = true;
                        }
                    }
                    if (!contained) {

                        intersectionPrefixList.add(myP);
                    }

                } else if (otherP.startsWith(myP)) {

                    for (String p : intersectionPrefixList) {

                        if (otherP.startsWith(p)) {

                            contained = true;
                        }
                    }
                    if (!contained) {

                        intersectionPrefixList.add(otherP);
                    }
                }
            }
        }


        return new PrefixRange(intersectionPrefixList);
    }

    //Tested
    public PrefixRange minus(PrefixRange prefixRange) {

        //todo: infinite?

        ArrayList<String> prefixN = new ArrayList<String>(prefixList);


        // if this is a subset then return empty range
        if (prefixRange.contains(this))
            return new PrefixRange(new ArrayList<String>());

        // else compute the difference
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

        if (infinite)
            return false;

        return (prefixList.size() == 1);
    }

    public boolean isEmpty() {
        return !isInfinite() && prefixList.isEmpty();
    }

    public ArrayList<String> getPrefixList() {
        return prefixList;
    }

    public boolean isInfinite() {

        return infinite;
    }

    //Tested
    public String toString() {

        String res="";

        if (prefixList.size() > 1)
            res += "{";

        boolean comma = false;

        for (String p : prefixList) {

            if (comma)
                res += ",";
            res = "\"" + p + "\"";

            comma = true;
        }
        if (prefixList.size() > 1)
            res += "}";

        return res;
    }

    public long getLength() {
        if (infinite) return Integer.MAX_VALUE;
        return prefixList.size();
    }

    public boolean equals(Object o) {
        if (this == o) return true;

        if (o instanceof PrefixRange) {
            PrefixRange range = (PrefixRange)o;
            //FIXME: Maybe list's equals is not what we need. Consider changing prefixList to Set
            boolean p = this.prefixList.equals(range.prefixList);
            return this.infinite == range.infinite && p;
        }

        return false;
    }
}
