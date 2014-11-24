package gr.demokritos.iit.irss.semagrow.tools;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nickozoulis on 21/11/2014.
 */
public class GeneratePointQueriesEvaluation {

    static final Logger logger = LoggerFactory.getLogger(GeneratePointQueriesEvaluation.class);
    static final OpenOption[] options = {StandardOpenOption.CREATE, StandardOpenOption.APPEND};
    private static String prefixes = "prefix dc: <http://purl.org/dc/terms/> prefix semagrow: <http://www.semagrow.eu/rdf/> ";
    private static final String DISTINCTPath = "/var/tmp/distinct/";
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

            execute();
        } else {
            logger.error("Invalid arguments");
            System.exit(1);
        }
    }

    private static void execute() throws IOException, RepositoryException {
        executors = Executors.newCachedThreadPool();

        evaluate(Utils.getRepository(year, inputPath));

        executors.shutdown();
    }

    private static void evaluate(Repository repo) throws IOException, RepositoryException {
        logger.info("Starting point query evaluation: " + year);
        RepositoryConnection conn;

        try {
            conn = repo.getConnection();

            Path path = Paths.get(outputPath, "evals_" + year + ".csv");
            BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8, options);
            bw.write("Prefix, Eval\n\n");

            String line = "", testQuery = "";
            BufferedReader br = new BufferedReader(new FileReader(DISTINCTPath + "subjects_" + year + ".txt"));

            while ((line = br.readLine()) != null) {
                testQuery = prefixes + " select * where {<%s> dc:subject ?o}";
                testQuery = String.format(testQuery, line);

                long actual = Utils.evaluateOnTripleStore(conn, testQuery);
                bw.write(getPrefix(testQuery) + ", " + actual);
                bw.newLine();
                bw.flush();
            }

            bw.close();
            conn.close();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        repo.shutDown();
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
