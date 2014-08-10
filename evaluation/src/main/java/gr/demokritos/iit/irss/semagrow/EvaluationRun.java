package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.api.Histogram;
import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.api.STHistogram;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;

/**
 * Created by angel on 8/9/14.
 */
public class EvaluationRun<R extends Rectangle<R>, S> {

    private int ITERATIONS;

    private STHistogram<R,S> histogram;

    private Iterable<? extends QueryRecord<R,S>> trainingWorkloadBatches[];

    private Iterable<R> evaluationWorkload;

    public EvaluationRun(STHistogram<R,S> histogram,
                         Iterable<? extends QueryRecord<R,S>> trainingWorkloadBatches[],
                         Iterable<R> evaluationWorkload)
    {
        this.histogram = histogram;
        this.trainingWorkloadBatches = trainingWorkloadBatches;
        ITERATIONS = trainingWorkloadBatches.length;
        this.evaluationWorkload = evaluationWorkload;
    }

    public void run() {

        Iterable<R> eval = getEvaluationWorkload();

        STHistogram<R,S> h = getHistogram();

        for (int i = 0; i <= ITERATIONS ; i++) {
            Iterable<? extends QueryRecord<R,S>> batch = getTrainingWorkloadBatch(i);
            h.refine(batch);
            long error = estimateError(h, eval);
            // output (i+1, error);
        }

    }

    public STHistogram<R,S> getHistogram() { return histogram; }

    public Iterable<? extends QueryRecord<R,S>> getTrainingWorkloadBatch(int iter){
        return trainingWorkloadBatches[iter];
    }

    public Iterable<R> getEvaluationWorkload() {
        return evaluationWorkload;
    }

    public long estimateError(Histogram<R> h, R r)
    {
        long estimated = h.estimate(r);
        return estimated - getActual(r);

         /*
        return (estimated - getActual(r))^2;
         */
    }

    public long estimateError(Histogram<R> h, Iterable<R> rects)
    {
        long accumError = 0;
        int W = 0;

        for (R r : rects) {
            accumError += estimateError(h, r);
            W += 1;
        }

        return accumError;

        //return Math.sqrt((double)accumError/W);
    }

    public <R extends Rectangle<R>> long
        getActual(R r) {
        return 0;
    }
}
