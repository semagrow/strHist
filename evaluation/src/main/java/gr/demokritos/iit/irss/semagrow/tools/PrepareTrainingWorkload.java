package gr.demokritos.iit.irss.semagrow.tools;

import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import gr.demokritos.iit.irss.semagrow.sesame.TestSail;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * Created by nickozoulis on 10/11/2014.
 */
public class PrepareTrainingWorkload {

    static final Logger logger = LoggerFactory.getLogger(PrepareTrainingWorkload.class);
    private static final String DISTINCTPath = "/var/tmp/distinct/";
    private static String prefixes = "prefix dc: <http://purl.org/dc/terms/> prefix semagrow: <http://www.semagrow.eu/rdf/> ";
    private static String query = prefixes + "select * where {?sub dc:subject <%s> filter regex(str(?sub), \"^%s\")}";

//    private static String inputPath, output, agroTermsPath;
//    private static int year;
    private static Random rand = new Random();

    private static int year = 1980;
    private static String inputPath = "/home/nickozoulis/",
            output = "/home/nickozoulis/semagrow/exp_No6/",
            agroTermsPath = "/home/nickozoulis/semagrow/agrovoc_terms.txt";


    public static void main(String[] args) throws IOException, RepositoryException {
        prepareTrainingWorkload();
//        OptionParser parser = new OptionParser("y:i:o:a:");
//        OptionSet options = parser.parse(args);
//
//        if (options.hasArgument("y") && options.hasArgument("i") && options.hasArgument("o") && options.hasArgument("a")) {
//            year = Integer.parseInt(options.valueOf("y").toString());
//            inputPath = options.valueOf("i").toString();
//            output = options.valueOf("o").toString();
//            agroTermsPath = options.valueOf("a").toString();
//
//            prepareTrainingWorkload();
//        } else {
//            logger.error("Invalid arguments");
//            System.exit(1);
//        }
    }

    private static void prepareTrainingWorkload() throws IOException, RepositoryException {
        queryStore(getFedRepository(getRepository(year), year));
    }

    private static void queryStore(Repository repo) throws IOException {
        int subjectsNum = countLineNumber(DISTINCTPath);
        String trimmedSubject;
        List<String> agroTerms = loadAgroTerms(agroTermsPath);

        logger.info("Starting quering triple store: " + year);
        RepositoryConnection conn;
        int term = 0;

        // Loop through some agroTerms
        for (int j=0; j<200; j++) {
            logger.info("Agrovoc Term: " + agroTerms.get(term));
            try {
                conn = repo.getConnection();

                trimmedSubject = trimSubject(loadDistinctSubject(randInt(0, subjectsNum)));
                String q = String.format(query, agroTerms.get(term++), trimmedSubject);
                TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, q);
                logger.info("Query: " + q);

                TupleQueryResult result = query.evaluate();
                consumeIteration(result);
                conn.close();

            } catch (MalformedQueryException | RepositoryException | QueryEvaluationException mqe) {
                mqe.printStackTrace();
            }
        }
    }

    private static void consumeIteration(Iteration<BindingSet,QueryEvaluationException> iter) throws QueryEvaluationException {
        int n = 0;
        logger.info("Consuming items.");
        while (iter.hasNext()) {
            iter.next();
            ++n;
        }
        logger.info("Iterated over " + n + " items.");
        Iterations.closeCloseable(iter);
    }

    private static String trimSubject(String subject) {
        String[] splits = subject.split("/");
        String lastSlashPrefix = splits[splits.length - 1];

        // Get random cut on the prefix. 3 is given to avoid memory heap overflow
        int randomCut = randInt(2, lastSlashPrefix.length() - 3);

        String trimmedSubject = "";
        // Reform the trimmed subject. Intentionally exclude the last one.
        for (int i=0; i<splits.length - 1; i++) {
            trimmedSubject += splits[i] + "/";
        }

        // Append the random cut.
        trimmedSubject += lastSlashPrefix.substring(0, randomCut);

        return trimmedSubject;
    }

    private static String loadDistinctSubject(int num) throws IOException{
        String subject = "";

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(DISTINCTPath + "subjects_" + year + ".txt"));
            String line = "";
            int counter = 0;

            while ((line = br.readLine()) != null) {
                if (counter++ == num) {
                    subject = line;
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (br != null)
                br.close();
        }

        return subject;
    }

    private static Repository getFedRepository(Repository actual, int date) throws RepositoryException {
        TestSail sail = new TestSail(actual, date);
        Repository repo = new SailRepository(sail);
        repo.initialize();
        return repo;
    }

    private static Repository getRepository(int year) throws RepositoryException, IOException {
        Properties properties = new Properties();

        File journal = new File(inputPath + "bigdata_agris_data_" + year + ".jnl");

        properties.setProperty(
                BigdataSail.Options.FILE,
                journal.getAbsolutePath()
        );

        // Instantiate a sail and a Sesame repository
        BigdataSail sail = new BigdataSail(properties);
        Repository repo = new BigdataSailRepository(sail);
        repo.initialize();

        return repo;
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
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        return rand.nextInt((max - min) + 1) + min;
    }

    public static int countLineNumber(String path) {
        int lines = 0;
        try {

            File file = new File(path + "subjects_" + year + ".txt");
            LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
            lineNumberReader.skip(Long.MAX_VALUE);
            lines = lineNumberReader.getLineNumber();
            lineNumberReader.close();

        } catch (FileNotFoundException e) {
            logger.debug("FileNotFoundException Occurred" +  e.getMessage());
        } catch (IOException e) {
            logger.debug("IOException Occurred" + e.getMessage());
        }

        return lines;
    }

    private static List<String> loadAgroTerms(String path) {
        List<String> list = new ArrayList<String>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String text = "";

            while ((text = br.readLine()) != null) {
                String[] split = text.split(",");
                list.add(split[1].trim());
            }

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

}
