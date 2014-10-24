package gr.demokritos.iit.irss.semagrow.qfr;

import gr.demokritos.iit.irss.semagrow.api.range.Range;
import info.aduna.iteration.Iteration;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;

/**
 * Created by angel on 10/23/14.
 */
public class StringRangeFilterIteration extends RangeFilterIteration<String> {

    public StringRangeFilterIteration(String bindingName, Range<String> range, Iteration<BindingSet, QueryEvaluationException> iter) {
        super(bindingName, range, iter);
    }

    @Override
    protected String convertValue(Value v) {
        return v.stringValue();
    }
}
