package gr.demokritos.iit.irss.semagrow.qfr;

import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.queryrender.sparql.SPARQLQueryRenderer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by nickozoulis on 24/10/2014.
 */
public class SerialQueryLogRecord implements Serializable, QueryLogRecord {

    private static final long serialVersionUID = -5389996019343297997L;

    private transient QueryLogRecord queryLogRecord;

    public SerialQueryLogRecord(){}

    public SerialQueryLogRecord(QueryLogRecord queryLogRecord) {
        this.queryLogRecord = queryLogRecord;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws Exception {
        out.writeObject(queryLogRecord.getSession());
        out.writeObject(queryLogRecord.getEndpoint());
        out.writeObject(queryLogRecord.getBindingNames());
        out.writeObject(queryLogRecord.getCardinality());
        out.writeObject(queryLogRecord.getStartTime());
        out.writeObject(queryLogRecord.getEndTime());
        out.writeObject(queryLogRecord.getDuration());
        out.writeObject(queryLogRecord.getResults());
        out.writeObject(queryLogRecord.getBindings());

        ParsedTupleQuery q = new ParsedTupleQuery(queryLogRecord.getQuery());
        out.writeObject(new SPARQLQueryRenderer().render(q)); // String
    }

    private void readObject(java.io.ObjectInputStream in) throws Exception {
        UUID uuid = (UUID)in.readObject();
        URI endpoint = (URI)in.readObject();
        List<String> bindingNames = (List<String>) in.readObject();
        long cardinality = (long)in.readObject();
        Date startTime = (Date)in.readObject();
        Date endTime = (Date)in.readObject();
        long duration = (long)in.readObject();
        URI results = (URI)in.readObject();
        BindingSet bindings = (BindingSet)in.readObject();

        ParsedTupleQuery q = QueryParserUtil.parseTupleQuery(QueryLanguage.SPARQL,
                                                            (String)in.readObject(),
                                                            "http://example.org/");
        TupleExpr query = q.getTupleExpr();

        this.queryLogRecord = new QueryLogRecordImpl(uuid, endpoint, query, bindings, bindingNames);
        this.queryLogRecord.setCardinality(cardinality);
        this.queryLogRecord.setDuration(startTime.getTime(), endTime.getTime());
        this.queryLogRecord.setResults(results);
    }



    public QueryLogRecord getQueryLogRecord() {
        return queryLogRecord;
    }

    @Override
    public URI getEndpoint() {
        return getQueryLogRecord().getEndpoint();
    }

    @Override
    public BindingSet getBindings() { return getQueryLogRecord().getBindings(); }

    @Override
    public TupleExpr getQuery() {
        return getQueryLogRecord().getQuery();
    }

    @Override
    public UUID getSession() {
        return getQueryLogRecord().getSession();
    }

    @Override
    public List<String> getBindingNames() {
        return getQueryLogRecord().getBindingNames();
    }

    @Override
    public void setCardinality(long card) {
        getQueryLogRecord().setCardinality(card);
    }

    @Override
    public long getCardinality() {
        return getQueryLogRecord().getCardinality();
    }

    @Override
    public void setDuration(long start, long end) {
        getQueryLogRecord().setDuration(start,end);
    }

    @Override
    public void setResults(URI handle) {
        getQueryLogRecord().setResults(handle);
    }

    @Override
    public Date getStartTime() {
        return getQueryLogRecord().getStartTime();
    }

    @Override
    public Date getEndTime() {
        return getQueryLogRecord().getEndTime();
    }

    @Override
    public long getDuration() {
        return getQueryLogRecord().getDuration();
    }

    @Override
    public URI getResults() {
        return getQueryLogRecord().getResults();
    }
}
