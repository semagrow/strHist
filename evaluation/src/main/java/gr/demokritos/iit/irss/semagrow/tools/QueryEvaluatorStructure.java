package gr.demokritos.iit.irss.semagrow.tools;

import org.openrdf.query.algebra.TupleExpr;

/**
 * Created by katerina on 28/9/2015.
 */
public interface QueryEvaluatorStructure {

    long getResultCount();

    void setResultCount(long count);

    TupleExpr getPlan();

    void setPlan(TupleExpr tupleExpr);

    void setTime(long time);

    long getTime();
}
