package gr.demokritos.iit.irss.semagrow.tools;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nickozoulis on 18/11/2014.
 */
public class ExtractRepoStats {

    static final Logger logger = LoggerFactory.getLogger(ExtractRepoStats.class);
    private static int year;
    private static String inputPath;

    public static void main(String[] args) throws IOException, RepositoryException {
        OptionParser parser = new OptionParser("y:i:o:");
        OptionSet options = parser.parse(args);

        if (options.hasArgument("y") && options.hasArgument("i")) {
            year = Integer.parseInt(options.valueOf("y").toString());
            inputPath = options.valueOf("i").toString();
            execute();
        } else {
            logger.error("Invalid arguments");
            System.exit(1);
        }
    }

    private static void execute() throws IOException, RepositoryException {
//        queryStore(Utils.getRepository(year, inputPath));
        findNumbersByDistribution(Utils.getRepository(year, inputPath));
    }

    private static void findNumbersByDistribution(Repository repo) throws IOException, RepositoryException {
        RepositoryConnection conn;

        try {
            conn = repo.getConnection();
            writeNumsToFile(year, getRandomNumbers(Utils.countRepoTriples(conn), 100));
        } catch (NumberFormatException e) {e.printStackTrace();
        } catch (Exception e) {e.printStackTrace();}

        repo.shutDown();
    }

    private static void queryStore(Repository repo) throws IOException, RepositoryException {
        RepositoryConnection conn;

        try {
            conn = repo.getConnection();
            long c;

            logger.info("Counting distinct subjects..");
            c = Utils.countDistinctRepoSubjects(conn);
            logger.info("Distinct subjects:" + c);

            logger.info("Counting distinct objects..");
            c = Utils.countDistinctRepoObjects(conn);
            logger.info("Distinct objects:" + c);

            logger.info("Counting triples for year: " + year);
            c = Utils.countRepoTriples(conn);
            logger.info("Total Triples:" + c);

        } catch (NumberFormatException e) {e.printStackTrace();
        } catch (Exception e) {e.printStackTrace();}

        repo.shutDown();
    }

    private static List<Long> getRandomNumbers(long triples, int nums) {
        logger.info("Total Repo Triples: " + triples);
        logger.info("Getting Random Numbers..");
        List<Long> list = new ArrayList<>();

        while (list.size() < nums)
            list.add((long)Utils.randInt(0, (int)triples));

        return list;
    }

    private static void writeNumsToFile(int year, List<Long> list) {
        logger.info("Writing Nums to file");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("/var/tmp/train_numb3rs/" + year + ".txt"));

            for (Long l : list)
                bw.write(l + "\n");

            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
