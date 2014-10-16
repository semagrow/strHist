package gr.demokritos.iit.irss.semagrow.sesame;

import org.openrdf.query.*;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

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

        String[] exprNames = (String[])expr.getBindingNames().toArray();

        String s, p, o = "";

        if (bindings.hasBinding(exprNames[0]))
            s = (bindings.getBinding(exprNames[0])).getValue().stringValue();
        else
            s = "?" + exprNames[0];

        if (bindings.hasBinding(exprNames[1]))
            p = (bindings.getBinding(exprNames[1])).getValue().stringValue();
        else
            p = "?" + exprNames[1];

        if (bindings.hasBinding(exprNames[2]))
            o = (bindings.getBinding(exprNames[2])).getValue().stringValue();
        else
            o = "?" + exprNames[2];

        String q = "select count(*) as card { " + s + " " + p + " " + o + " .}";
        long card = 0;

        TupleQuery query = null;
        TupleQueryResult res = null;

        try {
            query = cnx.prepareTupleQuery(QueryLanguage.SPARQL, q);
            res = query.evaluate();

            if (res.hasNext())
                card = Long.parseLong(res.next().getBinding("card").getValue().stringValue());

        } catch (NumberFormatException e) {e.printStackTrace();
        } catch (RepositoryException e) {e.printStackTrace();
        } catch (MalformedQueryException e) {e.printStackTrace();
        } catch (QueryEvaluationException e) {e.printStackTrace();}

        return card;
    }

}
