package gr.demokritos.iit.irss.semagrow.sesame;

import org.openrdf.query.*;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.queryrender.sparql.SPARQLQueryRenderer;
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
        long c = 0;

        if (expr instanceof Projection)
            expr = ((Projection)expr).getArg();

        ParsedTupleQuery parsedQuery = new ParsedTupleQuery(expr);
        SPARQLQueryRenderer renderer = new SPARQLQueryRenderer();

        try {

            String queryString = "SELECT (count(*) AS c) WHERE " + renderer.render(parsedQuery);


            TupleQuery query = cnx.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            TupleQueryResult res = query.evaluate();

            if (res.hasNext())
                c = Long.parseLong(res.next().getBinding("card").getValue().stringValue());

        } catch (NumberFormatException e) {e.printStackTrace();
        } catch (RepositoryException e) {e.printStackTrace();
        } catch (MalformedQueryException e) {e.printStackTrace();
        } catch (QueryEvaluationException e) {e.printStackTrace();
        } catch (Exception e) {e.printStackTrace();}

        return c;
    }

}
