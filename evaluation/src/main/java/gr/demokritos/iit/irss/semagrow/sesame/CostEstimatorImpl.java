package gr.demokritos.iit.irss.semagrow.sesame;

import eu.semagrow.stack.modules.sails.semagrow.optimizer.Plan;
import org.openrdf.query.algebra.*;
import org.openrdf.query.impl.EmptyBindingSet;

/**
 * Created by angel on 10/11/14.
 */
public class CostEstimatorImpl implements CostEstimator {

    private CardinalityEstimator cardinalityEstimator;

    public CostEstimatorImpl(CardinalityEstimator cardEst) {
        this.cardinalityEstimator = cardEst;
    }

    @Override
    public double getCost(TupleExpr tupleExpr) {
        if (tupleExpr instanceof StatementPattern)
            return getCost((StatementPattern)tupleExpr);
        else if (tupleExpr instanceof Join)
            return getCost((Join)tupleExpr);
        else if (tupleExpr instanceof Union)
            return getCost((Union)tupleExpr);
        else if (tupleExpr instanceof Plan)
            return getCost((Plan)tupleExpr);
        else if (tupleExpr instanceof UnaryTupleOperator)
            return getCost((UnaryTupleOperator)tupleExpr);
        else if (tupleExpr instanceof BinaryTupleOperator)
            return getCost((BinaryTupleOperator)tupleExpr);
        else
            return 0;
    }

    public double getCost(Plan plan) {
        return plan.getCost();
    }

    public double getCost(StatementPattern pattern) {
        return cardinalityEstimator.getCardinality(pattern, EmptyBindingSet.getInstance());
    }

    public double getCost(Join join) {
        long leftCard = cardinalityEstimator.getCardinality(join.getLeftArg(), EmptyBindingSet.getInstance());
        return getCost(join.getLeftArg()) + leftCard*getCost(join.getRightArg());
    }


    public double getCost(Union union) {
        return cardinalityEstimator.getCardinality(union, EmptyBindingSet.getInstance()) +
               getCost(union.getLeftArg()) + getCost(union.getRightArg());
    }

    public double getCost(UnaryTupleOperator op) {
        return getCost(op.getArg());
    }

    public double getCost(BinaryTupleOperator op) {
        return getCost(op.getLeftArg()) + getCost(op.getRightArg());
    }
}
