package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.base.NumRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.parsing.BindingSet;
import gr.demokritos.iit.irss.semagrow.rdf.qfr.RDFQueryRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Nick on 11-Aug-14.
 */
public class RDFtoNumRectangleConverter {

    public static String uniqueSubjectData = "C:\\Users\\Nick\\Downloads\\sorted\\sorted";
    static String trainingPool = "src/main/resources/training_pool/b1";
    static String evaluationPool = "src/main/resources/evaluation_pool/b1/";
    static String trainingNumPool = "src/main/resources/training_pool/num/b1";
    static String evaluationNumPool = "src/main/resources/evaluation_pool/num/b1/";

    public static void main(String[] args) {

        /*
         Convert Training Pool
        */
        CustomCollection<RDFQueryRecord> collection = new CustomCollection<RDFQueryRecord>(trainingPool);
        Iterator<RDFQueryRecord> iter = collection.iterator();
        System.out.println("Training Pool Conversion: " + trainingPool);

        while (iter.hasNext()) {

            RDFQueryRecord rdfRq = iter.next();
            System.out.println("Converting... >>>" + rdfRq.getQuery());

            NumQueryRecord numQueryRecord = new NumQueryRecord(rdfRq, true);
            System.out.println("<<<");

            // Get prefix to put it as a file's name.
            String prefix = rdfRq.getLogQuery().getQueryStatements().get(0).getValue();
            String[] splits = prefix.split("/");

            writeToPool(trainingNumPool, splits[splits.length - 1], numQueryRecord);
        }// while


//        /*
//         Convert Evaluation Pool
//        */
//        collection = new CustomCollection<RDFQueryRecord>(evaluationPool);
//        iter = collection.iterator();
//        System.out.println("Evaluation Pool Conversion: " + evaluationPool);
//
//        while (iter.hasNext()) {
//
//            RDFQueryRecord rdfRq = iter.next();
//            System.out.println("Converting... >>>" + rdfRq.getQuery());
//
//            NumQueryRecord numQueryRecord = new NumQueryRecord(rdfRq, false);
//            System.out.println("<<<");
//
//            // Get prefix to put it as a file's name.
//            String prefix = rdfRq.getLogQuery().getQueryStatements().get(0).getValue();
//            String[] splits = prefix.split("/");
//
//            writeToPool(evaluationNumPool, splits[splits.length - 1], numQueryRecord);
//        }// while

    }// main


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

        if (!file.exists()) {

            try {

                oos = new ObjectOutputStream(new FileOutputStream(file));

                oos.writeObject(qr);

                oos.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }// writeBinary


    private static void writeASCII(String path, String filename, NumQueryRecord numQr) {
        File file = new File(path + filename + ".txt");
        BufferedWriter bw;

        try {
            bw = new BufferedWriter(new FileWriter(file));

            bw.write("Query Statement: ");
            bw.write(numQr.getQuery());
            bw.newLine();
            bw.newLine();
            bw.write("Query Bindings: \n");
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
