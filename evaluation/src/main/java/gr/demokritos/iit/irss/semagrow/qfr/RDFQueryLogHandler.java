package gr.demokritos.iit.irss.semagrow.qfr;

import gr.demokritos.iit.irss.semagrow.rdf.io.vocab.QFR;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import java.util.List;
import java.util.UUID;

/**
 * Created by angel on 10/20/14.
 */
public class RDFQueryLogHandler implements QueryLogHandler {

    private RDFHandler handler;
    private ValueFactory vf;

    public RDFQueryLogHandler(RDFHandler handler, ValueFactory vf) {
        this.handler = handler;
        this.vf = vf;
    }

    @Override
    public void startQueryLog() throws QueryLogException {
        try {
            handler.startRDF();
        } catch (RDFHandlerException e) {
            throw new QueryLogException(e);
        }
    }


    @Override
    public void handleQueryRecord(QueryLogRecord queryLogRecord) throws QueryLogException {
        URI qrId = vf.createURI("urn:" + UUID.randomUUID().toString());
        handleQueryRecord(qrId, queryLogRecord);
    }

    private void createStatement(Resource subject, URI predicate, Value object)
            throws QueryLogException
    {
        try {
            handler.handleStatement(vf.createStatement(subject, predicate, object));
        } catch (RDFHandlerException e) {
            throw new QueryLogException(e);
        }
    }

    private Resource createBNodeStatement(Resource subject, URI predicate)
            throws QueryLogException
    {
        Resource q = vf.createBNode();
        createStatement(subject, predicate, q);
        return q;
    }

    private void handleQueryRecord(Resource record, QueryLogRecord qr) throws QueryLogException {

        createStatement(record, RDF.TYPE, QFR.QUERYRECORD);
        createStatement(record, QFR.SESSION, vf.createURI("urn:"+qr.getSession()));
        createStatement(record, QFR.ENDPOINT, qr.getEndpoint());
        createStatement(record, QFR.RESULTFILE, qr.getResults().getId());
        createStatement(record, QFR.CARDINALITY, vf.createLiteral(qr.getCardinality()));
        createStatement(record, QFR.START, vf.createLiteral(qr.getStartTime()));
        createStatement(record, QFR.END, vf.createLiteral(qr.getEndTime()));
        createStatement(record, QFR.DURATION, vf.createLiteral(qr.getDuration()));

        Resource q = createBNodeStatement(record, QFR.QUERY);
        handleTupleExpr(q, qr.getQuery(), EmptyBindingSet.getInstance());
    }

    private void handleTupleExpr(Resource r, TupleExpr expr, BindingSet bindings)
            throws QueryLogException
    {
        List<StatementPattern> patterns = StatementPatternCollector.process(expr);

        for (StatementPattern p : patterns) {
            Resource r1 = createBNodeStatement(r, QFR.PATTERN);
            handleStatementPattern(r1, p);
        }
    }

    private void handleStatementPattern(Resource r, StatementPattern pattern)
            throws QueryLogException
    {
        //createStatement(r, pattern.getSubjectVar().getValue());
    }

    private void handleBindingSet(Resource r, BindingSet bs)
            throws QueryLogException
    {

    }

    private void handleBinding(Resource r, Binding binding)
            throws QueryLogException
    {

    }

    @Override
    public void endQueryLog() throws QueryLogException {
        try {
            handler.endRDF();
        } catch (RDFHandlerException e) {
            throw new QueryLogException(e);
        }
    }

}
