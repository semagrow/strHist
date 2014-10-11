package gr.demokritos.iit.irss.semagrow.sesame;

import gr.demokritos.iit.irss.semagrow.api.Histogram;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.*;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailBase;
import org.openrdf.sail.helpers.SailConnectionBase;

/**
 * Created by angel on 10/11/14.
 */
public class TestSailConnection extends SailConnectionBase {

    private TestSail sail;

    public TestSailConnection(TestSail sailBase) {
        super(sailBase);
        sail = sailBase;
    }

    @Override
    protected void closeInternal() throws SailException {

    }

    @Override
    protected CloseableIteration<? extends BindingSet, QueryEvaluationException>
        evaluateInternal(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings, boolean b) throws SailException {

        Histogram<RDFRectangle> hist = getHistogram();
        CardinalityEstimator card = new CardinalityEstimatorImpl(hist);
        CostEstimator cost = new CostEstimatorImpl(card);

        QueryOptimizer opt = new JoinOptimizer(cost);

        opt.optimize(tupleExpr, dataset, bindings);

        try {
            RepositoryConnection cnx = sail.getRepositoryConnection();
            EvaluationStrategyImpl evalStrategy = new EvaluationStrategyImpl(cnx);
            return evalStrategy.evaluate(tupleExpr, bindings);
        }catch(QueryEvaluationException e) {
            throw new SailException(e);
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    @Override
    protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal() throws SailException {
        return null;
    }

    @Override
    protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(Resource resource, URI uri, Value value, boolean b, Resource... resources) throws SailException {
        return null;
    }

    @Override
    protected long sizeInternal(Resource... resources) throws SailException {
        return 0;
    }

    @Override
    protected void startTransactionInternal() throws SailException {

    }

    @Override
    protected void commitInternal() throws SailException {

    }

    @Override
    protected void rollbackInternal() throws SailException {

    }

    @Override
    protected void addStatementInternal(Resource resource, URI uri, Value value, Resource... resources) throws SailException {

    }

    @Override
    protected void removeStatementsInternal(Resource resource, URI uri, Value value, Resource... resources) throws SailException {

    }

    @Override
    protected void clearInternal(Resource... resources) throws SailException {

    }

    @Override
    protected CloseableIteration<? extends Namespace, SailException> getNamespacesInternal() throws SailException {
        return null;
    }

    @Override
    protected String getNamespaceInternal(String s) throws SailException {
        return null;
    }

    @Override
    protected void setNamespaceInternal(String s, String s2) throws SailException {

    }

    @Override
    protected void removeNamespaceInternal(String s) throws SailException {

    }

    @Override
    protected void clearNamespacesInternal() throws SailException {

    }

    public Histogram<RDFRectangle> getHistogram() {
        return null;
    }
}
