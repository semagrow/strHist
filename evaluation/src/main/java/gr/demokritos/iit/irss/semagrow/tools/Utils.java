package gr.demokritos.iit.irss.semagrow.tools;

import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import eu.semagrow.querylog.api.QueryLogException;
import eu.semagrow.querylog.api.QueryLogHandler;
import eu.semagrow.querylog.api.QueryLogParser;
import eu.semagrow.querylog.api.QueryLogRecord;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.base.Stat;
import gr.demokritos.iit.irss.semagrow.base.range.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.file.FileManager;
import gr.demokritos.iit.irss.semagrow.file.ResultMaterializationManager;
import eu.semagrow.querylog.QueryLogCollector;
import gr.demokritos.iit.irss.semagrow.impl.serial.SerialQueryLogFactory;
import gr.demokritos.iit.irss.semagrow.impl.serial.SerialQueryLogParser;
import gr.demokritos.iit.irss.semagrow.qfr.QueryRecordAdapter;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.RDFSTHolesHistogram;
import gr.demokritos.iit.irss.semagrow.rdf.RDFURIRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFValueRange;
import gr.demokritos.iit.irss.semagrow.rdf.io.json.JSONDeserializer;
import gr.demokritos.iit.irss.semagrow.rdf.io.json.JSONSerializer;
import gr.demokritos.iit.irss.semagrow.rdf.io.sevod.VoIDSerializer;
import gr.demokritos.iit.irss.semagrow.sesame.ActualCardinalityEstimator;
import gr.demokritos.iit.irss.semagrow.sesame.CardinalityEstimatorImpl;
import gr.demokritos.iit.irss.semagrow.sesame.TestSail;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesBucket;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;
import org.json.simple.parser.ParseException;
import org.openrdf.model.URI;
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
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Created by nickozoulis on 11/11/2014.
 */
public class Utils {

    static final Logger logger = LoggerFactory.getLogger(Utils.class);
    private static Random rand = new Random(1);
    private static final String prefixes = "prefix dc: <http://purl.org/dc/terms/> prefix semagrow: <http://www.semagrow.eu/rdf/> ";

    public static Collection<QueryLogRecord> parseFeedbackLog(String path) {
        Collection<QueryLogRecord> logs = new LinkedList<QueryLogRecord>();
        QueryLogCollector handler = new QueryLogCollector(logs);
        QueryLogParser parser = new SerialQueryLogParser();
        parser.setQueryRecordHandler(handler);

        logger.info("Parsing file : " + path);
        try {
            parser.parseQueryLog(new FileInputStream(path));
        } catch (QueryLogException | IOException e) {e.printStackTrace();}
        logger.info("Number of parsed query logs: " + logs.size());

        return logs;
    }

    public static Collection<QueryRecord> adaptLogs(Collection<QueryLogRecord> logs, ExecutorService executors) {
        Collection<QueryRecord> queryRecords = new LinkedList<QueryRecord>();
        Iterator iter = logs.iterator();

        while (iter.hasNext())
            queryRecords.add(new QueryRecordAdapter((QueryLogRecord)iter.next(), getMateralizationManager(executors)));

        return queryRecords;
    }

    public static void serializeHistogram(STHolesHistogram histogram, String path) {
        logger.info("Serializing histogram to JSON histJSON.txt");
        try {
            new JSONSerializer(histogram, path + "histJSON.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Serializing histogram to VOID in : " + path + "histVOID.txt");
        try {
            new VoIDSerializer("application/x-turtle", path + "histVOID.ttl").serialize(histogram);
        } catch (RDFHandlerException e) {
            e.printStackTrace();
        }
    }

    public static RDFSTHolesHistogram loadPreviousHistogram(String logFolder) {
        File jsonHist = new File(logFolder + "histJSON.txt");

        if (!jsonHist.exists())
            logger.info("Creating a new histogram.");
        else
            logger.info("Deserializing previous histogram: ");

        try {
            return (!jsonHist.exists())
                    ? new RDFSTHolesHistogram(getFixedRoot())
                    : new JSONDeserializer(logFolder + "histJSON.txt").getHistogram();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Fixed root for bigdata_agris_data_2009.jnl
     * @return
     */
    private static STHolesBucket getFixedRoot() {
        RDFRectangle box = new RDFRectangle(new RDFURIRange(), new ExplicitSetRange<URI>(), new RDFValueRange());

        List<Long> list = new ArrayList<>();
        list.add((long)2576877);//2004: 2318520
        list.add((long)1);
        list.add((long)20692);//2004: 19709

        Stat stats = new Stat((long) 17447544, list);// 2004: 15371754

        return new STHolesBucket(box, stats);
    }

    public static RDFSTHolesHistogram loadCurrentHistogram(String logFolder) {
        logger.info("Deserializing current histogram: ");

        try {
            return new JSONDeserializer(logFolder + "histJSON.txt").getHistogram();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> loadAgroTerms(String path) {
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
            File file = new File(path);
            LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
            lineNumberReader.skip(Long.MAX_VALUE);
            lines = lineNumberReader.getLineNumber();
            lineNumberReader.close();

        } catch (IOException e) {e.printStackTrace();}

        return lines;
    }

    public static ResultMaterializationManager getMateralizationManager(ExecutorService executors) {
        File baseDir = new File("/var/tmp/");
        TupleQueryResultFormat resultFF = TupleQueryResultFormat.TSV;

        TupleQueryResultWriterRegistry registry = TupleQueryResultWriterRegistry.getInstance();
        TupleQueryResultWriterFactory writerFactory = registry.get(resultFF);
        return new FileManager(baseDir, writerFactory, executors);
    }

    public static QueryLogHandler getHandler() {
        QueryLogHandler handler = null;
        SerialQueryLogFactory factory = new SerialQueryLogFactory();

        try {
            File dir = new File("/var/tmp/");
            if (!dir.exists())
                dir.mkdir();

            File qfrLog = new File("/var/tmp/_log.ser");
            OutputStream out = new FileOutputStream(qfrLog, true);
            handler = factory.getQueryRecordLogger(out);
        } catch (IOException e) {e.printStackTrace();}

        return handler;
    }

    public static Repository getRepository(String inputPath) throws RepositoryException, IOException {
        Properties properties = new Properties();

        File journal = new File(inputPath + "bigdata_agris_data_.jnl");

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

    public static Repository getFedRepository(Repository actual, int date) throws RepositoryException {
        TestSail sail = new TestSail(actual, date);
        Repository repo = new SailRepository(sail);
        repo.initialize();
        return repo;
    }

    public static String loadDistinctSubject(int num, String path) throws IOException {
        String subject = "";

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path + "subjects_.txt"));
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

    public static String trimSubject(String subject, int trimPos) {
        String[] splits = subject.split("/");
        String lastSlashPrefix = splits[splits.length - 1];

        // Get random cut on the prefix. 3 is given to avoid memory heap overflow
//        int randomCut = randInt(2, lastSlashPrefix.length() - 3);

        String trimmedSubject = "";
        // Reform the trimmed subject. Intentionally exclude the last one.
        for (int i=0; i<splits.length - 1; i++) {
            trimmedSubject += splits[i] + "/";
        }

        // Append the random cut.
        trimmedSubject += lastSlashPrefix.substring(0, trimPos);

        return trimmedSubject;
    }

    public static void consumeIteration(Iteration<BindingSet,QueryEvaluationException> iter) throws QueryEvaluationException {
        int n = 0;
        logger.info("Consuming items.");
        while (iter.hasNext()) {
            iter.next();
            ++n;
        }
        logger.info("Iterated over " + n + " items.");
        Iterations.closeCloseable(iter);
    }


    public static long evaluateOnHistogram(RepositoryConnection conn, RDFSTHolesHistogram histogram, String query) {
        try {
            logger.info("Cardinality estimation on Histogram for query: " + query);
            ParsedTupleQuery q = QueryParserUtil.parseTupleQuery(QueryLanguage.SPARQL, query, "http://example.org/");

            long card = new CardinalityEstimatorImpl(histogram).
                    getCardinality(q.getTupleExpr(), EmptyBindingSet.getInstance());

            return card;
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static long evaluateOnTripleStore(RepositoryConnection conn, String query) {
        try {
            logger.info("Cardinality estimation on Triple Store for query: " + query);
            ParsedTupleQuery q = QueryParserUtil.parseTupleQuery(QueryLanguage.SPARQL, query, "http://example.org/");

            long card = new ActualCardinalityEstimator(conn).
                    getCardinality(q.getTupleExpr(), EmptyBindingSet.getInstance());

            return card;

        } catch (MalformedQueryException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static Set getSamplingRows(String filePath, double percentage) {
        Set set = new HashSet<Integer>();

        int lines = countLineNumber(filePath);

        while (set.size() < lines * percentage)
            set.add(randInt(0, lines));

        return set;
    }

    public static long countRepoTriples(RepositoryConnection conn) {
        return countQueryRepo(conn, prefixes + "select * where {?sub dc:subject ?obj}");
    }

    public static long countDistinctRepoSubjects(RepositoryConnection conn) {
        return countQueryRepo(conn, prefixes + "select distinct ?sub where {?sub dc:subject ?obj}");
    }

    public static long countDistinctRepoObjects(RepositoryConnection conn) {
        return countQueryRepo(conn, prefixes + "select distinct ?obj where {?sub dc:subject ?obj}");
    }

    private static long countQueryRepo(RepositoryConnection conn, String queryStr) {
        TupleQuery query;
        TupleQueryResult res;
        long c = 0;

        try {
            query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryStr);
            res = query.evaluate();
            while (res.hasNext()) {
                c++;
                res.next();
            }
        } catch (NumberFormatException e) {e.printStackTrace();
        } catch (RepositoryException e) {e.printStackTrace();
        } catch (MalformedQueryException e) {e.printStackTrace();
        } catch (QueryEvaluationException e) {e.printStackTrace();
        } catch (Exception e) {e.printStackTrace();}

        return c;
    }

    public static List<String> selectSubjectsByRows(RepositoryConnection conn, List<Long> rows) {
        Collections.sort(rows);
        List<String> listSubjects = new ArrayList<>();

        String queryStr = prefixes + "select ?sub where {?sub dc:subject ?obj}";
        TupleQuery query;
        TupleQueryResult res;
        long c = 0;

        try {
            query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryStr);
            res = query.evaluate();
            BindingSet bs;
            while (res.hasNext()) {
                bs = res.next();

                if (!rows.isEmpty() && c++ == rows.get(0)) {
                    if (bs.hasBinding("sub"))
                        listSubjects.add(bs.getValue("sub").stringValue());
                    rows.remove(0);
                }
            }
        } catch (NumberFormatException e) {e.printStackTrace();
        } catch (RepositoryException e) {e.printStackTrace();
        } catch (MalformedQueryException e) {e.printStackTrace();
        } catch (QueryEvaluationException e) {e.printStackTrace();
        } catch (Exception e) {e.printStackTrace();}

        return listSubjects;
    }

}
