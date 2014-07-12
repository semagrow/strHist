package gr.demokritos.iit.irss.semagrow.api;

/**
 * Defines a subset of all the strings that have a common prefix.
 * Created by angel on 7/12/14.
 */
public class PrefixRange implements Range<String> {

    @Override
    public boolean contains(String item) {
        return false;
    }

    @Override
    public boolean contains(Range<String> range) {
        return false;
    }

    @Override
    public Range<String> intersect(Range<String> range) {
        return null;
    }

    @Override
    public Range<String> union(Range<String> range) {
        return null;
    }
}
