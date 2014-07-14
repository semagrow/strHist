package gr.demokritos.iit.irss.semagrow.api;

/**
 * Defines a subset of all the strings that have a common prefix.
 * Created by angel on 7/12/14.
 */
public class PrefixRange implements Range<String> {

    private String prefix;

    public PrefixRange(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean contains(String item) {
        return item.startsWith(prefix);
    }

    @Override
    public boolean contains(Range<String> range) {

        if (range instanceof PrefixRange) {
            return ((PrefixRange) range).getPrefix().startsWith(prefix);
        }
        return false;
    }

    @Override
    public Range<String> intersect(Range<String> range) {


        if (range instanceof PrefixRange) {
            String otherPrefix = ((PrefixRange) range).getPrefix();

            if (prefix.startsWith(otherPrefix)) {
                return new PrefixRange(prefix);
            } else if (otherPrefix.startsWith(prefix)) {
                return new PrefixRange(otherPrefix);
            }
        }

        return range;
    }

    @Override
    public Range<String> union(Range<String> range) {

        if (range instanceof PrefixRange) {

            String otherPrefix = ((PrefixRange) range).getPrefix();

            if (prefix.startsWith(otherPrefix)) {
                return new PrefixRange(otherPrefix);
            } else if (otherPrefix.startsWith(prefix)) {
                return new PrefixRange(prefix);
            }
            //TODO Union of non intersecting prefixes
            // Return CollectionRange or allow PrefixRange to be a list of prefixes?
        }
        return range;
    }


    /*@Override
    public long getLength(){
        //if we use list of prefixes
        //return list.size();
    }*/

    public String getPrefix() {
        return prefix;
    }

    public String toString() {
        return prefix;
    }

    public static void main(String [] args){
        PrefixRange myRange = new PrefixRange("http://a/");
        String item1 = "http://a/b/c/d";
        String item2 = "http://b/c";
        boolean res1 = myRange.contains(item1);
        boolean res2 = myRange.contains(item2);

        // Test getters
        System.out.println("My range is " + myRange.getPrefix());
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
        PrefixRange testRange1 = new PrefixRange("http://a/b");
        PrefixRange testRange2 = new PrefixRange("http://b");

        res1 = myRange.contains(testRange1);
        res2 = myRange.contains(testRange2);

        System.out.println("Range " + myRange.toString() + " contains range "
                + testRange1.toString() + ": " + res1);
        System.out.println("Range " + myRange.toString() + " contains range "
                + testRange2.toString() + ": " + res2);

        //Test intersection method
        testRange1 = new PrefixRange("http://a/b");
        testRange2 = new PrefixRange("http://");
        PrefixRange testRange3 = new PrefixRange("http://b");


        PrefixRange intersection1 = (PrefixRange) myRange.intersect(
                testRange1);
        PrefixRange intersection2 = (PrefixRange) myRange.intersect(
                testRange2);
        PrefixRange intersection3 = (PrefixRange) myRange.intersect(
                testRange3);

        System.out.println("Intersection of range " + myRange.toString() +
                " and range " + testRange1.toString() + ": " + intersection1);
        System.out.println("Intersection of range " + myRange.toString() +
                " and range " + testRange2.toString() + ": " + intersection2);
        System.out.println("Intersection of range " + myRange.toString() +
                " and range " + testRange3.toString() + ": "  + intersection3);

        //Test union method
        PrefixRange union1 = (PrefixRange) myRange.union(testRange1);
        PrefixRange union2 = (PrefixRange) myRange.union(testRange2);
        PrefixRange union3 = (PrefixRange) myRange.union(testRange3);

        System.out.println("Union of range " + myRange.toString() + " and " +
                "range " + testRange1.toString() + ": " + union1);
        System.out.println("Union of range " + myRange.toString() + " and " +
                "range " + testRange2.toString() + ": " + union2);
        System.out.println("Union of range " + myRange.toString() + " and " +
                "range " + testRange3.toString() + ": " + union3);
    }
}
