package gr.demokritos.iit.irss.semagrow.rdf;

import gr.demokritos.iit.irss.semagrow.api.Range;
import gr.demokritos.iit.irss.semagrow.api.Rangeable;
import org.openrdf.model.Value;

/**
 * Created by angel on 7/15/14.
 */
public class RDFLiteralRange
        implements Range<Value>, Rangeable<RDFLiteralRange>
{

    public boolean isUnit() {
        return false;
    }

    public RDFLiteralRange intersection(RDFLiteralRange rect) {
        return null;
    }

    public boolean contains(RDFLiteralRange rect) {
        return false;
    }
}
