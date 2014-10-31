package gr.demokritos.iit.irss.semagrow.qfr;

import gr.demokritos.iit.irss.semagrow.api.range.Range;
import info.aduna.iteration.FilterIteration;
import info.aduna.iteration.Iteration;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;

/**
 * Created by angel on 10/23/14.
 */
public abstract class RangeFilterIteration<T> extends FilterIteration<BindingSet, QueryEvaluationException> {

    private Range<T> range;
    private String bindingName;

    public RangeFilterIteration(String bindingName, Range<T> range, Iteration<BindingSet, QueryEvaluationException> iter) {
        super(iter);

        this.range = range;
        this.bindingName = bindingName;
    }

    @Override
    protected boolean accept(BindingSet bindings) throws QueryEvaluationException {

        Binding b = bindings.getBinding(bindingName);
        if (b != null) {
            Value v = b.getValue();
            T convertedValue = convertValue(v);
            if (convertedValue != null)
                return range.includes(convertedValue);
        }
        return false;
    }

    protected abstract T convertValue(Value v);
}
