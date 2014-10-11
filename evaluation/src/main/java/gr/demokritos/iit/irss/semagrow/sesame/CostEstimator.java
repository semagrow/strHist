package gr.demokritos.iit.irss.semagrow.sesame;

import org.openrdf.query.algebra.TupleExpr;

/**
 * Created by angel on 10/11/14.
 */
public interface CostEstimator {

    public double getCost(TupleExpr tupleExpr);

}
