package gr.demokritos.iit.irss.semagrow.qfr;

import eu.semagrow.stack.modules.sails.semagrow.helpers.FilterCollector;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryResult;
import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.base.Stat;
import gr.demokritos.iit.irss.semagrow.base.range.CalendarRange;
import gr.demokritos.iit.irss.semagrow.base.range.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import gr.demokritos.iit.irss.semagrow.base.range.PrefixRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFLiteralRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
import org.openrdf.query.algebra.helpers.VarNameCollector;

import java.util.*;

/**
 * Created by angel on 10/22/14.
 */
public class QueryRecordAdapter implements QueryRecord<RDFRectangle, Stat> {

    private QueryLogRecord queryLogRecord;

    private RDFRectangle rectangle;

    private StatementPattern pattern;

    public QueryRecordAdapter(QueryLogRecord queryLogRecord) {
        this.queryLogRecord = queryLogRecord;
    }

    @Override
    public String getQuery() {
        return null;
    }

    @Override
    public RDFRectangle getRectangle() {

        if (rectangle == null)
            rectangle = computeQueryRectangle(queryLogRecord.getQuery());

        return rectangle;
    }

    private RDFRectangle computeQueryRectangle(TupleExpr expr) {

        Collection<StatementPattern> patterns = StatementPatternCollector.process(expr);
        Collection<ValueExpr> filters = FilterCollector.process(expr);

        if (patterns.size() == 1)
        {
            pattern = patterns.iterator().next();
            RDFRectangle rect = computePatternRectangle(pattern, filters);

            return rect;
        }

        return null;
    }

    private RDFRectangle computePatternRectangle(StatementPattern pattern, Collection<ValueExpr> filters) {
        Value sVal = pattern.getSubjectVar().getValue();
        Value pVal = pattern.getPredicateVar().getValue();
        Value oVal = pattern.getObjectVar().getValue();

        PrefixRange sRange = new PrefixRange();
        ExplicitSetRange<String> pRange = new ExplicitSetRange<String>();
        RDFLiteralRange oRange = new RDFLiteralRange();

        if (sVal != null) {
            ArrayList<String> singletonSubject = new ArrayList<String>();
            singletonSubject.add(sVal.stringValue());
            sRange = new PrefixRange(singletonSubject);
        }

        if (pVal != null) {
            ArrayList<String> singletonPredicate = new ArrayList<String>();
            singletonPredicate.add(pVal.stringValue());
            pRange = new ExplicitSetRange<String>(singletonPredicate);
        }

        if (oVal != null) {
            if (oVal instanceof Literal) {
                Literal oLit = (Literal)oVal;
                oRange = new RDFLiteralRange(oLit.getDatatype(), computeObjectRange(oLit));
            }
        } else  {
            // oVal is a variable but can have filters
            oRange = computeObjectRange(pattern.getObjectVar(), filters);
        }

        return new RDFRectangle(sRange, pRange, oRange);
    }

    private RangeLength<?> computeObjectRange(Literal lit) {
        if (lit.getDatatype() == XMLSchema.INT) {
            int val = lit.intValue();
            return new IntervalRange(val,val);
        } else if (lit.getDatatype() == XMLSchema.DATE) {
            Date val = lit.calendarValue().toGregorianCalendar().getTime();
            return new CalendarRange(val,val);
        }
        else {
            //return new PrefixRange(lit.stringValue());
            //FIXME
            return null;
        }
    }

    private RDFLiteralRange computeObjectRange(Var var, Collection<ValueExpr> filters) {
        //FIXME
        Collection<ValueExpr> relevantFilters = findRelevantFilters(var, filters);

        ValueExpr high = null, low = null;

        for (ValueExpr e : relevantFilters) {
            if (e instanceof Compare) {
                Compare c = (Compare)e;
                if (c.getOperator().equals(Compare.CompareOp.LE))
                {
                    if (c.getLeftArg().equals(var))
                        high = c.getRightArg();
                    else if (c.getRightArg().equals(var))
                        low = c.getLeftArg();
                } else if (c.getOperator().equals(Compare.CompareOp.GE)) {
                    if (c.getRightArg().equals(var))
                        high = c.getLeftArg();
                    else if (c.getLeftArg().equals(var))
                        low = c.getRightArg();
                }
            }
        }

        /*
        if (low != null && high != null &&
            low instanceof Var && high instanceof Var)
        {
            Value lowVal = ((Var)low).getValue();
            Value highVal = ((Var)high).getValue();

            if (lowVal != null && highVal != null &&
                lowVal instanceof Literal && highVal instanceof Literal) {

            }
        }
        */

        return null;
    }

    private Collection<ValueExpr> findRelevantFilters(Var var, Collection<ValueExpr> filters) {

        Collection<ValueExpr> relevant = new LinkedList<ValueExpr>();

        for (ValueExpr e : filters) {
            Set<String> varnames = VarNameCollector.process(e);
            if (varnames.contains(var.getName()))
                relevant.add(e);
        }

        return relevant;
    }

    @Override
    public QueryResult<RDFRectangle, Stat> getResultSet() {
        return new QueryResultImpl();
    }

    private class QueryResultImpl implements QueryResult<RDFRectangle, Stat> {

        @Override
        public Stat getCardinality(RDFRectangle rect) {

            CloseableIteration<BindingSet,QueryEvaluationException> iter = getResult();
            iter = filter(rect, iter);
            return null;
        }

        private CloseableIteration<BindingSet, QueryEvaluationException>
            filter(RDFRectangle rectangle, CloseableIteration<BindingSet, QueryEvaluationException> iter) {



            return iter;
        }

        private CloseableIteration<BindingSet, QueryEvaluationException>
            getResult() {

            return null;
        }

        @Override
        public List<RDFRectangle> getRectangles(RDFRectangle rect) {
            return null;
        }
    }

}
