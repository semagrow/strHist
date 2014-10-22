package gr.demokritos.iit.irss.semagrow.sesame;

import gr.demokritos.iit.irss.semagrow.api.Histogram;
import gr.demokritos.iit.irss.semagrow.file.FileManager;
import gr.demokritos.iit.irss.semagrow.file.ResultMaterializationManager;
import gr.demokritos.iit.irss.semagrow.qfr.QueryLogException;
import gr.demokritos.iit.irss.semagrow.qfr.QueryLogFactory;
import gr.demokritos.iit.irss.semagrow.qfr.QueryLogHandler;
import gr.demokritos.iit.irss.semagrow.qfr.RDFQueryLogFactory;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.*;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailConnectionBase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by angel on 10/11/14.
 */
public class TestSailConnection extends SailConnectionBase {

    private TestSail sail;
    private RepositoryConnection conn;

    private QueryLogHandler handler;
    private ResultMaterializationManager manager;

    public TestSailConnection(TestSail sailBase) throws RepositoryException {
        super(sailBase);
        sail = sailBase;
        conn = sail.getRepositoryConnection();
        handler = getQueryLogHandler();
        manager = getMateralizationManager();
    }

    private QueryLogHandler getQueryLogHandler() {

        QueryLogHandler handler;

        File qfrLog  = new File("/var/tmp/qfr.log");
        RDFFormat rdfFF = RDFFormat.NTRIPLES;

        RDFWriterRegistry writerRegistry = RDFWriterRegistry.getInstance();
        RDFWriterFactory rdfWriterFactory = writerRegistry.get(rdfFF);
        QueryLogFactory factory = new RDFQueryLogFactory(rdfWriterFactory);
        try {
            OutputStream out = new FileOutputStream(qfrLog, true);
            handler = factory.getQueryRecordLogger(out);
            return handler;
        } catch (FileNotFoundException e) {

        }
        return null;
    }

    private ResultMaterializationManager getMateralizationManager(){
        File baseDir = new File("/var/tmp/");
        TupleQueryResultFormat resultFF = TupleQueryResultFormat.TSV;

        TupleQueryResultWriterRegistry registry = TupleQueryResultWriterRegistry.getInstance();
        TupleQueryResultWriterFactory writerFactory = registry.get(resultFF);
        return new FileManager(baseDir, writerFactory);
    }

    @Override
    protected void closeInternal() throws SailException {
        try {
            conn.close();
            if (handler != null)
                handler.endQueryLog();
        } catch (RepositoryException | QueryLogException e) {
            throw new SailException(e);
        }
    }

    @Override
    protected CloseableIteration<? extends BindingSet, QueryEvaluationException>
        evaluateInternal(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings, boolean b) throws SailException {

        Histogram<RDFRectangle> hist = getHistogram();
        CardinalityEstimator card = new CardinalityEstimatorImpl(hist);
        CostEstimator cost = new CostEstimatorImpl(card);

        QueryOptimizer opt = new JoinOptimizer(cost, card);

        tupleExpr = new QueryRoot(tupleExpr);

        opt.optimize(tupleExpr, dataset, bindings);

        try {
            EvaluationStrategyImpl evalStrategy = new EvaluationStrategyImpl(conn, handler, manager);
            return evalStrategy.evaluate(tupleExpr, bindings);
        }catch(QueryEvaluationException e) {
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
        return sail.getHistogram();
    }
}
