package gr.demokritos.iit.irss.semagrow.sesame;

import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import gr.demokritos.iit.irss.semagrow.api.QueryLogException;
import gr.demokritos.iit.irss.semagrow.api.QueryLogParser;
import gr.demokritos.iit.irss.semagrow.api.QueryLogRecord;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.base.Stat;
import gr.demokritos.iit.irss.semagrow.file.FileManager;
import gr.demokritos.iit.irss.semagrow.file.ResultMaterializationManager;
import gr.demokritos.iit.irss.semagrow.impl.QueryLogRecordCollector;
import gr.demokritos.iit.irss.semagrow.impl.serial.SerialQueryLogParser;
import gr.demokritos.iit.irss.semagrow.qfr.*;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.io.json.JSONDeserializer;
import gr.demokritos.iit.irss.semagrow.rdf.io.json.JSONSerializer;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by angel on 10/11/14.
 */
public class Workflow {
     /*
        Variables for local run
     */
//    private static List<String> agroTerms = loadAgroTerms("/home/nickozoulis/agrovoc_terms.txt");
//    public static String HISTPATH = "/home/nickozoulis/semagrow/serial/";
//    private static String TSPATH = "/home/nickozoulis/Downloads/";
//    private static int term = 0;
//    private static int startDate = 1980, endDate = 1980;


    static final Logger logger = LoggerFactory.getLogger(Workflow.class);
    private static String prefixes = "prefix dc: <http://purl.org/dc/terms/> prefix semagrow: <http://www.semagrow.eu/rdf/> ";
    private static String testQ1 = prefixes + "select * {?x semagrow:year %s . }";
    static final OpenOption[] options = {StandardOpenOption.CREATE, StandardOpenOption.APPEND};
    private static ExecutorService executors;

    private static List<String> agroTerms;

    public static String HISTPATH, TSPATH;
    private static int startDate, endDate;

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

            HISTPATH = options.valueOf("l").toString();
            TSPATH = options.valueOf("t").toString();
            agroTerms = loadAgroTerms(options.valueOf("a").toString());

            runMultiAnnualExperiment();
        }
        else {
            logger.error("Invalid arguments");
            System.exit(1);
        }
    }

    private static void runMultiAnnualExperiment() throws RepositoryException, IOException {
        executors = Executors.newCachedThreadPool();

        for (int date=startDate; date<=endDate; date++) {

            // Query triple stores and write feedback.
            Repository repo = getFedRepository(getRepository(date), date);
            queryTripleStores(repo, date);


            // Load feedback
            Collection<QueryLogRecord> logs = parseFeedbackLog("/var/tmp/" + date + "/" + date + "_log.ser");
            Collection<QueryRecord> queryRecords = adaptLogs(logs, date);

            // Refine histogram according to the feedback.
            STHolesHistogram histogram = refineHistogram(queryRecords.iterator(), date);

            // Execute test queries on triple store and refined histogram.
            execTestQueries(repo, histogram, date);
            repo.shutDown();
        }
        executors.shutdown();
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

    private static Iterator<QueryRecord<RDFRectangle,Stat>>
        parseQFR(String path, int year) throws IOException, QueryLogException
    {

        QueryLogParser parser = new SerialQueryLogParser();
        RDFQueryFeedbackProvider qfrProvider = new FileRDFQueryFeedbackProvider(new File(path), parser, getMateralizationManager(year), executors);
        return qfrProvider.getQueryRecordIterator();
    }

    private static Collection<QueryLogRecord> parseFeedbackLog(String path) {
        Collection<QueryLogRecord> logs = new LinkedList<QueryLogRecord>();
        QueryLogRecordCollector handler = new QueryLogRecordCollector(logs);
        QueryLogParser parser = new SerialQueryLogParser();
        parser.setQueryRecordHandler(handler);

        logger.info("Parsing file : " + path);
        try {
            parser.parseQueryLog(new FileInputStream(path));
        } catch (FileNotFoundException e) {e.printStackTrace();
        } catch (QueryLogException e) {e.printStackTrace();
        } catch (IOException e) {e.printStackTrace();}
        logger.info("Number of parsed query logs: " + logs.size());

        return logs;
    }

    private static Collection<QueryRecord> adaptLogs(Collection<QueryLogRecord> logs, int year) {
        Collection<QueryRecord> queryRecords = new LinkedList<QueryRecord>();
        Iterator iter = logs.iterator();

        while (iter.hasNext()) {
            queryRecords.add(new QueryRecordAdapter((QueryLogRecord)iter.next(), getMateralizationManager(year)));
        }

        return queryRecords;
    }

    private static STHolesHistogram refineHistogram(Iterator<QueryRecord> listQueryRecords, int date) {
        STHolesHistogram histogram = loadPreviousHistogram(HISTPATH, date);

        logger.debug("Refining histogram " + date);
        histogram.refine(listQueryRecords);
        logger.debug("Refinement is over.");

        serializeHistogram(histogram, HISTPATH, date);

        return histogram;
    }

    private static void execTestQueries(Repository repo,
                                        STHolesHistogram<RDFRectangle> histogram,
                                        int date) throws RepositoryException, IOException {
        RepositoryConnection conn;
        logger.debug("Executing test queries of: " + date);
        Path path = Paths.get(HISTPATH, "results.csv");
        BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8, options);
        bw.write("Date, Q, QYear, Act, Est, AbsErr%");
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

        long actual = execTripleStore(conn, testQ1str);
        long estim = execHistogram(conn, histogram, testQ1str);
        long error = (Math.abs(actual - estim) * 100) / (Math.max(actual, estim));

        try {
            bw.write(numPass + ", Q1, " + year + ", " +
                    actual + ", " + estim + ", " + error + "%");
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

    private static Repository getFedRepository(Repository actual, int date) throws RepositoryException {
        TestSail sail = new TestSail(actual, date);
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

        File journal = new File(TSPATH + "bigdata_agris_data_" + year + ".jnl");

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
        logger.debug("Serializing histogram to JSON in : " + HISTPATH + "histJSON_" + date + ".txt");
        new JSONSerializer(histogram, HISTPATH + "histJSON_" + date + ".txt");
        logger.debug("Serializing histogram to VOID in : " + HISTPATH + "histVOID_" + date + ".txt");
        new VoIDSerializer("application/x-turtle", HISTPATH + "histVOID_" + date + ".ttl").serialize(histogram);
    }

    private static STHolesHistogram loadPreviousHistogram(String logFolder, int date) {
        File jsonHist = new File(logFolder + "histJSON_" + (date - 1) + ".txt");

        if (!jsonHist.exists())
            logger.debug("Creating a new histogram.");
        else
            logger.debug("Deserializing histogram: " + (date - 1));

        return (!jsonHist.exists())
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

    private static ResultMaterializationManager getMateralizationManager(int year) {
        File baseDir = new File("/var/tmp/" + year + "/");
        TupleQueryResultFormat resultFF = TupleQueryResultFormat.TSV;

        TupleQueryResultWriterRegistry registry = TupleQueryResultWriterRegistry.getInstance();
        TupleQueryResultWriterFactory writerFactory = registry.get(resultFF);
        return new FileManager(baseDir, writerFactory, executors);
    }

}
