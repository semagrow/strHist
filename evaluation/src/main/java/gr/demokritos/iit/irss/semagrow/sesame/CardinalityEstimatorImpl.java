package gr.demokritos.iit.irss.semagrow.sesame;

import gr.demokritos.iit.irss.semagrow.api.Histogram;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;

/**
 * Created by angel on 10/11/14.
 */
public class CardinalityEstimatorImpl implements CardinalityEstimator {

    private Histogram<RDFRectangle> histogram;

    public CardinalityEstimatorImpl(Histogram<RDFRectangle> histogram) {
        this.histogram = histogram;
    }

    public long getCardinality(TupleExpr tupleExpr, BindingSet bindings) {
        if (tupleExpr instanceof StatementPattern)
            return getCardinality((StatementPattern) tupleExpr, bindings);
        else
            return 0;
    }

    public long getCardinality(StatementPattern pattern, BindingSet bindings) {

        return histogram.estimate(toRectangle(pattern, bindings));
    }

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
