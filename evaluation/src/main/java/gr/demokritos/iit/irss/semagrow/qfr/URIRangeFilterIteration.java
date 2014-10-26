package gr.demokritos.iit.irss.semagrow.qfr;

import gr.demokritos.iit.irss.semagrow.api.range.Range;
import info.aduna.iteration.Iteration;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;

/**
 * Created by angel on 10/26/14.
 */
public class URIRangeFilterIteration extends RangeFilterIteration<URI> {

    public URIRangeFilterIteration(String bindingName, Range<URI> range, Iteration<BindingSet, QueryEvaluationException> iter) {
        super(bindingName, range, iter);
    }

    @Override
    protected URI convertValue(Value v) {
        if (v instanceof URI)
            return (URI)v;
        else
            return null;
    }
}