package gr.demokritos.iit.irss.semagrow.sesame;

import eu.semagrow.core.plan.Plan;
import gr.demokritos.iit.irss.semagrow.api.Histogram;
import gr.demokritos.iit.irss.semagrow.api.range.Range;
import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.base.range.CalendarRange;
import gr.demokritos.iit.irss.semagrow.base.range.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import gr.demokritos.iit.irss.semagrow.base.range.PrefixRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFLiteralRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.RDFURIRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFValueRange;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.*;

import java.util.*;

/**
 * Created by angel on 10/11/14.
 */
public class CardinalityEstimatorImpl implements CardinalityEstimator {

    private Histogram<RDFRectangle> histogram;

    public CardinalityEstimatorImpl(Histogram<RDFRectangle> histogram) {
        this.histogram = histogram;
    }

    public long getCardinality(TupleExpr expr, BindingSet bindings) {

        if (expr instanceof StatementPattern)
            return getCardinality((StatementPattern)expr, bindings);
        else if (expr instanceof Union)
            return getCardinality((Union)expr, bindings);
        else if (expr instanceof Filter)
            return getCardinality((Filter)expr, bindings);
        else if (expr instanceof Projection)
            return getCardinality((Projection)expr, bindings);
        else if (expr instanceof Slice)
            return getCardinality((Slice)expr, bindings);
        else if (expr instanceof Join)
            return getCardinality((Join)expr, bindings);
        else if (expr instanceof LeftJoin)
            return getCardinality((LeftJoin)expr, bindings);
        else if (expr instanceof Plan)
            return getCardinality((Plan)expr, bindings);
        else
            return 0;
    }

    public long getCardinality(Plan plan, BindingSet bindings) {
        return plan.getProperties().getCardinality();
    }

    public long getCardinality(Slice slice, BindingSet bindings) {
        long card = getCardinality(slice.getArg(), bindings);
        return Math.min(card, slice.getLimit());
    }

    public long getCardinality(Join join, BindingSet bindings) {
        long card1 = getCardinality(join.getLeftArg(), bindings);
        long card2 = getCardinality(join.getRightArg(), bindings);
        double sel = 0.5;
        return (long)(card1 * card2 * sel);
    }

    public long getCardinality(LeftJoin join, BindingSet bindings) {
        long card1 = getCardinality(join.getLeftArg(), bindings);
        long card2 = getCardinality(join.getRightArg(), bindings);
        double sel = 0.5;
        return Math.max(card1, (long)(card1 * card2 * sel));
    }

    public long getCardinality(Filter filter, BindingSet bindings) {
        long card = getCardinality(filter.getArg(), bindings);
        return (long)(card * 0.5);
    }

    public long getCardinality(UnaryTupleOperator op, BindingSet bindings) {
        return getCardinality(op.getArg(), bindings);
    }

    public long getCardinality(Union union, BindingSet bindings) {
        return getCardinality(union.getLeftArg(), bindings) + getCardinality(union.getRightArg(), bindings);
    }

    public long getCardinality(StatementPattern pattern, BindingSet bindings) {

        return histogram.estimate(toRectangle(pattern, bindings));
    }

    private RDFRectangle toRectangle(StatementPattern pattern, BindingSet bindings) {
        Value sVal = pattern.getSubjectVar().getValue();
        Value pVal = pattern.getPredicateVar().getValue();
        Value oVal = pattern.getObjectVar().getValue();

        RDFURIRange subjectRange;
        List<String> list = new ArrayList<String>();
        if (sVal == null) {
            if (bindings.hasBinding(pattern.getSubjectVar().getName()))
                list.add(bindings.getValue(pattern.getSubjectVar().getName()).stringValue());
        } else
            list.add(sVal.stringValue());

        if (!list.isEmpty())
            subjectRange = new RDFURIRange(list);
        else
            subjectRange = new RDFURIRange();

        ExplicitSetRange<URI> predicateRange;
        Set<URI> set = new HashSet<URI>();
        if (pVal == null) {
            if (bindings.hasBinding(pattern.getPredicateVar().getName()))
                set.add((URI)bindings.getValue(pattern.getPredicateVar().getName()));
        } else
            set.add((URI) pVal);

        if (!set.isEmpty())
            predicateRange = new ExplicitSetRange<URI>(set);
        else
            predicateRange = new ExplicitSetRange<>();

        RDFValueRange objectRange = new RDFValueRange();
        if (oVal == null) {
            if (bindings.hasBinding(pattern.getObjectVar().getName()))
                objectRange = fillObjectRange(bindings.getValue(pattern.getObjectVar().getName()));
        } else
            objectRange = fillObjectRange(oVal);

        return new RDFRectangle(subjectRange, predicateRange, objectRange);
    }

    private RDFValueRange fillObjectRange(Value oVal) {
        if (oVal instanceof URI) {
            URI uri = (URI) oVal;
            PrefixRange pr = new PrefixRange();
            pr.getPrefixList().add(uri.stringValue());


            return new RDFValueRange(new RDFURIRange(pr.getPrefixList()),
                                     new RDFLiteralRange(Collections.<URI, RangeLength<?>>emptyMap()));
        } else if (oVal instanceof Literal) {
            Literal l = (Literal) oVal;
            Range literalRange = null;

            if (l.getDatatype().equals(XMLSchema.INT))
                literalRange = new RDFLiteralRange(XMLSchema.INT, new IntervalRange(l.intValue(), l.intValue()));
            else if (l.getDatatype().equals(XMLSchema.LONG))
                literalRange = new RDFLiteralRange(XMLSchema.LONG, new IntervalRange((int) l.longValue(), (int) l.longValue()));
            else if (l.getDatatype().equals(XMLSchema.STRING)) {
                PrefixRange pr = new PrefixRange();
                pr.getPrefixList().add(l.stringValue());
                literalRange = new RDFLiteralRange(XMLSchema.STRING, pr);
            } else if (l.getDatatype().equals(XMLSchema.DATETIME)) {
                Calendar cal = l.calendarValue().toGregorianCalendar();
                CalendarRange cr = new CalendarRange(cal.getTime(), cal.getTime());
                literalRange = new RDFLiteralRange(XMLSchema.DATETIME, cr);
            }

            if (literalRange != null)
                return new RDFValueRange(new RDFURIRange(Collections.<String>emptyList()),
                                        (RDFLiteralRange)literalRange);
        }

        return new RDFValueRange();
    }

}
