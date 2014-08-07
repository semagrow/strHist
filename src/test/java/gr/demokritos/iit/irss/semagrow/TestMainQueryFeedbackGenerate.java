package gr.demokritos.iit.irss.semagrow;

import java.io.IOException;

/**
 * Created by Nick on 07-Aug-14.
 */
public class TestMainQueryFeedbackGenerate {

    public static void main(String[] args) throws IOException {


        String uniqueSubjectData = "C:\\Users\\Nick\\Downloads\\sorted\\sorted";
        String filteredDataFolder = "C:\\Users\\Nick\\Downloads\\filtered\\";
        String outputDataFolder = "C:\\Users\\Nick\\git\\sthist\\src\\main\\resources\\data\\";

        QueryFeedbackGenerator qfg = new QueryFeedbackGenerator(uniqueSubjectData, filteredDataFolder, outputDataFolder);
        // qfg.getQueryRecord() gia na pareis to RDFQueryRecord
    }
}
