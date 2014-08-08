package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import org.openrdf.repository.RepositoryException;

import java.io.IOException;

/**
 * Created by Nick on 07-Aug-14.
 */
public class TestMainQueryFeedbackGenerate {

    public static void main(String[] args) throws IOException, RepositoryException {


        String uniqueSubjectData = "C:\\Users\\Nick\\Downloads\\sorted\\sorted";
        String filteredDataFolder = "C:\\Users\\Nick\\Downloads\\filtered\\";
        String outputDataFolder = "C:\\Users\\Nick\\git\\sthist\\src\\main\\resources\\data\\";
        String nativeStoreFolder = "src\\main\\resources\\native_store\\";

        QueryFeedbackGenerator qfg = new QueryFeedbackGenerator(uniqueSubjectData, filteredDataFolder,
                outputDataFolder, nativeStoreFolder);

        Iterable<QueryRecord> trainingSet = qfg.generateTrainingSet(1);
        for (QueryRecord qr : trainingSet)
            System.out.println(qr.getQuery());

        Iterable<QueryRecord> evaluationSet = qfg.generateEvaluationSet(1);
        for (QueryRecord qr : evaluationSet)
            System.out.println(qr.getQuery());
    }
}
