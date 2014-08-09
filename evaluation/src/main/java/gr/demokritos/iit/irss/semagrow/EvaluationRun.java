package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.api.Histogram;
import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.api.STHistogram;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.api.range.Range;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.RDFSTHolesHistogram;
import gr.demokritos.iit.irss.semagrow.rdf.qfr.RDFQueryRecord;

/**
 * Created by angel on 8/9/14.
 */
public class EvaluationRun<R extends Rectangle<R>, S> {

    public static int ITERATIONS = 10;

    public static int QUERYPERBATCH = 50;

    public static int EVALQUERIES = 10;

    public static int MAXBUCKETS = 100;

    public static void main (String[] args) {

    }

    public void run() {

        Iterable<R> eval = getEvaluationWorkload();

        STHistogram<R,?> h = getHistogram();

        int i = 1;

        for (i = 1; i <= ITERATIONS ; i++) {
//            Iterable<RDFQueryRecord> batch = getTrainingWorkloadBatch();
//            h.refine(batch);
//            long error = estimateError(h, eval);
            // output (i, error);
        }

    }

    public STHistogram<R,?> getHistogram() { return null; }

    public Iterable<? extends QueryRecord<R,?>> getTrainingWorkloadBatch(){
        return null;
    }

    public Iterable<R> getEvaluationWorkload() {
        return null;
    }

    public long estimateError(Histogram<R> h, R r)
    {
        long estimated = h.estimate(r);
        return estimated - getActual(r);
    }

    public long estimateError(Histogram<R> h, Iterable<R> rects)
    {
        long accumError = 0;
        for (R r : rects)
            accumError += estimateError(h, r);

        return accumError;
    }

    public <R extends Rectangle<R>> long
        getActual(R r) {
        return 0;
    }
}
