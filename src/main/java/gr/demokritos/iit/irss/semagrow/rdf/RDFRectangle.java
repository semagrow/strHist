package gr.demokritos.iit.irss.semagrow.rdf;

import gr.demokritos.iit.irss.semagrow.api.*;

import java.util.List;

/**
 * Created by angel on 7/15/14.
 */
public class RDFRectangle implements Rectangle<RDFRectangle> {

    private PrefixRange subjectRange;

    private ExplicitSetRange<String> predicateRange;

    private Range<Object> objectRange;

    public int getDimensionality() { return 3; }

    @Override
    public RDFRectangle intersection(RDFRectangle rec) {
        return null;
    }

    @Override
    public boolean contains(RDFRectangle rec) {
        return false;
    }

    public boolean equals(RDFRectangle rec) {
        return false;
    }

    public Range<?> getRange(int i) {
        return null;
    }

}
