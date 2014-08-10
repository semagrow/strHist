package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.api.STHistogram;
import gr.demokritos.iit.irss.semagrow.rdf.parsing.HistogramIO;
import gr.demokritos.iit.irss.semagrow.rdf.qfr.RDFQueryRecord;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;

import java.io.*;
import java.util.Iterator;

/**
 * Created by Nick on 10-Aug-14.
 */
public class TestHistogram {

    static String trainingPool = "src\\main\\resources\\training_pool\\";
    static String evaluationPool = "src\\main\\resources\\evaluation_pool\\";
    static String trainingOutputPath = "src\\main\\resources\\histograms\\training_pool\\";
    static String evaluationOutputPath = "src\\main\\resources\\histograms\\training_pool\\";
    static String trainingActualEstimates = "src\\main\\resources\\training_actual_estimates.txt";
    static String evaluationActualEstimates = "src\\main\\resources\\evaluation_actual_estimates.txt";
    static BufferedWriter bw;


    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        bw = new BufferedWriter(new FileWriter(evaluationActualEstimates));

        HistogramIO histIO;
        CustomCollection<RDFQueryRecord> collection = new CustomCollection<RDFQueryRecord>(evaluationPool);
        Iterator<RDFQueryRecord> iter = collection.iterator();

        STHolesHistogram h = new STHolesHistogram();

        while (iter.hasNext()) {
            RDFQueryRecord rdfRq = iter.next();

            h.refine(rdfRq);

            // Write actual and estimate query cardinality.
            bw.write(rdfRq.getLogQuery().getQueryStatements().get(0).getValue() + "\n" +
                    "Evaluation: " + h.estimate(rdfRq.getRectangle()) +
                    ", Actual: " + rdfRq.getQueryResult().getBindingSets().size());
            bw.newLine();

            // Write histogram to a file.
            histIO = new HistogramIO(evaluationOutputPath + getSubjectLastSplit(rdfRq),
                    ((STHolesHistogram) h));
            histIO.write();
        }

        bw.close();
        long end = System.currentTimeMillis();
        System.out.println("Total Time: " + (double) (end - start) / 1000 + " sec.");
    }


    private static String getSubjectLastSplit(RDFQueryRecord rdfQr) {

        String prefix = rdfQr.getLogQuery().getQueryStatements().get(0).getValue();
        String[] splits = prefix.split("/");

        return splits[splits.length - 1];
    }
}
