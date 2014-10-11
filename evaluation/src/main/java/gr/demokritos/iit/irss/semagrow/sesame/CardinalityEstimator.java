package gr.demokritos.iit.irss.semagrow.sesame;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.TupleExpr;

/**
 * Created by angel on 10/11/14.
 */
public interface CardinalityEstimator {

    public long getCardinality(TupleExpr expr, BindingSet bindings);

}
