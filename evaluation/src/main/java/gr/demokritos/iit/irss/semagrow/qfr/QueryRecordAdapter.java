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
import gr.demokritos.iit.irss.semagrow.file.MaterializationHandle;
import gr.demokritos.iit.irss.semagrow.file.ResultMaterializationManager;
import gr.demokritos.iit.irss.semagrow.rdf.RDFLiteralRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iteration;
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

    private Collection<ValueExpr> filters;

    private ResultMaterializationManager fileManager;

    public QueryRecordAdapter(QueryLogRecord queryLogRecord, ResultMaterializationManager fileManager)
        throws IllegalArgumentException
    {
        this.queryLogRecord = queryLogRecord;
        this.fileManager = fileManager;

        TupleExpr expr = queryLogRecord.getQuery();
        Collection<StatementPattern> patterns = StatementPatternCollector.process(expr);
        filters = FilterCollector.process(expr);

        if (patterns.size() != 1)
            throw new IllegalArgumentException("Only single-pattern queries are supported");

        pattern = patterns.iterator().next();

    }

    @Override
    public String getQuery() {
        return null;
    }

    @Override
    public RDFRectangle getRectangle() {

        if (rectangle == null)
            rectangle = computePatternRectangle(pattern, filters);

        return rectangle;
    }

    private List<Var> getDimensions() {
        List<Var> list = new ArrayList<Var>();
        list.add(pattern.getSubjectVar());
        list.add(pattern.getPredicateVar());
        list.add(pattern.getObjectVar());
        return list;
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

    private RDFRectangle computeRectangle(Value s, Value p, Value o) {

        PrefixRange sRange = new PrefixRange();
        ExplicitSetRange<String> pRange = new ExplicitSetRange<String>();
        RDFLiteralRange oRange = new RDFLiteralRange();

        if (s != null) {
            ArrayList<String> subjects = new ArrayList<String>();
            subjects.add(s.stringValue());
            sRange = new PrefixRange(subjects);
        }

        if (p != null) {
            ArrayList<String> predicates = new ArrayList<String>();
            predicates.add(s.stringValue());
            pRange = new ExplicitSetRange<String>(predicates);
        }

        if (o != null) {
            if (o instanceof Literal) {
                Literal l = (Literal)o;
                oRange = new RDFLiteralRange(l.getDatatype(), computeObjectRange(l));
            }
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

        private Stat emptyStat = new Stat();

        @Override
        public Stat getCardinality(RDFRectangle rect) {

            RDFRectangle queryRect = getRectangle();

            if (!queryRect.contains(rect))
                return emptyStat;

            CloseableIteration<BindingSet,QueryEvaluationException> iter = getResult();

            iter = filter(rect, iter);

            try {
                Stat stat =  getStat(iter);
                iter.close();
                return stat;
            } catch (QueryEvaluationException e) { }

            return emptyStat;
        }

        private CloseableIteration<BindingSet, QueryEvaluationException>
            filter(RDFRectangle rectangle, CloseableIteration<BindingSet, QueryEvaluationException> iter)
        {
            if (!pattern.getSubjectVar().hasValue())
                iter = new StringRangeFilterIteration(pattern.getSubjectVar().getName(), rectangle.getSubjectRange(), iter);

            if (!pattern.getPredicateVar().hasValue())
                iter = new StringRangeFilterIteration(pattern.getPredicateVar().getName(), rectangle.getPredicateRange(), iter);

            if (!pattern.getObjectVar().hasValue())
                iter = new ValueRangeFilterIteration(pattern.getObjectVar().getName(), rectangle.getObjectRange(), iter);

            return iter;
        }

        private CloseableIteration<BindingSet, QueryEvaluationException>
            getResult()
        {
            try {
                return fileManager.getResult(queryLogRecord.getResults().getId());
            } catch (QueryEvaluationException e) {
                e.printStackTrace();
            }
            return null;
        }

        private Stat getStat(Iteration<BindingSet, QueryEvaluationException> iter)
                throws QueryEvaluationException
        {
            Stat stat = new Stat();
            long count = 0;
            Map<String, Set<Value>> distinctValues = new HashMap<String,Set<Value>>();

            while (iter.hasNext()) {
                count++;
                BindingSet b = iter.next();

                for (String bindingName : b.getBindingNames()) {
                    if (!distinctValues.containsKey(bindingName)){
                        Set<Value> d = new HashSet<Value>();
                        d.add(b.getValue(bindingName));
                        distinctValues.put(bindingName, d);
                    }
                }
            }

            stat.setFrequency(count);

            ArrayList<Long> distinctCounts = new ArrayList<Long>();

            List<Var> dimensions = getDimensions();

            for (Var dim : dimensions) {

                if (dim.hasValue())
                    distinctCounts.add((long)1);
                else
                {
                    Set<Value> values = distinctValues.get(dim.getName());

                    if (values == null)
                        distinctCounts.add((long) 0);
                    else
                        distinctCounts.add((long) values.size());
                }
            }

            stat.setDistinctCount(distinctCounts);

            return stat;
        }

        @Override
        public List<RDFRectangle> getRectangles(RDFRectangle rect) {
            return getRectangles();
        }

        public List<RDFRectangle> getRectangles() {

            Map<Value, RDFRectangle> rectangles = new HashMap<Value, RDFRectangle>();

            CloseableIteration<BindingSet, QueryEvaluationException> iter = getResult();

            try {

                while (iter.hasNext()) {
                    BindingSet b = iter.next();
                    RDFRectangle rect;

                    Value sVal = (pattern.getSubjectVar().hasValue()) ? pattern.getSubjectVar().getValue() : b.getValue(pattern.getSubjectVar().getName());
                    Value oVal = (pattern.getObjectVar().hasValue())  ? pattern.getObjectVar().getValue() : b.getValue(pattern.getObjectVar().getName());
                    Value pVal = (pattern.getPredicateVar().hasValue())  ? pattern.getPredicateVar().getValue() : b.getValue(pattern.getPredicateVar().getName());

                    if (rectangles.containsKey(pVal)) {
                        rect = rectangles.get(pVal);
                        rect.getSubjectRange().expand(sVal.stringValue());
                        //rect.getPredicateRange().expand(pVal.stringValue());
                        rect.getObjectRange().expand(oVal);
                    } else {
                        rect = computeRectangle(sVal, pVal, oVal);
                        rectangles.put(pVal, rect);
                    }
                }

            } catch(QueryEvaluationException e) {
                return Collections.<RDFRectangle>emptyList();
            }

            return new LinkedList<RDFRectangle>(rectangles.values());
        }

    }

}
