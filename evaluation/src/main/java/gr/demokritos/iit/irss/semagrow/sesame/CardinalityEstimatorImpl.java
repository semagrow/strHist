package gr.demokritos.iit.irss.semagrow.sesame;

import gr.demokritos.iit.irss.semagrow.api.Histogram;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.*;

/**
 * Created by angel on 10/11/14.
 */
public class CardinalityEstimatorImpl implements CardinalityEstimator {

    private Histogram<RDFRectangle> histogram;

    public CardinalityEstimatorImpl(Histogram<RDFRectangle> histogram) {
        this.histogram = histogram;
    }

    public long getCardinality(TupleExpr expr, BindingSet bindings) {

        if (expr instanceof StatementPattern)
            return getCardinality((StatementPattern)expr, bindings);
        else if (expr instanceof Union)
            return getCardinality((Union)expr, bindings);
        else if (expr instanceof Filter)
            return getCardinality((Filter)expr, bindings);
        else if (expr instanceof Projection)
            return getCardinality((Projection)expr, bindings);
        else if (expr instanceof Slice)
            return getCardinality((Slice)expr, bindings);
        else if (expr instanceof Join)
            return getCardinality((Join)expr, bindings);
        else if (expr instanceof LeftJoin)
            return getCardinality((LeftJoin)expr, bindings);
        else
            return 0;
    }

    public long getCardinality(Slice slice, BindingSet bindings) {
        long card = getCardinality(slice.getArg(), bindings);
        return Math.min(card, slice.getLimit());
    }

    public long getCardinality(Join join, BindingSet bindings) {
        long card1 = getCardinality(join.getLeftArg(), bindings);
        long card2 = getCardinality(join.getRightArg(), bindings);
        double sel = 0.5;
        return (long)(card1 * card2 * sel);
    }

    public long getCardinality(LeftJoin join, BindingSet bindings) {
        long card1 = getCardinality(join.getLeftArg(), bindings);
        long card2 = getCardinality(join.getRightArg(), bindings);
        double sel = 0.5;
        return Math.max(card1, (long)(card1 * card2 * sel));
    }

    public long getCardinality(Filter filter, BindingSet bindings) {
        long card = getCardinality(filter.getArg(), bindings);
        return (long)(card * 0.5);
    }

    public long getCardinality(UnaryTupleOperator op, BindingSet bindings) {
        return getCardinality(op, bindings);
    }

    public long getCardinality(Union union, BindingSet bindings) {
        return getCardinality(union.getLeftArg(), bindings) + getCardinality(union.getRightArg(), bindings);
    }

    public long getCardinality(StatementPattern pattern, BindingSet bindings) {

        return histogram.estimate(toRectangle(pattern, bindings));
    }

    //TODO
    private RDFRectangle toRectangle(StatementPattern pattern, BindingSet bindings) {
        Value sVal = pattern.getPredicateVar().getValue();
        Value pVal = pattern.getPredicateVar().getValue();
        Value oVal = pattern.getPredicateVar().getValue();

        //TODO: (zoulis) create the appropriate RDFRectangle based on the values of pattern
        //      AND the bindings.
        //      If variable then we should leave the corresponding dimension infinite.
        //new RDFRectangle();
        return null;
    }

}
