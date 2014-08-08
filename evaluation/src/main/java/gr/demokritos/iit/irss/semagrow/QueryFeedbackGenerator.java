package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.rdf.parsing.Binding;
import gr.demokritos.iit.irss.semagrow.rdf.parsing.BindingSet;
import gr.demokritos.iit.irss.semagrow.rdf.parsing.LogQuery;
import gr.demokritos.iit.irss.semagrow.rdf.qfr.RDFQueryRecord;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by Nick on 07-Aug-14.
 */
public class QueryFeedbackGenerator {

    private Iterable<QueryRecord> queryRecords;
    private String uniqueSubjectData, filteredDataFolder, outputDataFolder;
    private int uniqueSubjectFileRows;
    private ArrayList<String> savedPrefixes;


    public QueryFeedbackGenerator(String uniqueSubjectData, String filteredDataFolder, String outputDataFolder) {

        this.uniqueSubjectData = uniqueSubjectData;
        this.filteredDataFolder = filteredDataFolder;
        this.outputDataFolder = outputDataFolder;

        savedPrefixes = new ArrayList<String>();

        // Count total lines of unique subject file.
        uniqueSubjectFileRows = countLineNumber(uniqueSubjectData);
        System.out.println("Total File Rows: " + uniqueSubjectFileRows);
    }


    public Iterable<QueryRecord> generateTrainingSet(int size) throws IOException {
        System.out.println("Generating Training Set..");
        ArrayList<QueryRecord> queryRecords = new ArrayList<QueryRecord>();

        for (int i=0; i<size; i++)
            queryRecords.add(generateTrainingQueryRecord());

        return queryRecords;
    }


    public Iterable<QueryRecord> generateEvaluationSet(int size) throws IOException {
        System.out.println("Generating Evaluation Set..");
        ArrayList<QueryRecord> queryRecords = new ArrayList<QueryRecord>();

        for (int i=0; i<size; i++)
            queryRecords.add(generateEvaluationQueryRecord());

        return queryRecords;
    }


    /**
     * The returned URI must at least satisfies a prefix used in the training workload.
     */
    private RDFQueryRecord generateEvaluationQueryRecord() throws IOException {

        // Choose a random prefix from the saved ones.
        int randomRowNumber = randInt(1, savedPrefixes.size());
        String prefix = savedPrefixes.get(randomRowNumber - 1);

        String subject = getSpecificSubject(prefix);

        // Extract query feedback from filtered data based on that subject.
        return getQueryFeedback(filteredDataFolder, subject);
    }


    private RDFQueryRecord generateTrainingQueryRecord() throws IOException {

        // Choose a random subject.
        int randomRowNumber = randInt(1, uniqueSubjectFileRows);
        System.out.println("Random Row Number: " + randomRowNumber);

        String subject = getSpecificSubject(randomRowNumber);
        System.out.println("Random Subject: " + subject);

        // Trim its prefix randomly
        // TODO: Make it general for various datasets
        // For this dataset, only last slash defers.

        String[] splits = subject.split("/");
        String lastSlashPrefix = splits[splits.length - 1];
        System.out.println("Last Prefix: " + lastSlashPrefix);

        // Get random cut on the prefix.
        int randomCut = randInt(0, lastSlashPrefix.length() - 1);
        System.out.println("Random Cut Number: " + randomCut);

        String trimmedSubject = "";
        // Reform the trimmed subject. Intentionally exclude the last one.
        for (int i=0; i<splits.length - 1; i++) {
            trimmedSubject += splits[i] + "/";
        }

        // Append the random cut.
        trimmedSubject += lastSlashPrefix.substring(0, randomCut);
        System.out.println("Trimmed Prefix: " + trimmedSubject);

        // Store the prefix into a collection for the evaluation set.
        if (!savedPrefixes.contains(trimmedSubject))
            savedPrefixes.add(trimmedSubject);

        // Extract query feedback from filtered data based on that subject.
        return getQueryFeedback(filteredDataFolder, trimmedSubject);
    }


    private RDFQueryRecord getQueryFeedback(String filteredDataFolder, String subject) throws IOException {

        // Query Statements.
        LogQuery lq = new LogQuery();
        lq.setSessionId("");
        lq.setStartTime(0);
        lq.setSparqlEndpoint("");
        lq.getQueryStatements().add(new Binding("subject", subject));
        lq.getQueryStatements().add(new Binding("predicate", ""));
        lq.getQueryStatements().add(new Binding("object", ""));

        // Query BindingSets.
        ArrayList<BindingSet> bindingSets = extractQueryBindingSets(new File(filteredDataFolder), subject);
        System.out.println("Size of BindingSets = " + bindingSets.size());
        RDFQueryRecord queryRecord = new RDFQueryRecord(lq);
        queryRecord.getQueryResult().setBindingSets(bindingSets);
        return queryRecord;
    }


    private ArrayList<BindingSet> extractQueryBindingSets(File filteredDataFolder, String subject) throws IOException {

        ArrayList<BindingSet> bindingSets = new ArrayList<BindingSet>();
        File[] files = filteredDataFolder.listFiles();

        for (File file : files) {
            System.out.println(file.getName());
            bindingSets.addAll(extractFromFile(file, subject));
        }

        return bindingSets;
    }


    private ArrayList<BindingSet> extractFromFile(File file, String subject) throws IOException {
        ArrayList<BindingSet> bindingSets = new ArrayList<BindingSet>();

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        String splits[];

        while ((line = br.readLine()) != null) {
            splits = line.split(" ");

            // Check if the current subject starts with the trimmed subject.
            String currentSubject = cleanString(splits[0]);

            if (currentSubject.startsWith(subject)) {// If yes, add the predicate and object of this tuple to the BindingSet.
                BindingSet bs = new BindingSet();

                bs.getBindings().add(new Binding("predicate", cleanString(splits[1])));
                bs.getBindings().add(new Binding("object", cleanString(splits[2])));

                bindingSets.add(bs);
            }
        }

        br.close();
        return bindingSets;
    }


    private String cleanString(String string) {

        string = string.replace("<", "");
        string = string.replace(">", "");

        return string.trim();
    }


    private String getSpecificSubject(int row) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(uniqueSubjectData));
        String line = "";
        int counter = 0;

        while ((line = br.readLine()) != null) {
            counter++;

            if (counter == row) {
                line = cleanString(line);
                break;
            }
        }

        br.close();

        return line;
    }


    private String getSpecificSubject(String prefix) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(uniqueSubjectData));
        String line = "", subject = "";

        while ((line = br.readLine()) != null) {

            if ((subject = cleanString(line)).startsWith(prefix)) {
                line = subject;
                break;
            }
        }

        br.close();

        return line;
    }


    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }


    public static int countLineNumber(String path) {
        int lines = 0;
        try {

            File file = new File(path);
            LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
            lineNumberReader.skip(Long.MAX_VALUE);
            lines = lineNumberReader.getLineNumber();
            lineNumberReader.close();

        } catch (FileNotFoundException e) {
             System.out.println("FileNotFoundException Occurred"
                     + e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException Occurred" + e.getMessage());
        }

        return lines;
    }


    public Iterable<QueryRecord> getQueryRecords() {
        return queryRecords;
    }

}


