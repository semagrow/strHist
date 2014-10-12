package gr.demokritos.iit.irss.semagrow.sesame;

import eu.semagrow.stack.modules.sails.semagrow.helpers.BPGCollector;
import eu.semagrow.stack.modules.sails.semagrow.helpers.CombinationIterator;
import eu.semagrow.stack.modules.sails.semagrow.optimizer.Plan;
import eu.semagrow.stack.modules.sails.semagrow.optimizer.PlanCollection;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by angel on 10/11/14.
 */
public class JoinOptimizer implements QueryOptimizer {



    public JoinOptimizer(CostEstimator estimator) {

    }

    @Override
    public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {

        // optimize the order of joins.
        // use always nested loop joins.
        Collection<TupleExpr> basicGraphPatterns = BPGCollector.process(tupleExpr);

        for (TupleExpr bgp : basicGraphPatterns)
            optimizeBGP(bgp, dataset, bindings);

    }

    public void optimizeBGP(TupleExpr bgp, Dataset dataset, BindingSet bindings) {

        // optPlans is a function from (Set of Expressions) to (Set of Plans)
        PlanCollection optPlans = new PlanCollection();

        Collection<Plan> accessPlans = accessPlans(bgp, dataset, bindings);

        optPlans.addPlan(accessPlans);

        // plans.getExpressions() get basic expressions
        // subsets S of size i
        //
        Set<TupleExpr> r = optPlans.getExpressions();

        int count = r.size();

        // bottom-up starting for subplans of size "k"
        for (int k = 2; k <= count; k++) {

            // enumerate all subsets of r of size k
            for (Set<TupleExpr> s : subsetsOf(r, k)) {

                for (int i = 1; i < k; i++) {

                    // let disjoint sets o1 and o2 such that s = o1 union o2
                    for (Set<TupleExpr> o1 : subsetsOf(s, i)) {

                        Set<TupleExpr> o2 = new HashSet<TupleExpr>(s);
                        o2.removeAll(o1);

                        Collection<Plan> plans1 = optPlans.get(o1);
                        Collection<Plan> plans2 = optPlans.get(o2);
                        Collection<Plan> newPlans = joinPlans(plans1, plans2);

                        optPlans.addPlan(newPlans);
                    }
                }
                prunePlans(optPlans.get(s));
            }
        }
        Collection<Plan> fullPlans = optPlans.get(r);
        finalizePlans(fullPlans);

        if (!fullPlans.isEmpty()) {
            TupleExpr bestPlan = getBestPlan(fullPlans);
            bgp.replaceWith(bestPlan);
        }
    }

    private Collection<Plan> accessPlans(TupleExpr expr, Dataset dataset, BindingSet bindings) {

        return null;

    }

    private void finalizePlans(Collection<Plan> plans) { }

    private Collection<Plan> joinPlans(Collection<Plan> e1, Collection<Plan> e2) {
        Collection<Plan> plans = new LinkedList<Plan>();
        for (Plan p1 : e1)
            for (Plan p2 : e2)
                plans.addAll(joinPlans(p1, p2));

        return plans;
    }

    private Collection<Plan> joinPlans(Plan p1, Plan p2) {
        Set<TupleExpr> s = new HashSet<TupleExpr>(p1.getPlanId());
        s.addAll(p2.getPlanId());

        Collection<Plan> plans = new LinkedList<Plan>();
        plans.add(new Plan(s, new Join(p1,p2)));
        return plans;
    }

    private Collection<Plan> prunePlans(Collection<Plan> expr) {
        return expr;
    }

    private static <T> Iterable<Set<T>> subsetsOf(Set<T> s, int k) {
        return new CombinationIterator<T>(k, s);
    }

    private TupleExpr getBestPlan(Collection<Plan> plans) {
        if (plans.isEmpty())
            return null;

        Plan bestPlan = plans.iterator().next();

        for (Plan p : plans)
            if (p.getCost() < bestPlan.getCost())
                bestPlan = p;

        return bestPlan;
    }
}
