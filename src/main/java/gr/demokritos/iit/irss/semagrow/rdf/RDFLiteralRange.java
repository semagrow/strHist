package gr.demokritos.iit.irss.semagrow.rdf;

import gr.demokritos.iit.irss.semagrow.api.Range;
import gr.demokritos.iit.irss.semagrow.api.Rangeable;

/**
 * Created by angel on 7/15/14.
 */
public class RDFLiteralRange
        implements Range<Object>, Rangeable<RDFLiteralRange>
{


    @Override
    public long getLength() {
        return 0;
    }

    @Override
    public boolean isUnit() {
        return false;
    }

    @Override
    public RDFLiteralRange intersection(RDFLiteralRange rect) {
        return null;
    }

    @Override
    public boolean contains(RDFLiteralRange rect) {
        return false;
    }
}
