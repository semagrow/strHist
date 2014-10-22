package gr.demokritos.iit.irss.semagrow.tools;

import gr.demokritos.iit.irss.semagrow.CustomCollection;
import gr.demokritos.iit.irss.semagrow.NumQuery;
import gr.demokritos.iit.irss.semagrow.NumQueryRecord;
import gr.demokritos.iit.irss.semagrow.NumQueryResult;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.base.NumRectangle;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import gr.demokritos.iit.irss.semagrow.rdf.io.log.BindingSet;
import gr.demokritos.iit.irss.semagrow.rdf.io.log.RDFQueryRecord;
import gr.demokritos.iit.irss.semagrow.tools.NumericalMapper;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by Nick on 11-Aug-14.
 */
public class RDFtoNumQueryConverter {

    public static String uniqueSubjectData = "C:/Users/Nick/Downloads/sorted/sorted";
    static NumericalMapper numericalMapper;

    public static void main(String[] args) {

        OptionParser parser = new OptionParser("t:e:s:o:");
        OptionSet options = parser.parse(args);
        String trainingRdfPool = "src/main/resources/training_pool/b1/rdf/";
        String evaluationRdfPool = "src/main/resources/evaluation_pool/b1/rdf/";

        String outputDir = "o";

        if (options.hasArgument("s")) {

            uniqueSubjectData = options.valueOf("s").toString();

            // Instantiate collection that holds the sorted subjects. Caution: Heavy Process
            System.out.println("Loading index collections...\n");
            numericalMapper = new NumericalMapper(uniqueSubjectData);
        }

        if (options.hasArgument("o"))
            outputDir = options.valueOf("o").toString();

        if (options.hasArgument("t")) {
            for (Object tDir : options.valuesOf("t")) {
                File f = new File(tDir.toString());
                if (f.exists()) {
                    File o = new File(outputDir);
                    if (options.valuesOf("t").size() > 1) {
                        o = new File(outputDir, f.getName());
                    }

                    if (!o.exists())
                        o.mkdir();
                    trainingRdfPool = f.getAbsolutePath();
                    convertTraining(trainingRdfPool + "/", o.getAbsolutePath() + "/");
                }
            }
        }



        if (options.hasArgument("e")) {
            for (Object eDir : options.valuesOf("e")) {
                File f = new File(eDir.toString());
                if (f.exists()) {
                    File o = new File(outputDir);
                    if (options.valuesOf("e").size() > 1) {
                        o = new File(outputDir, f.getName());
                    }
                    if (!o.exists())
                        o.mkdir();
                    evaluationRdfPool = f.getAbsolutePath();
                    convertEvaluation(evaluationRdfPool + "/", o.getAbsolutePath() + "/");
                }
            }
        }
    }// main

    private static void convertTraining(String trainingRdfPool, String trainingNumPool) {

        /*
            Convert Training Pool
        */
        CustomCollection<RDFQueryRecord> collection = new CustomCollection<RDFQueryRecord>(trainingRdfPool);
        Iterator<RDFQueryRecord> iter = collection.iterator();
        System.out.println("Training Pool Conversion: " + trainingRdfPool);

        while (iter.hasNext()) {

            RDFQueryRecord rdfRq = iter.next();
            System.out.println("\nConverting... >>>" + rdfRq.getQuery());
            NumQueryRecord numQueryRecord = RDFConvertToNum(rdfRq, true);
            System.out.println("<<<");

            // Get prefix to put it as a file's name.
            String prefix = rdfRq.getLogQuery().getQueryStatements().get(0).getValue();
            String[] splits = prefix.split("/");

            writeToPool(trainingNumPool, splits[splits.length - 1], numQueryRecord);
        }// while
    }

    private static void convertEvaluation(String evaluationRdfPool, String evaluationNumPool) {
        /*
              Convert Evaluation Pool
        */
        CustomCollection<RDFQueryRecord> collection = new CustomCollection<RDFQueryRecord>(evaluationRdfPool);
        Iterator<RDFQueryRecord> iter = collection.iterator();
        System.out.println("Evaluation Pool Conversion: " + evaluationRdfPool);

        while (iter.hasNext()) {

            RDFQueryRecord rdfRq = iter.next();
            System.out.println("Converting... >>>" + rdfRq.getQuery());
            NumQueryRecord numQueryRecord = RDFConvertToNum(rdfRq, false);
            System.out.println("<<<");

            // Get prefix to put it as a file's name.
            String prefix = rdfRq.getLogQuery().getQueryStatements().get(0).getValue();
            String[] splits = prefix.split("/");

            writeToPool(evaluationNumPool, splits[splits.length - 1], numQueryRecord);
        }// while

    }

    private static NumQueryRecord RDFConvertToNum(RDFQueryRecord rdfRq, boolean isPrefix) {
        Random rand = new Random();
        List<IntervalRange> queryStatements = new ArrayList<IntervalRange>(3);
        List<List<IntervalRange>> queryResults = new ArrayList<List<IntervalRange>>();

        /*
            Convert Query Statements.
        */
        String subject = rdfRq.getLogQuery().getQueryStatements().get(0).getValue();

        queryStatements.add(numericalMapper.getMapping(subject, isPrefix));            // subject
        queryStatements.add(new IntervalRange(Integer.MIN_VALUE, Integer.MAX_VALUE));  // predicate
        queryStatements.add(new IntervalRange(Integer.MIN_VALUE, Integer.MAX_VALUE));  // object

        /*
            Convert Query Results.
        */
        for (BindingSet bs : rdfRq.getQueryResult().getBindingSets()) {

            List<IntervalRange> bindingSet = new ArrayList<IntervalRange>(3);

            subject = bs.getBindings().get(0).getValue();

            bindingSet.add(numericalMapper.getMapping(subject, false));     // subject
            bindingSet.add(new IntervalRange(1, 1));                        // predicate
            int randInt = rand.nextInt(1000);
            bindingSet.add(new IntervalRange(randInt, randInt));            // object

            queryResults.add(bindingSet);
        }

        return new NumQueryRecord(new NumQuery(queryStatements, queryResults));
    }// RDFConvertToNum


    private static QueryRecord readFromPool(String path, String filename) {
        QueryRecord queryRecord = null;
        File file = new File(path + filename);
        ObjectInputStream ois;

        try {

            ois = new ObjectInputStream(new FileInputStream(file));

            queryRecord = (QueryRecord)ois.readObject();

            ois.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (EOFException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return queryRecord;
    }// readFromPool


    public static void writeToPool(String path, String filename, QueryRecord qr) {
        writeBinary(path, filename, qr);
        writeASCII(path, filename, (NumQueryRecord)qr);
    }// writeToPool


    private static void writeBinary(String path, String filename, QueryRecord qr) {
        File file = new File(path + filename);
        ObjectOutputStream oos;

//        if (!file.exists()) {

            try {

                oos = new ObjectOutputStream(new FileOutputStream(file));

                oos.writeObject(qr);

                oos.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
//        }
    }// writeBinary


    private static void writeASCII(String path, String filename, NumQueryRecord numQr) {
        File file = new File(path + filename + ".txt");
        BufferedWriter bw;

        try {
            bw = new BufferedWriter(new FileWriter(file));

            bw.write("Query Statements: \n");
            bw.write(numQr.getQuery());
            bw.newLine();
            bw.write("Query Results: \n");
            List<NumRectangle> resultNumRectangles = ((NumQueryResult)numQr.getResultSet()).getResultNumRectangles();

            for (NumRectangle nr : resultNumRectangles) {
                bw.write(nr.toString());
                bw.newLine();
            }

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }// writeASCII

}
