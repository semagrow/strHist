package gr.demokritos.iit.irss.semagrow.qfr;

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
public class RDFQueryLogRecordSerialWrapper implements Serializable {

    private static final long serialVersionUID = 2159354069707133543L;

    private transient QueryLogRecord queryLogRecord;

    public RDFQueryLogRecordSerialWrapper(){}

    public RDFQueryLogRecordSerialWrapper(QueryLogRecord queryLogRecord) {
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

            //TODO: Write also ProjectElemList

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
        StatementPattern sp = new StatementPattern();
        sp.setSubjectVar(new Var((String)in.readObject(), (Value)in.readObject()));
        sp.setPredicateVar(new Var((String) in.readObject(), (Value) in.readObject()));
        sp.setObjectVar(new Var((String) in.readObject(), (Value) in.readObject()));

        //TODO: Read also ProjectElemList

        return new Projection(sp);
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

}
