package gr.demokritos.iit.irss.semagrow.qfr;

import gr.demokritos.iit.irss.semagrow.file.MaterializationHandle;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by nickozoulis on 24/10/2014.
 */
public class SerialQueryLogRecord implements Serializable, QueryLogRecord {

    private static final long serialVersionUID = 3274204530379085447L;
    private transient QueryLogRecord queryLogRecord;

    public SerialQueryLogRecord(){}

    public SerialQueryLogRecord(QueryLogRecord queryLogRecord) {
        this.queryLogRecord = queryLogRecord;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(queryLogRecord.getSession());
        out.writeObject(queryLogRecord.getEndpoint());
        out.writeObject(queryLogRecord.getBindingNames());
        out.writeObject(queryLogRecord.getCardinality());
        out.writeObject(queryLogRecord.getStartTime());
        out.writeObject(queryLogRecord.getEndTime());
        out.writeObject(queryLogRecord.getDuration());

        writeTupleExpr(out, queryLogRecord.getQuery());

        // TODO: out.writeObject(queryLogRecord.getResult());
    }

    private static void writeTupleExpr(java.io.ObjectOutputStream out, TupleExpr query) throws IOException {
        if (query instanceof Projection) {
            Projection proj = (Projection)query;

            // Write projectionElemList size in order to know inside readObject()
            // how many ProjectionElem the loop must expect.
            out.writeObject(proj.getProjectionElemList().getElements().size());

            for (ProjectionElem projElem : proj.getProjectionElemList().getElements())
                out.writeObject(projElem.getSourceName());

            if (proj.getArg() instanceof StatementPattern) {
                StatementPattern sp = (StatementPattern)proj.getArg();

                Var subjVar = sp.getSubjectVar();
                out.writeObject(subjVar.getName());
                out.writeObject(subjVar.getValue());

                Var predVar = sp.getPredicateVar();
                out.writeObject(predVar.getName());
                out.writeObject(predVar.getValue());

                Var objVar = sp.getObjectVar();
                out.writeObject(objVar.getName());
                out.writeObject(objVar.getValue());
            }
        }
    }

    private static TupleExpr readTupleExpr(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        // The size of the ProjectionElemList
        int size = (int)in.readObject();

        ProjectionElemList projElemList = new ProjectionElemList();
        for (int i=0; i<size; i++)
            projElemList.addElement(new ProjectionElem((String)in.readObject()));

        StatementPattern sp = new StatementPattern();
        sp.setSubjectVar(new Var((String)in.readObject(), (Value)in.readObject()));
        sp.setPredicateVar(new Var((String) in.readObject(), (Value) in.readObject()));
        sp.setObjectVar(new Var((String) in.readObject(), (Value) in.readObject()));

        return new Projection(sp, projElemList);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        UUID uuid = (UUID)in.readObject();
        URI endpoint = (URI)in.readObject();
        List<String> bindingNames = (List<String>)in.readObject();
        long cardinality = (long)in.readObject();
        Date startTime = (Date)in.readObject();
        Date endTime = (Date)in.readObject();
        long duration = (long)in.readObject();

        TupleExpr query = readTupleExpr(in);

        this.queryLogRecord = new QueryLogRecordImpl(uuid, endpoint, query , bindingNames);
        this.queryLogRecord.setCardinality(cardinality);
        this.queryLogRecord.setDuration(startTime.getTime(), endTime.getTime());
        //TODO: this.queryLogRecord.setResults();
    }

    public QueryLogRecord getQueryLogRecord() {
        return queryLogRecord;
    }

    @Override
    public URI getEndpoint() {
        return getQueryLogRecord().getEndpoint();
    }

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
    public void setResults(MaterializationHandle handle) {
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
    public MaterializationHandle getResults() {
        return getQueryLogRecord().getResults();
    }
}
