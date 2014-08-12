package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.rdf.parsing.Binding;
import gr.demokritos.iit.irss.semagrow.rdf.parsing.BindingSet;
import gr.demokritos.iit.irss.semagrow.rdf.parsing.HistogramIO;
import gr.demokritos.iit.irss.semagrow.rdf.qfr.RDFQueryRecord;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import org.openrdf.repository.RepositoryException;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Nick on 07-Aug-14.
 */
public class TestMainQueryFeedbackGenerate {

    static String uniqueSubjectData = "C:\\Users\\Nick\\Downloads\\sorted\\sorted";
    static String filteredDataFolder = "C:\\Users\\Nick\\Downloads\\filtered\\";
    static String outputDataFolder = "C:\\Users\\Nick\\git\\sthist\\src\\main\\resources\\data\\";
    static String nativeStoreFolder = "src/main/resources/native_store/";
    static String trainingPool = "src/main/resources/training_pool/b1";
    static String evaluationPool = "src/main/resources/evaluation_pool/b1/";

    public static void main(String[] args) throws IOException, RepositoryException {

        int times = Integer.parseInt(args[0]);
        String uniqueSubjectData = args[1];
        String filteredDataFolder = args[2];
        String trainingPool = args[3];


        QueryFeedbackGenerator qfg = new QueryFeedbackGenerator(uniqueSubjectData, filteredDataFolder,
                nativeStoreFolder);

        QueryRecord qr;
        ArrayList<String> allPrefixes = new ArrayList<String>();
        File[] files = new File( trainingPool ).listFiles();
        for (File f : files) {
        	allPrefixes.add( "http://agris.fao.org/aos/records/" + f.getName() );
        }
        
        // --- Training Pool Generator
        for (int i=0; i<times; i++) {
            qr = qfg.generateTrainingSet();
            System.out.println(qr.getQuery());

            RDFQueryRecord rdfQr = (RDFQueryRecord)qr;
            // Get prefix
            String prefix = rdfQr.getLogQuery().getQueryStatements().get(0).getValue();
            String[] splits = prefix.split("/");
            writeToPool(trainingPool, splits[splits.length - 1], rdfQr);

//            rdfQr = readFromPool(trainingPool, splits[splits.length - 1]);
//            System.out.println(rdfQr.getQuery());
        }


//        // --- Evaluation Pool Generator
//        // Read all training prefixes.
//        qfg.savedPrefixes.addAll(getAllPrefixes(trainingPool));
//
//        for (int i=0; i<10; i++) {
//            qr = qfg.generateEvaluationSet();
//            System.out.println(qr.getQuery());
//
//            RDFQueryRecord rdfQr = (RDFQueryRecord)qr;
//            // Get prefix
//            String prefix = rdfQr.getLogQuery().getQueryStatements().get(0).getValue();
//            String[] splits = prefix.split("/");
//            writeToPool(trainingPool, splits[splits.length - 1], rdfQr);
//
////            rdfQr = readFromPool(trainingPool, splits[splits.length - 1]);
////            System.out.println(rdfQr.getQuery());
//        }


        // --- Evaluation Pool Generator
        // Read all training prefixes.
        qfg.savedPrefixes.addAll( allPrefixes );

        for (int i=0; i<2; i++) {
            qr = qfg.generateEvaluationSet();
            System.out.println(qr.getQuery());

            RDFQueryRecord rdfQr = (RDFQueryRecord)qr;
            // Get prefix
            String prefix = rdfQr.getLogQuery().getQueryStatements().get(0).getValue();
            String[] splits = prefix.split("/");
            writeToPool(evaluationPool, splits[splits.length - 1], rdfQr);

//            rdfQr = readFromPool(evaluationPool, splits[splits.length - 1]);
//            System.out.println(rdfQr.getQuery());
        }

//        new QueryPool(trainingPool, evaluationPool).getTrainingQueryRecords();

//        CustomCollection<RDFQueryRecord> collection = new CustomCollection<RDFQueryRecord>(trainingPool);
//        Iterator<RDFQueryRecord> iter = collection.iterator();
//        int i = 0;
//        while (iter.hasNext()) {
//           RDFQueryRecord rdfRq = iter.next();
//            System.out.println(i++);
//            for (BindingSet bs : rdfRq.getResultSet().getBindingSets()) {
//                if (bs.getBindings().get(1).getValue().length() < 32)
//                    System.out.println(">>>>> ");
////                else
////                    System.out.println(bs.getBindings().get(1).getValue());
//            }
//
//        }

//        System.out.println("http://aims.fao.org/aos/agrovoc/".length());

    }// main


    /**
     *
     * @param path
     * @param filename The last split of the prefix
     */
    private static void writeToPool(String path, String filename, RDFQueryRecord rdfQr) {

        writeBinary(path, filename, rdfQr);
        writeASCII(path, filename, rdfQr);
    }// writeToPool


    private static void writeBinary(String path, String filename, RDFQueryRecord rdfQr) {
        File file = new File(path + filename);
        ObjectOutputStream oos;

        if (!file.exists()) {

            try {

                oos = new ObjectOutputStream(new FileOutputStream(file));

                oos.writeObject(rdfQr);

                oos.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }// writeBinary


    private static void writeASCII(String path, String filename, RDFQueryRecord rdfQr) {
        File file = new File(path + filename + ".txt");
        BufferedWriter bw;

        try {
            bw = new BufferedWriter(new FileWriter(file));

            bw.write("Query Statement: ");
            bw.write(rdfQr.getQuery());
            bw.newLine();
            bw.newLine();
            bw.write("Query Bindings: \n");
            ArrayList<BindingSet> bsSets = rdfQr.getResultSet().getBindingSets();

            for (BindingSet bs : bsSets) {
                bw.write(bs.getBindings().get(0).getValue() + ", " +
                        bs.getBindings().get(1).getValue() + ", " +
                        bs.getBindings().get(2).getValue());
                bw.newLine();
            }

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }// writeASCII

}
