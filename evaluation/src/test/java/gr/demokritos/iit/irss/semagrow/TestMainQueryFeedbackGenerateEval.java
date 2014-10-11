package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.rdf.io.log.BindingSet;
import gr.demokritos.iit.irss.semagrow.rdf.io.log.RDFQueryRecord;
import org.openrdf.repository.RepositoryException;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by angel on 8/13/14.
 * Testing the generation evaluation pools for the experiment.
 */
public class TestMainQueryFeedbackGenerateEval {

    static String uniqueSubjectData = "C:\\Users\\Nick\\Downloads\\sorted\\sorted";
    static String filteredDataFolder = "C:\\Users\\Nick\\Downloads\\filtered\\";
    static String outputDataFolder = "C:\\Users\\Nick\\git\\sthist\\src\\main\\resources\\data\\";
    static String nativeStoreFolder = "src/main/resources/native_store/";
    static String trainingPool = "src/main/resources/training_pool/b1";
    static String evaluationPool = "src/main/resources/evaluation_pool/b1/";
    static String prefixFile = "prefix.txt";

    public static void main(String[] args) throws IOException, RepositoryException {

        int size = Integer.parseInt(args[0]);
        prefixFile = args[1];
        uniqueSubjectData = args[2];
        evaluationPool = args[3];
        nativeStoreFolder = args[4];

        QueryFeedbackGenerator qfg = new QueryFeedbackGenerator(uniqueSubjectData, filteredDataFolder,
                nativeStoreFolder);

        ArrayList<String> allPrefixes = new ArrayList<String>();
        allPrefixes = readPrefixes(prefixFile);

        //File[] files = new File(trainingPool).listFiles();
        //for (File f : files) {
            //allPrefixes.add("http://agris.fao.org/aos/records/" + f.getName());
        //}

        evalGen(size, qfg, allPrefixes);

    }// main

    private static ArrayList<String> readPrefixes(String prefixFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(prefixFile));
        ArrayList<String> prefixes = new ArrayList<String>();
        String line;
        while ((line = br.readLine()) != null) {
            // process the line.
            line = line.replace("<", "").replace(">","").trim();
            prefixes.add(line);
        }
        br.close();
        return prefixes;
    }


    private static void evalGen(int s, QueryFeedbackGenerator qfg, ArrayList<String> allPrefixes) throws IOException, RepositoryException {

        // --- Evaluation Pool Generator
        // Read all training prefixes.
        qfg.savedPrefixes.addAll(allPrefixes);

        for (int i = 0; i < s; i++) {
            QueryRecord qr = qfg.generateEvaluationSet();
            System.out.println(qr.getQuery());

            RDFQueryRecord rdfQr = (RDFQueryRecord) qr;
            // Get prefix
            String prefix = rdfQr.getLogQuery().getQueryStatements().get(0).getValue();
            String[] splits = prefix.split("/");
            writeToPool(evaluationPool, splits[splits.length - 1], rdfQr);

//            rdfQr = readFromPool(evaluationPool, splits[splits.length - 1]);
//            System.out.println(rdfQr.getQuery());
        }


    }

    /**
     * @param path
     * @param filename The last split of the prefix
     */
    private static void writeToPool(String path, String filename, RDFQueryRecord rdfQr) {

        writeBinary(path, filename, rdfQr);
        //writeASCII(path, filename, rdfQr);
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
