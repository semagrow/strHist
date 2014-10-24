package gr.demokritos.iit.irss.semagrow.sesame;

import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.io.json.JSONDeserializer;
import gr.demokritos.iit.irss.semagrow.rdf.io.json.JSONSerializer;
import gr.demokritos.iit.irss.semagrow.rdf.io.log.LogParser;
import gr.demokritos.iit.irss.semagrow.rdf.io.log.RDFQueryRecord;
import gr.demokritos.iit.irss.semagrow.rdf.io.sevod.VoIDSerializer;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.openrdf.model.Literal;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.*;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * Created by angel on 10/11/14.
 */
public class Workflow {
     /*
        Variables for local run
     */
//    private static List<String> agroTerms = loadAgroTerms("/home/nickozoulis/agrovoc_terms.txt");
//    public static String logFolder = "/home/nickozoulis/strhist_exp_logs/";
//    private static String tripleStorePath = "/home/nickozoulis/Downloads/";
//    private static int term = 0;
//    private static int startDate = 1980, endDate = 1980;
//    public static Path path =  Paths.get(logFolder, "semagrow_logs.log");


    static final Logger logger = LoggerFactory.getLogger(Workflow.class);

    private static String prefixes = "prefix dc: <http://purl.org/dc/terms/> prefix semagrow: <http://www.semagrow.eu/rdf/> ";
    private static String testQ1 = prefixes + "select * {?x semagrow:year %s . }";

    private static List<String> agroTerms;
    private static String logFolder, tripleStorePath;
    private static int term = 0, startDate, endDate;
    public static Path path;
    static final OpenOption[] options = {StandardOpenOption.CREATE, StandardOpenOption.APPEND};

    /**
     * s = Starting date, e = Ending Date, l = LogOutput path, t = TripleStore, a = AgroknowTerms
     * @param args
     * @throws RepositoryException
     * @throws IOException
     */
    static public void main(String[] args) throws RepositoryException, IOException {
        OptionParser parser = new OptionParser("s:e:l:t:a:");
        OptionSet options = parser.parse(args);
        
        if (options.hasArgument("s") && options.hasArgument("e") && options.hasArgument("l")
                && options.hasArgument("t") && options.hasArgument("a")) {

            startDate = Integer.parseInt(options.valueOf("s").toString());
            endDate = Integer.parseInt(options.valueOf("e").toString());
            if (startDate > endDate) {
                logger.error("Invalid arguments");
                System.exit(1);
            }

            logFolder = options.valueOf("l").toString();
            tripleStorePath = options.valueOf("t").toString();
            agroTerms = loadAgroTerms(options.valueOf("a").toString());

            runMultiAnnualExperiment();
        }
        else {
            logger.error("Invalid arguments");
            System.exit(1);
        }
    }

    private static void runMultiAnnualExperiment() throws RepositoryException, IOException {
        for (int date=startDate; date<=endDate; date++) {

            path = Paths.get(logFolder, "semagrow_log_" + date + ".log");

            // Query triple stores and get feedback.
            Repository repo = getFedRepository(getRepository(date));
//            queryTripleStores(repo, date);

            // Parse feedback logs.
            List<RDFQueryRecord> listQueryRecords = new LogParser(path.toString()).parse();

            // Refine histogram according to the feedback.
            STHolesHistogram histogram = refineHistogram(listQueryRecords, date);

            // Execute test queries on triple store and refined histogram.
            execTestQueries(repo, histogram, date);
        }
    }

    private static void queryTripleStores(Repository repo, int date) throws RepositoryException, IOException {
        RepositoryConnection conn;
        int term = 0;
        logger.debug("Starting quering triples store: " + date);
            // For now loop for some agroTerms
            for (int j=0; j<200; j++) {
                logger.debug(term + " -- " + agroTerms.get(term));
                try {
                    conn = repo.getConnection();

                    String qq = prefixes + "select * {?u dc:subject <%s> . ?u semagrow:year ?y }";
                    String quer = String.format(qq, agroTerms.get(term++));
                    TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, quer);

                    TupleQueryResult result = query.evaluate();
                    consumeIteration(result);
                    conn.close();

                } catch (MalformedQueryException | QueryEvaluationException mqe) {
                    mqe.printStackTrace();
                }

            }
    }

    private static STHolesHistogram refineHistogram(List<RDFQueryRecord> listQueryRecords, int date) {
        STHolesHistogram histogram = loadPreviousHistogram(logFolder, date);

        if (listQueryRecords.size() > 0) {
            logger.debug("Refining histogram " + date);
            histogram.refine(listQueryRecords);
            logger.debug("Refinement is over.");
        }
        else logger.debug("No query records. No histogram refinement.");

        serializeHistogram(histogram, logFolder, date);

        return histogram;
    }

    private static void execTestQueries(Repository repo,
                                        STHolesHistogram<RDFRectangle> histogram,
                                        int date) throws RepositoryException, IOException {
        RepositoryConnection conn;
        logger.debug("Executing test queries of: " + date);
        Path path = Paths.get(logFolder, "results.csv");
        BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8, options);
        bw.write("Date, Q, QYear, Act, Est");
        bw.newLine();

        Random r = new Random();

        for (int j=0; j<10; j++) {
            conn = repo.getConnection();

            int year = date - r.nextInt(10);
            execTestQueries(conn, histogram, year, bw, date);
            conn.close();
        }

        bw.close();
    }

    private static void execTestQueries(RepositoryConnection conn,
                                        STHolesHistogram<RDFRectangle> histogram,
                                        int year,
                                        BufferedWriter bw, int numPass) {

        Literal lit = ValueFactoryImpl.getInstance().createLiteral(year);
        String testQ1str = String.format(testQ1, lit.toString());

        try {
            bw.write(numPass + ", Q1, " + year + ", " +
                    execTripleStore(conn, testQ1str) + ", " +
                    execHistogram(conn, histogram, testQ1str));
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static long execHistogram(RepositoryConnection conn,
                                      STHolesHistogram<RDFRectangle> histogram,
                                      String query) {
        try {
            logger.debug("Cardinality estimation on Histogram for query: " + query);
            ParsedTupleQuery q = QueryParserUtil.parseTupleQuery(QueryLanguage.SPARQL, query, "http://example.org/");
            long card = new CardinalityEstimatorImpl(histogram).
                    getCardinality(q.getTupleExpr(), EmptyBindingSet.getInstance());

            return card;

        } catch (MalformedQueryException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private static long execTripleStore(RepositoryConnection conn, String query) {
        try {

            logger.debug("Cardinality estimation on Triple Store for query: " + query);
            ParsedTupleQuery q = QueryParserUtil.parseTupleQuery(QueryLanguage.SPARQL, query, "http://example.org/");
            long card = new ActualCardinalityEstimator(conn).
                    getCardinality(q.getTupleExpr(), EmptyBindingSet.getInstance());

            return card;

        } catch (MalformedQueryException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private static Repository getFedRepository(Repository actual) throws RepositoryException {
        TestSail sail = new TestSail(actual);
//        histogram = sail.getHistogram();
        Repository repo = new SailRepository(sail);
        repo.initialize();
        return repo;
    }

    private static void consumeIteration(Iteration<BindingSet,QueryEvaluationException> iter) throws QueryEvaluationException {
    	int n = 0;
        logger.debug("Consuming items.");
        while (iter.hasNext()) {
            iter.next();
            ++n;
        }
        logger.debug("Iterated over " + n + " items." );
        Iterations.closeCloseable(iter);
    }

    private static Repository getRepository(int year) throws RepositoryException, IOException {
        Properties properties = new Properties();

        File journal = new File(tripleStorePath + "bigdata_agris_data_" + year + ".jnl");

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

    private static void serializeHistogram(STHolesHistogram histogram, String path, int date) {
        logger.debug("Serializing histogram to JSON in : " + logFolder + "histJSON_" + date + ".txt");
        new JSONSerializer(histogram, logFolder + "histJSON_" + date + ".txt");
        logger.debug("Serializing histogram to VOID in : " + logFolder + "histVOID_" + date + ".txt");
        new VoIDSerializer("application/x-turtle", logFolder + "histVOID_" + date + ".ttl").serialize(histogram);
    }

    private static STHolesHistogram loadPreviousHistogram(String logFolder, int date) {
        if (date == startDate)
            logger.debug("Creating a new histogram.");
        else
            logger.debug("Deserializing histogram: " + (date - 1));

        return (date == startDate)
                ? new STHolesHistogram<RDFRectangle>()
                : new JSONDeserializer(logFolder + "histJSON_" + (date - 1) + ".txt").getHistogram();
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
