package gr.demokritos.iit.irss.semagrow.sesame;

import eu.semagrow.stack.modules.api.estimator.CostEstimator;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;

/**
 * Created by angel on 10/11/14.
 */
public class JoinOptimizer implements QueryOptimizer {



    public JoinOptimizer(CostEstimator estimator) {

    }

    @Override
    public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {

        // optimize the order of joins.
        // use always nested loop joins.

    }
}
