package gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix;

import gr.demokritos.iit.irss.semagrow.api.QueryLogRecord;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.rdf.RDFSTHolesHistogram;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import gr.demokritos.iit.irss.semagrow.tools.Utils;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nickozoulis on 11/11/2014.
 */
public class RefineAndEvaluate {
    /*
        Variables for local run
     */
    private static int year = 1980;
    private static String inputPath = "/home/nickozoulis/", outputPath = "/home/nickozoulis/semagrow/exp_No6/";

    static final Logger logger = LoggerFactory.getLogger(RefineAndEvaluate.class);
    static final OpenOption[] options = {StandardOpenOption.CREATE, StandardOpenOption.APPEND};
    private static String prefixes = "prefix dc: <http://purl.org/dc/terms/> prefix semagrow: <http://www.semagrow.eu/rdf/> ";
    private static ExecutorService executors;

//    private static String inputPath, outputPath;
//    private static int year;


    public static void main(String[] args) throws IOException, RepositoryException {
        executeExperiment();
//        OptionParser parser = new OptionParser("y:i:o:");
//        OptionSet options = parser.parse(args);
//
//        if (options.hasArgument("y") && options.hasArgument("i") && options.hasArgument("o")) {
//            year = Integer.parseInt(options.valueOf("y").toString());
//            inputPath = options.valueOf("i").toString();
//            outputPath = options.valueOf("o").toString();
//
//            executeExperiment();
//        } else {
//            logger.error("Invalid arguments");
//            System.exit(1);
//        }
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

            // For every 10 queryRecords
            for (int i=0; i<queryRecords.size() - 10; i+=10) {
                // Get a subList
                Collection<QueryRecord> subList = ((List) queryRecords).subList(i, i + 10);

                // Refine histogram according to the feedback subList
                RDFSTHolesHistogram histogram = (RDFSTHolesHistogram)refineHistogram(subList.iterator(), year);

                // Evaluate a point query on histogram and triple store.
                evaluateWithTestQuery(conn, histogram);
            }

            conn.close();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        repo.shutDown();
    }

    private static void evaluateWithTestQuery(RepositoryConnection conn, RDFSTHolesHistogram histogram) {
        logger.info("Executing test query of year: " + year);
        Path path = Paths.get(outputPath + year, "results.csv");
        BufferedWriter bw;

        try {
            bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8, options);

            bw.write("Year, Act, Est, AbsErr%");
            bw.newLine();

            String testQuery = "";//TODO

            evaluateTestQuery(conn, histogram, testQuery, bw);

            bw.close();
        } catch (IOException e) {e.printStackTrace();}
    }

    private static void evaluateTestQuery(RepositoryConnection conn, RDFSTHolesHistogram histogram,
                                        String testQuery, BufferedWriter bw) {
        long actual = Utils.evaluateOnTripleStore(conn, testQuery);
        long estimate = Utils.evaluateOnHistogram(conn, histogram, testQuery);
        long error = (Math.abs(actual - estimate) * 100) / (Math.max(actual, estimate));

        try {
            bw.write(year + ", " + actual + ", " + estimate + ", " + error + "%");
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

}
