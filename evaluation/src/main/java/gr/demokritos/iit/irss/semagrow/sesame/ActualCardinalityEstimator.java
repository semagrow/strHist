package gr.demokritos.iit.irss.semagrow.sesame;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.repository.RepositoryConnection;

/**
 * Created by angel on 10/11/14.
 */
public class ActualCardinalityEstimator implements CardinalityEstimator {

    private RepositoryConnection cnx;

    public ActualCardinalityEstimator(RepositoryConnection cnx) {
        this.cnx = cnx;
    }

    @Override
    public long getCardinality(TupleExpr expr, BindingSet bindings) {
        return 0;

        // to be filled with code that do a count query to the repository and
        // returns the result.
    }
}
