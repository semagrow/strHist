package gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix;

import gr.demokritos.iit.irss.semagrow.rdf.RDFSTHolesHistogram;
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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nickozoulis on 20/11/2014.
 */
public class Evaluate {

    static final Logger logger = LoggerFactory.getLogger(Evaluate.class);
    static final OpenOption[] options = {StandardOpenOption.CREATE, StandardOpenOption.APPEND};
    private static String prefixes = "prefix dc: <http://purl.org/dc/terms/> prefix semagrow: <http://www.semagrow.eu/rdf/> ";
    private static final String DISTINCTPath = "/var/tmp/distinct/";
    private static Hashtable<String, Long> hashTable;

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
        evaluate(Utils.getRepository(year, inputPath));
    }

    private static void evaluate(Repository repo) throws IOException, RepositoryException {
        // Load Evaluations
        logger.info("Loading point query evaluations: " + year);
        loadPointQueryEvaluations();

        logger.info("Starting evaluation: " + year);
        RepositoryConnection conn;

        try {
            conn = repo.getConnection();

            Path path = Paths.get(outputPath, "results_" + year + ".csv");
            BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8, options);
            bw.write("Year, Prefix, Act, Est, AbsErr%\n\n");

            RDFSTHolesHistogram histogram = loadHistogram(year, 1);

            // Evaluate a point query on histogram and triple store.
            evaluateWithSampleTestQueries(conn, histogram, bw, 0.01);

            bw.flush();
            bw.close();
            conn.close();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        repo.shutDown();
    }

    private static RDFSTHolesHistogram loadHistogram(int year, int iteration) {
        RDFSTHolesHistogram histogram;

        if (iteration == 0)
            histogram = Utils.loadPreviousHistogram(outputPath, year);
        else
            histogram = Utils.loadCurrentHistogram(outputPath, year);

        return histogram;
    }

    private static void evaluateWithSampleTestQueries(RepositoryConnection conn,
                                                      RDFSTHolesHistogram histogram,
                                                      BufferedWriter bw,
                                                      double percentage) {

        logger.info("Executing test queries of year: " + year);

        String testQuery;
        Set samplingRows = Utils.getSamplingRows(DISTINCTPath + "subjects_" + year + ".txt", percentage);
        Iterator iter = samplingRows.iterator();

        while (iter.hasNext()) {
            try {
                Integer i = (Integer) iter.next();
                String subject = Utils.loadDistinctSubject(i, year, DISTINCTPath);

                testQuery = prefixes + " select * where {<%s> dc:subject ?o}";
                testQuery = String.format(testQuery, subject);

                evaluateTestQuery(conn, histogram, testQuery, bw);
            } catch (IOException e) {e.printStackTrace();}
        }

        try {bw.write("\n");} catch (IOException e) {e.printStackTrace();}
    }

    private static void evaluateTestQuery(RepositoryConnection conn, RDFSTHolesHistogram histogram,
                                          String testQuery, BufferedWriter bw) {
        String prefix = getPrefix(testQuery);

        long actual = hashTable.get(prefix);
        long estimate = Utils.evaluateOnHistogram(conn, histogram, testQuery);
        long error;

        if (actual == 0 && estimate == 0)
            error = 0;
        else
            error = (Math.abs(actual - estimate) * 100) / (Math.max(actual, estimate));

        try {
            bw.write(year + ", " + prefix + ", " + actual + ", " + estimate + ", " + error + "%");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private static Hashtable loadPointQueryEvaluations() {
        hashTable = new Hashtable<>();

        String line = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(DISTINCTPath + "evals_" + 2014 + ".csv"));

            while ((line = br.readLine()) != null) {
                String[] splits = line.split(",");

                try {
                    if (splits.length == 2)
                        hashTable.put(splits[0].trim(), Long.parseLong(splits[1].trim()));
                } catch (NumberFormatException e) {}
            }

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {e.printStackTrace();}

        return hashTable;
    }

}
