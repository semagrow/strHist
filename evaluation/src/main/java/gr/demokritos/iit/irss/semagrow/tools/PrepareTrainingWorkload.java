package gr.demokritos.iit.irss.semagrow.tools;

import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import gr.demokritos.iit.irss.semagrow.api.QueryLogHandler;
import gr.demokritos.iit.irss.semagrow.file.FileManager;
import gr.demokritos.iit.irss.semagrow.file.ResultMaterializationManager;
import gr.demokritos.iit.irss.semagrow.impl.QueryLogInterceptor;
import gr.demokritos.iit.irss.semagrow.impl.serial.SerialQueryLogFactory;
import gr.demokritos.iit.irss.semagrow.sesame.TestSail;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.*;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nickozoulis on 10/11/2014.
 */
public class PrepareTrainingWorkload {
    /*
        Variables for local run
     */
//    private static int year = 1980;
//    private static String inputPath = "/home/nickozoulis/";

    static final Logger logger = LoggerFactory.getLogger(PrepareTrainingWorkload.class);
    private static URI endpoint = ValueFactoryImpl.getInstance().createURI("http://histogramnamespace/example");
    private static final String DISTINCTPath = "/var/tmp/distinct/";
    private static String prefixes = "prefix dc: <http://purl.org/dc/terms/> prefix semagrow: <http://www.semagrow.eu/rdf/> ";
    private static String query = prefixes + "select * where {?sub dc:subject ?obj . filter regex(str(?sub), \"^%s\")}";
    private static QueryLogInterceptor interceptor;
    private static ExecutorService executors;
    private static Random rand = new Random();

    private static String inputPath;
    private static int year, numOfQueries;


    public static void main(String[] args) throws IOException, RepositoryException {
        OptionParser parser = new OptionParser("y:i:b:");
        OptionSet options = parser.parse(args);

        if (options.hasArgument("y") && options.hasArgument("i") && options.hasArgument("b")) {
            year = Integer.parseInt(options.valueOf("y").toString());
            inputPath = options.valueOf("i").toString();
            numOfQueries = Integer.parseInt(options.valueOf("b").toString());

            prepareTrainingWorkload();
        } else {
            logger.error("Invalid arguments");
            System.exit(1);
        }
    }

    private static void prepareTrainingWorkload() throws IOException, RepositoryException {
        executors = Executors.newCachedThreadPool();

        interceptor = new QueryLogInterceptor(initHandler(), initMateralizationManager());
        queryStore(getRepository(year));

        executors.shutdown();
    }

    private static void queryStore(Repository repo) throws IOException {
        int subjectsNum = countLineNumber(DISTINCTPath);
        String trimmedSubject;

        logger.info("Starting quering triple store: " + year);
        RepositoryConnection conn;

        for (int j=0; j<numOfQueries; j++) {
            logger.info("Query No: " + j);
            try {
                conn = repo.getConnection();

                trimmedSubject = trimSubject(loadDistinctSubject(randInt(0, subjectsNum)));
                String q = String.format(query, trimmedSubject);
                TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, q);
                logger.info("Query: " + q);

                // Get TupleExpr
                ParsedTupleQuery psq = QueryParserUtil.parseTupleQuery(QueryLanguage.SPARQL,q, "http://example.org/");
                TupleExpr tupleExpr = psq.getTupleExpr();

                CloseableIteration<BindingSet, QueryEvaluationException> result =
                        interceptor.afterExecution(endpoint, tupleExpr, tupleQuery.getBindings(), tupleQuery.evaluate());
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

    private static QueryLogHandler initHandler() {
        QueryLogHandler handler = null;
        SerialQueryLogFactory factory = new SerialQueryLogFactory();

        try {
            File dir = new File("/var/tmp/" + year + "/");
            if (!dir.exists())
                dir.mkdir();

            File qfrLog = new File("/var/tmp/" + year + "/" + year + "_log.ser");
            OutputStream out = new FileOutputStream(qfrLog, true);
            handler = factory.getQueryRecordLogger(out);
        } catch (IOException e) {e.printStackTrace();}

        return handler;
    }

    private static ResultMaterializationManager initMateralizationManager(){
        ResultMaterializationManager manager = null;

        if (manager == null) {
            File baseDir = new File("/var/tmp/" + year + "/");
            if (!baseDir.exists())
                baseDir.mkdir();

            TupleQueryResultFormat resultFF = TupleQueryResultFormat.TSV;

            TupleQueryResultWriterRegistry registry = TupleQueryResultWriterRegistry.getInstance();
            TupleQueryResultWriterFactory writerFactory = registry.get(resultFF);
            manager = new FileManager(baseDir, writerFactory, executors);
        }

        return manager;
    }

}
