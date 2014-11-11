package gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix;

import gr.demokritos.iit.irss.semagrow.api.QueryLogRecord;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.rdf.RDFSTHolesHistogram;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import gr.demokritos.iit.irss.semagrow.tools.Utils;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nickozoulis on 11/11/2014.
 */
public class RefineAndEvaluate {
    /*
        Variables for local run
     */
//    private static int year = 1980;
//    private static String inputPath = "/home/nickozoulis/", outputPath = "/home/nickozoulis/semagrow/exp_No6/";

    static final Logger logger = LoggerFactory.getLogger(RefineAndEvaluate.class);
    static final OpenOption[] options = {StandardOpenOption.CREATE, StandardOpenOption.APPEND};
    private static String prefixes = "prefix dc: <http://purl.org/dc/terms/> prefix semagrow: <http://www.semagrow.eu/rdf/> ";
    private static ExecutorService executors;

    private static String inputPath, outputPath;
    private static int year;


    public static void main(String[] args) throws IOException, RepositoryException {
        OptionParser parser = new OptionParser("y:i:o:");
        OptionSet options = parser.parse(args);

        if (options.hasArgument("y") && options.hasArgument("i") && options.hasArgument("o")) {
            year = Integer.parseInt(options.valueOf("y").toString());
            inputPath = options.valueOf("i").toString();
            outputPath = options.valueOf("o").toString();

            executeExperiment();
        } else {
            logger.error("Invalid arguments");
            System.exit(1);
        }
    }

    private static void executeExperiment() throws IOException, RepositoryException {
        executors = Executors.newCachedThreadPool();

        refineAndEvaluate(Utils.getRepository(year, inputPath));

        executors.shutdown();
    }

    private static void refineAndEvaluate(Repository repo) throws IOException, RepositoryException {
        // Load Feedback
        Collection<QueryLogRecord> logs = Utils.parseFeedbackLog("/var/tmp/" + year + "/" + year + "_log.ser");
        Collection<QueryRecord> queryRecords = Utils.adaptLogs(logs, year, executors);

        logger.info("Starting refining histogram: " + year);
        RepositoryConnection conn;

        try {
            conn = repo.getConnection();
            Path path = Paths.get(outputPath + year, "results.csv");
            BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8, options);
            bw.write("Year, Prefix, Act, Est, AbsErr%\n");

            // For every 10 queryRecords
            for (int i=0; i<queryRecords.size(); i+=10) {
                // Get a subList
                Collection<QueryRecord> subQRList = ((List)queryRecords).subList(i, i + 10);
                Collection<QueryLogRecord> subLogList = ((List)logs).subList(i, i + 10);

                // Refine histogram according to the feedback subList
                RDFSTHolesHistogram histogram = refineHistogram(subQRList.iterator(), year);

                // Evaluate a point query on histogram and triple store.
                evaluateWithTestQuery(conn, histogram, bw, subLogList);
            }

            bw.close();
            conn.close();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        repo.shutDown();
    }

    private static void evaluateWithTestQuery(RepositoryConnection conn,
                                              RDFSTHolesHistogram histogram,
                                              BufferedWriter bw,
                                              Collection<QueryLogRecord> subList) {

        logger.info("Executing test query of year: " + year);

        String testQuery;

        // Fire point queries
        for (int i=0; i<10; i++) {
            testQuery = prefixes + " select * where {%s dc:subject ?o}";
            testQuery = String.format(testQuery, getRandomSubjectFromTSV(subList));
            evaluateTestQuery(conn, histogram, testQuery, bw);
        }
    }

    private static void evaluateTestQuery(RepositoryConnection conn, RDFSTHolesHistogram histogram,
                                        String testQuery, BufferedWriter bw) {

        long actual = Utils.evaluateOnTripleStore(conn, testQuery);
        long estimate = Utils.evaluateOnHistogram(conn, histogram, testQuery);
        if (actual == 0 && estimate == 0) return; // Should never happen
        long error = (Math.abs(actual - estimate) * 100) / (Math.max(actual, estimate));

        String prefix = getPrefix(testQuery);
        try {
            bw.write(year + ", " + prefix + ", " + actual + ", " + estimate + ", " + error + "%");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static RDFSTHolesHistogram refineHistogram(Iterator<QueryRecord> listQueryRecords, int year) {
        File folder = new File(outputPath + year);
        if (!folder.exists())
            folder.mkdir();

        RDFSTHolesHistogram histogram = Utils.loadPreviousHistogram(folder.getPath(), year);

        logger.info("Refining histogram " + year);
        ((STHolesHistogram)histogram).refine(listQueryRecords);
        logger.info("Refinement is over.");

        Utils.serializeHistogram(histogram, folder.getPath() + "/", year);

        return histogram;
    }

    /**
     * Chooses randomly a tsv-file from the subList and then randomly a subject from that tsv.
     * @return String subject
     */
    private static String getRandomSubjectFromTSV(Collection<QueryLogRecord> subList) {
        String path = "/var/tmp/" + year + "/", subject = "";
        List<String> tsvFiles = new ArrayList<String>();

        for (QueryLogRecord qlr : subList) {
            String[] splits = qlr.getResults().stringValue().split("/");
            tsvFiles.add(splits[splits.length - 1]);
        }

        int tsv = Utils.randInt(0, tsvFiles.size() - 1);

        try {
            BufferedReader br = new BufferedReader(new FileReader(path + tsvFiles.get(tsv)));

            int lines = Utils.countLineNumber(path + tsvFiles.get(tsv));
            int pos = Utils.randInt(1, lines);
            int counter = 0;

            String line = "";
            while ((line = br.readLine()) != null) {
                if (++counter == pos) {

                    String[] splits = line.split("\t");
                    subject = splits[0];

                    break;
                }
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return subject;
    }

    private static String getPrefix(String testQuery) {
        String pattern = "<(.*?)>";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(testQuery);

        while (m.find()) {
            if (m.group(0).contains("http://agris.fao.org/aos/records/")) {
                String[] splits = m.group(1).split("/");
                return splits[splits.length - 1];
            }
        }

        return "";
    }

}
