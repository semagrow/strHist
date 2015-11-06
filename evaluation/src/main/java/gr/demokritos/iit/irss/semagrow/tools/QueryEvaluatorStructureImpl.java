package gr.demokritos.iit.irss.semagrow.tools;

import org.openrdf.query.algebra.TupleExpr;

/**
 * Created by katerina on 28/9/2015.
 */

public class QueryEvaluatorStructureImpl implements QueryEvaluatorStructure {
    private long count = 0;
    private TupleExpr plan;
    private long time;

    @Override
    public long getResultCount() {
        return count;
    }

    @Override
    public void setResultCount(long count) {
        this.count = count;
    }

    @Override
    public TupleExpr getPlan() {
        return plan;
    }

    @Override
    public void setPlan(TupleExpr tupleExpr) {
        this.plan = tupleExpr;
    }

    @Override
    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public long getTime() {
        return this.time;
    }
}
