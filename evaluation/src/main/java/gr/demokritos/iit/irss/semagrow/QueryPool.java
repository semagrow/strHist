package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.rdf.qfr.RDFQueryRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by Nick on 09-Aug-14.
 */
public class QueryPool {

    private String trainingPoolPath, evaluationPoolPath;


    public QueryPool(String trainingPoolPath, String evaluationPoolPath) {
        this.trainingPoolPath = trainingPoolPath;
        this.evaluationPoolPath = evaluationPoolPath;
    }// Constructor


    public Iterator<RDFQueryRecord> getTrainingQueryRecords() {
        ArrayList<RDFQueryRecord> queryRecords = new ArrayList<RDFQueryRecord>();

        File[] files = new File(trainingPoolPath).listFiles();

        System.out.println("Reading files...");
        for (File f : files) {
            System.out.println(f.getName());
            queryRecords.add(readFromPool(trainingPoolPath, f.getName()));
        }

        return queryRecords.iterator();
    }// getTrainingQueryRecords


    public Iterator<RDFQueryRecord> getEvaluationQueryRecords() {
        ArrayList<RDFQueryRecord> queryRecords = new ArrayList<RDFQueryRecord>();

        File[] files = new File(evaluationPoolPath).listFiles();

        System.out.println("Reading files...");
        for (File f : files) {
            System.out.println(f.getName());
            queryRecords.add(readFromPool(evaluationPoolPath, f.getName()));
        }

        return queryRecords.iterator();
    }// getTrainingQueryRecords


    private RDFQueryRecord readFromPool(String path, String filename) {
        RDFQueryRecord rdfQueryRecord = null;
        File file = new File(path + filename);
        ObjectInputStream ois;

        try {

            ois = new ObjectInputStream(new FileInputStream(file));

            rdfQueryRecord = (RDFQueryRecord)ois.readObject();

            ois.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return  rdfQueryRecord;
    }// readFromPool

}
