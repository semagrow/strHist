package gr.demokritos.iit.irss.semagrow.qfr;

import eu.semagrow.stack.modules.sails.semagrow.helpers.FilterCollector;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryResult;
import gr.demokritos.iit.irss.semagrow.base.Stat;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;

import java.util.Collection;
import java.util.List;

/**
 * Created by angel on 10/22/14.
 */
public class QueryRecordAdapter implements QueryRecord<RDFRectangle, Stat> {

    private QueryLogRecord queryLogRecord;

    public QueryRecordAdapter(QueryLogRecord queryLogRecord) {
        this.queryLogRecord = queryLogRecord;
    }

    @Override
    public String getQuery() {
        return null;
    }

    @Override
    public RDFRectangle getRectangle() {

        Collection<StatementPattern> patterns = StatementPatternCollector.process(queryLogRecord.getQuery());
        Collection<ValueExpr> filters = FilterCollector.process(queryLogRecord.getQuery());

        if (patterns.size() == 1)
        {

        }

        return null;
    }

    @Override
    public QueryResult<RDFRectangle, Stat> getResultSet() {
        return new QueryResultImpl();
    }

    private class QueryResultImpl implements QueryResult<RDFRectangle, Stat> {

        @Override
        public Stat getCardinality(RDFRectangle rect) {
            return null;
        }

        @Override
        public List<RDFRectangle> getRectangles(RDFRectangle rect) {
            return null;
        }
    }

}
