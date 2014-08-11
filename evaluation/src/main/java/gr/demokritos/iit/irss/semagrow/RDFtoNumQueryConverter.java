package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.base.NumRectangle;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import gr.demokritos.iit.irss.semagrow.rdf.parsing.BindingSet;
import gr.demokritos.iit.irss.semagrow.rdf.qfr.RDFQueryRecord;
import gr.demokritos.iit.irss.semagrow.tools.NumericalMapper;

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
    static String trainingRdfPool = "src/main/resources/training_pool/b1/rdf/";
    static String evaluationRdfPool = "src/main/resources/evaluation_pool/b1/rdf/";
    static String trainingNumPool = "src/main/resources/training_pool/b1/num/";
    static String evaluationNumPool = "src/main/resources/evaluation_pool/b1/num/";
    static NumericalMapper numericalMapper;

    public static void main(String[] args) {


//        uniqueSubjectData = args[0];
//        trainingRdfPool = args[1];
//        trainingNumPool = args[2];
//        evaluationPool = args[1];
//        evaluationNumPool = args[2];


        // Instantiate collection that holds the sorted subjects. Caution: Heavy Process
        System.out.println("Loading index collections...\n");
        numericalMapper = new NumericalMapper(uniqueSubjectData);



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


        /*
              Convert Evaluation Pool
        */
//        CustomCollection<RDFQueryRecord> collection = new CustomCollection<RDFQueryRecord>(evaluationRdfPool);
//        Iterator<RDFQueryRecord> iter = collection.iterator();
//        System.out.println("Evaluation Pool Conversion: " + evaluationRdfPool);
//
//        while (iter.hasNext()) {
//
//            RDFQueryRecord rdfRq = iter.next();
//            System.out.println("Converting... >>>" + rdfRq.getQuery());
//            NumQueryRecord numQueryRecord = RDFConvertToNum(rdfRq, false);
//            System.out.println("<<<");
//
//            // Get prefix to put it as a file's name.
//            String prefix = rdfRq.getLogQuery().getQueryStatements().get(0).getValue();
//            String[] splits = prefix.split("/");
//
//            writeToPool(evaluationNumPool, splits[splits.length - 1], numQueryRecord);
//        }// while

    }// main


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
