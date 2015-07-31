package gr.demokritos.iit.irss.semagrow.sesame;

import eu.semagrow.stack.modules.sails.semagrow.optimizer.Plan;
import gr.demokritos.iit.irss.semagrow.file.ResultMaterializationManager;
import eu.semagrow.querylog.api.QueryLogHandler;
import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.sail.federation.evaluation.RepositoryTripleSource;

/**
 * Created by angel on 10/11/14.
 */
public class EvaluationStrategyImpl extends org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl {


    private QueryLogInterceptor interceptor;
//    private ObservingInterceptor interceptor;
    private URI endpoint = ValueFactoryImpl.getInstance().createURI("http://histogramnamespace/example");

    private QueryLogHandler queryLogHandler;
    private ResultMaterializationManager materializationManager;

    public EvaluationStrategyImpl(final RepositoryConnection cnx, QueryLogHandler handler, ResultMaterializationManager manager) {
        super(new RepositoryTripleSource(cnx));
        queryLogHandler = handler;
        materializationManager = manager;
        interceptor = new QueryLogInterceptor(queryLogHandler, materializationManager);
//        interceptor = new ObservingInterceptor(endpoint.toString());
    }

    public CloseableIteration<BindingSet, QueryEvaluationException>
        evaluate(TupleExpr expr, BindingSet bindings) throws QueryEvaluationException {

        if (expr instanceof Plan)
            expr = ((Plan)expr).getArg();

        return super.evaluate(expr, bindings);
    }

    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException>
        evaluate(StatementPattern pattern, BindingSet bindings) throws QueryEvaluationException {

        return interceptor.afterExecution(endpoint, pattern, bindings, super.evaluate(pattern, bindings));
    }

}
