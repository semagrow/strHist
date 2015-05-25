package gr.demokritos.iit.irss.semagrow.tools.deprecated;

import gr.demokritos.iit.irss.semagrow.base.NumRectangle;
import gr.demokritos.iit.irss.semagrow.numericalqfr.NumQueryRecord;
import gr.demokritos.iit.irss.semagrow.rdf.RDFSTHolesHistogram;
import gr.demokritos.iit.irss.semagrow.rdf.io.json.JSONDeserializer;
import gr.demokritos.iit.irss.semagrow.rdf.io.json.JSONSerializer;
import gr.demokritos.iit.irss.semagrow.rdf.io.log.RDFQueryRecord;
import gr.demokritos.iit.irss.semagrow.stholesOrig.STHolesOrigHistogram;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by Nick on 10-Aug-14.
 * Full Experiment Setup.
 */
public class TestHistogram {

    private static Logger logger = LoggerFactory.getLogger(TestHistogram.class);

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        /*
        *  -t training dirs
        *  -e evaluation dirs
        *  -h histogram to start (optional)
        *  -o output dir
        *  -v verbose (output histogram in each
        *  -b max bucket
        *  -d difference of inaccuracy (epsilon)
        * */
        OptionParser parser = new OptionParser("nvt:e:o:h::");
        parser.accepts("maxbuckets", "Maximum Buckets").withRequiredArg();
        parser.accepts("epsilon", "Max difference of accurate statistics").withRequiredArg();
        parser.accepts("penalty").withRequiredArg();
        parser.accepts("?").forHelp();

        OptionSet options = parser.parse(args);

        if (options.has("n"))
            mainNum(options);
        else
            mainRDF(options);

        long end = System.currentTimeMillis();
        System.out.println("Total Time: " + (double) (end - start) / 1000 + " sec.");
    }

    public static void mainRDF(OptionSet options) throws IOException {

        RDFSTHolesHistogram h = new RDFSTHolesHistogram();

        String outputDir = ".";

        if (options.has("maxbuckets") && options.hasArgument("maxbuckets"))
            h.setMaxBucketsNum(Integer.parseInt(options.valueOf("maxbuckets").toString()));

        if (options.has("epsilon") && options.hasArgument("epsilon"))
            h.epsilon = Double.parseDouble(options.valueOf("epsilon").toString());

        if (options.has("penalty") && options.hasArgument("penalty")) {
            try { h.PENALTY_TYPE = Integer.parseInt(options.valueOf("penalty").toString()); }
            catch( Exception ex ) { h.PENALTY_TYPE = 1; }
            if( (h.PENALTY_TYPE<0) || (h.PENALTY_TYPE>h.MAX_PENALTY_TYPE) ) {
            	h.PENALTY_TYPE = 1;
            }
        }

        if (options.has("h") && options.hasArgument("h"))
            try {
                h = (RDFSTHolesHistogram)new JSONDeserializer(options.valueOf("h").toString()).
                        getHistogram();
            } catch (ParseException e) {
                e.printStackTrace();
            }

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

    }


    public static void mainNum(OptionSet options) throws IOException {

        STHolesOrigHistogram<NumRectangle> h = new STHolesOrigHistogram<NumRectangle>();

        String outputDir = ".";


        if (options.has("maxbuckets") && options.hasArgument("maxbuckets"))
            h.setMaxBucketsNum(Integer.parseInt(options.valueOf("maxbuckets").toString()));

        if (options.has("epsilon") && options.hasArgument("epsilon"))
            h.epsilon = Integer.parseInt(options.valueOf("epsilon").toString());

        if (options.has("o") && options.hasArgument("o"))
            outputDir = options.valueOf("o").toString();

        if (options.has("h") && options.hasArgument("h"))
            h = new gr.demokritos.iit.irss.semagrow.stholesOrig.io.json.JSONDeserializer
                    (options.valueOf("h").toString()).getHistogram();

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

                    runBatchNum(h, f.getAbsolutePath(), batchOutput, verbose);

                    outputHistogramNum(h, batchOutput + "/histogram.json");
                    outputHistogramStatsNum(h, batchOutput + "/stats.txt");

                    if (options.has("e") && options.hasArgument("e")) {
                        for (Object evopt : options.valuesOf("e")) {

                            File evf = new File(evopt.toString());
                            if (evf.exists()) {
                                String evalOutput = batchOutput + "/" + "estimates.csv";
                                if (options.valuesOf("e").size() > 1)
                                    evalOutput = batchOutput + "/" + "estimates" + evf.getName()  + ".csv";
                                runEvaluationNum(h, evf.getAbsolutePath() + "/", evalOutput);
                            }
                        }
                    }
                }
            }
        }

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

    private static void runBatchNum(STHolesOrigHistogram<NumRectangle> h, String trainingPoolPath, String outputPath, boolean verbose) {

        CustomCollection<NumQueryRecord> collection = new CustomCollection<NumQueryRecord>(trainingPoolPath + "/");
        Iterator<NumQueryRecord> iter = collection.iterator();


        while (iter.hasNext()) {
            NumQueryRecord rdfRq = iter.next();

            System.out.println(">>>" + rdfRq.getQuery());


            h.refine(rdfRq);
            System.out.println("<<<");

            // Write histogram to a file.
            if (verbose) {
                File hfd = new File(outputPath + "/histograms/");
                if (!hfd.exists()) hfd.mkdir();
                File hf = new File(outputPath + "/histograms/" + getSubjectLastSplitNum(rdfRq));
                outputHistogramNum(h, hf.toString());
            }
        }

    }

    private static void runEvaluationNum(STHolesOrigHistogram<NumRectangle> h,
                                      String evaluationPoolPath,
                                      String evaluationActualEstimates) throws IOException
    {
        CustomCollection<NumQueryRecord> collection = new CustomCollection<NumQueryRecord>(evaluationPoolPath);
        Iterator<NumQueryRecord> iter = collection.iterator();

        BufferedWriter bw = new BufferedWriter(new FileWriter(evaluationActualEstimates));

        bw.write("query_subject, evaluation, actual\n");

        while (iter.hasNext()) {
            NumQueryRecord rdfRq = iter.next();

            // Write actual and estimate query cardinality.
            bw.write(rdfRq.getNumQuery().getQueryStatements().get(0) + ", " +
                    h.estimate(rdfRq.getRectangle()) + ", " +
                    rdfRq.getNumQuery().getQueryResults().size());
            bw.newLine();
        }

        bw.close();
    }

    private static void outputHistogram(RDFSTHolesHistogram h, String filename) {
        try {
            new JSONSerializer(h, filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void outputHistogramNum(STHolesOrigHistogram<NumRectangle> h, String filename) {
        new gr.demokritos.iit.irss.semagrow.stholesOrig.io.json.JSONSerializer(h, filename);
    }

    private static void outputHistogramStats(RDFSTHolesHistogram h, String filename) throws IOException {

        BufferedWriter bw = new BufferedWriter(new FileWriter(filename));

        bw.write("Max Buckets: " + h.maxBucketsNum);
        bw.newLine();
        bw.write("Total Buckets: " + h.getBucketsNum());
        bw.newLine();
        bw.write("PC Merges: " + h.pcMergesNum);
        bw.newLine();
        bw.write("SS Merges: " + h.ssMergesNum);
        bw.newLine();
        bw.write("Total Merges: " + (h.ssMergesNum + h.pcMergesNum));
        bw.newLine();
        bw.close();
    }

    private static void outputHistogramStatsNum(STHolesOrigHistogram<NumRectangle> h, String filename) throws IOException {

        BufferedWriter bw = new BufferedWriter(new FileWriter(filename));

        bw.write("Max Buckets: " + h.maxBucketsNum);
        bw.newLine();
        bw.write("Total Buckets: " + h.getBucketsNum());
        bw.newLine();
        bw.write("PC Merges: " + h.pcMergesNum);
        bw.newLine();
        bw.write("SS Merges: " + h.ssMergesNum);
        bw.newLine();
        bw.write("Total Merges: " + (h.ssMergesNum + h.pcMergesNum));
        bw.newLine();
        bw.close();
    }

    private static String getSubjectLastSplit(RDFQueryRecord rdfQr) {

        String prefix = rdfQr.getLogQuery().getQueryStatements().get(0).getValue();
        String[] splits = prefix.split("/");

        return splits[splits.length - 1];
    }


    private static String getSubjectLastSplitNum(NumQueryRecord rdfQr) {

        String prefix =
                rdfQr.getNumQuery().getQueryStatements().get(0).getLow() + "-" +
                rdfQr.getNumQuery().getQueryStatements().get(0).getHigh();

        String[] splits = prefix.split("/");

        return splits[splits.length - 1];
    }
}
