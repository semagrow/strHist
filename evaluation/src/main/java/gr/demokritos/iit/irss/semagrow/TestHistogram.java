package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.rdf.RDFSTHolesHistogram;
import gr.demokritos.iit.irss.semagrow.rdf.parsing.HistogramIO;
import gr.demokritos.iit.irss.semagrow.rdf.qfr.RDFQueryRecord;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Iterator;

/**
 * Created by Nick on 10-Aug-14.
 */
public class TestHistogram {

    static String trainingPool = "src/main/resources/training_pool/b1/";
    static String evaluationPool = "src/main/resources/evaluation_pool/b1/";
    static String trainingOutputPath = "src/main/resources/histograms/training_pool/b1/";
    static String evaluationOutputPath = "src/main/resources/histograms/evaluation_pool/";
    static String evaluationActualEstimates = "src/main/resources/evaluation_actual_estimates.txt";

    static  HistogramIO histIO;

    private static Logger logger = LoggerFactory.getLogger(TestHistogram.class);

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();

        RDFSTHolesHistogram h = new RDFSTHolesHistogram();

        /*
        *  -t training dirs
        *  -e evaluation dirs
        *  -h histogram to start (optional)
        *  -o output dir
        *  -v verbose (output histogram in each
        * */
        OptionParser parser = new OptionParser("vt:e:o:h::");
        OptionSet options = parser.parse(args);

        String outputDir = ".";

        if (options.has("h") && options.hasArgument("h"))
             h = HistogramIO.read(options.valueOf("h").toString());

        if (options.has("o") && options.hasArgument("o"))
            outputDir = options.valueOf("o").toString();

        if (options.has("t") && options.hasArgument("t")) {

            for (Object tropt : options.valuesOf("t")) {

                File f = new File(tropt.toString());
                System.out.println("Training folder :"  + f.getAbsolutePath());

                String batchOutput = outputDir;

                if (options.valuesOf("t").size() > 1) {

                    File outf = new File(outputDir + "/" + f.getName() + "/");

                    if (!outf.exists())
                        outf.mkdirs();

                    batchOutput = outf.getAbsolutePath();
                }
                System.out.println("Output folder :"  + batchOutput);

                if (f.exists()) {
                    boolean verbose = false;

                    if (options.has("v"))
                        verbose = true;

                    runBatch(h, f.getAbsolutePath(), batchOutput, verbose);

                    outputHistogram(h, batchOutput + "/histogram.json");
                    outputHistogramStats(h, batchOutput + "/stats.txt");

                    if (options.has("e") && options.hasArgument("e")) {
                        for (Object evopt : options.valuesOf("e")) {

                            File evf = new File(evopt.toString());
                            if (evf.exists()) {
                                String evalOutput = batchOutput + "/" + "estimates.csv";
                                if (options.valuesOf("e").size() > 1)
                                    evalOutput = batchOutput + "/" + "estimates" + evf.getName()  + ".csv";
                                runEvaluation(h, evf.getAbsolutePath() + "/", evalOutput);
                            }
                        }
                    }
                }
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("Total Time: " + (double) (end - start) / 1000 + " sec.");
    }

    private static void runBatch(RDFSTHolesHistogram h, String trainingPoolPath, String outputPath, boolean verbose) {

        CustomCollection<RDFQueryRecord> collection = new CustomCollection<RDFQueryRecord>(trainingPoolPath + "/");
        Iterator<RDFQueryRecord> iter = collection.iterator();


        while (iter.hasNext()) {
            RDFQueryRecord rdfRq = iter.next();

            System.out.println(">>>" + rdfRq.getQuery());


            h.refine(rdfRq);
            System.out.println("<<<");

            // Write histogram to a file.
            if (verbose) {
                File hfd = new File(outputPath + "/histograms/");
                if (!hfd.exists()) hfd.mkdir();
                File hf = new File(outputPath + "/histograms/" + getSubjectLastSplit(rdfRq));
                outputHistogram(h, hf.toString());
            }
        }

    }

    private static void runEvaluation(RDFSTHolesHistogram h,
                                      String evaluationPoolPath,
                                      String evaluationActualEstimates) throws IOException
    {
        CustomCollection<RDFQueryRecord> collection = new CustomCollection<RDFQueryRecord>(evaluationPoolPath);
        Iterator<RDFQueryRecord> iter = collection.iterator();

        BufferedWriter bw = new BufferedWriter(new FileWriter(evaluationActualEstimates));

        bw.write("query_subject, evaluation, actual\n");

        while (iter.hasNext()) {
            RDFQueryRecord rdfRq = iter.next();

            // Write actual and estimate query cardinality.
            bw.write(rdfRq.getLogQuery().getQueryStatements().get(0).getValue() + ", " +
                    h.estimate(rdfRq.getRectangle()) + ", " +
                    rdfRq.getQueryResult().getBindingSets().size());
            bw.newLine();
        }

        bw.close();
    }

    private static void outputHistogram(RDFSTHolesHistogram h, String filename) {
        histIO = new HistogramIO(filename, h);
        histIO.write();
    }

    private static void outputHistogramStats(RDFSTHolesHistogram h, String filename) throws IOException {

        BufferedWriter bw = new BufferedWriter(new FileWriter(filename));

        bw.write("Max Buckets: " + h.maxBucketsNum);
        bw.write("Total Buckets: " + h.getBucketsNum());
        bw.close();
    }

    private static String getSubjectLastSplit(RDFQueryRecord rdfQr) {

        String prefix = rdfQr.getLogQuery().getQueryStatements().get(0).getValue();
        String[] splits = prefix.split("/");

        return splits[splits.length - 1];
    }
}
