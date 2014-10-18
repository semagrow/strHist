package gr.demokritos.iit.irss.semagrow.sesame;

import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import gr.demokritos.iit.irss.semagrow.rdf.RDFSTHolesHistogram;
import gr.demokritos.iit.irss.semagrow.rdf.io.json.JSONSerializer;
import gr.demokritos.iit.irss.semagrow.rdf.io.log.LogParser;
import gr.demokritos.iit.irss.semagrow.rdf.io.log.RDFQueryRecord;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by angel on 10/11/14.
 */
public class Workflow {

    private static String agroTermsPath = "agrovoc_terms.txt";
    private static String prefixes = "prefix dc: <http://purl.org/dc/terms/> prefix sg: <http://www.semagrow.eu/rdf/> ";
    private static String q = prefixes + "select * {?u dc:subject <%s> . ?u sg:year ?y} limit 1";

    //TODO: Isws thelei count
    private static String testQ1 = prefixes + "select * {?x semagrow:year 1980 . }";

    private static String testQ2 = prefixes + "select * {?x semagrow:year 1980 . } filter regex(\".*US\"), str(?x)";

    // Use it like this : String.format(q, "2012", "US");
//
//    private static int startDate, endDate;
    private static String tripleStorePath = "/home/nickozoulis/Downloads/bigdata_agris_data_";
//    public static String logOutputPath;

//    static String q = "select * {?uu dc:subject <tt> . ??uu sg:year ?yy }";
//    static final String tripleStorePath = "/home/nickozoulis/data_";
    static final String logOutputPath = "/home/nickozoulis/strhist_exp_logs/semagrow_logs.log";

    public static RDFSTHolesHistogram histogram;

    private static List<String> agroTerms =
            loadAgrovocTerms("/home/nickozoulis/git/sthist/evaluation/src/test/resources/agrovoc_terms.txt");
    private static int term = 0;

    /**
     * s = Starting date, e = Ending Date, l = LogOutput path
     * @param args
     * @throws RepositoryException
     * @throws IOException
     */
    static public void main(String[] args) throws RepositoryException, IOException, NumberFormatException {

//        OptionParser parser = new OptionParser("s:e:l:");
//        OptionSet options = parser.parse(args);
//
//        if (options.hasArgument("s") && options.hasArgument("e") && options.hasArgument("l")) {
//            startDate = Integer.parseInt(options.valueOf("s").toString());
//            endDate = Integer.parseInt(options.valueOf("e").toString());
//            if (startDate > endDate) System.exit(1);
//            logOutputPath = options.valueOf("l").toString();
//        }
//        else System.exit(1);

        runExperiment();
    }

    private static void runExperiment() throws RepositoryException, IOException {
        for (int i=1980; i<=1980; i++) {
            // -- Query Evaluation
            Repository repo = getFedRepository(getRepository(1980));

            // For some agroTerms
            for (int j=0; j<50; j++) {

                try {
                    RepositoryConnection conn = repo.getConnection();
                    String quer = String.format(q, agroTerms.get(term++));
                    System.out.println(">< TrainQuery: " + quer);
                    TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, quer);

                    TupleQueryResult result = query.evaluate();
                    consumeIteration(result);

                } catch (MalformedQueryException | QueryEvaluationException mqe) {
                    mqe.printStackTrace();
                }

                // -- Histogram Training

//            // The evaluation of the query will write logs (query feedback).
                List<RDFQueryRecord> listQueryRecords = new LogParser(logOutputPath).parse();
                System.out.println("---<");
                if (listQueryRecords.size() > 0) {
                    histogram.refine(listQueryRecords);
                    System.out.println("--->\n\n" + histogram.getRoot().toString());

//            Maybe write histogram to file in void or json format
                    new JSONSerializer(histogram, "/home/nickozoulis/strhist_exp_logs/histJSON.txt");

                    // -- Histogram Testing

//            Compare with actual cardinalities using ActualCardinalityEstimator
//             execTestQueries();
                }
            }
        }
    }

    private static void execTestQueries() {
        execHistogram();
        execTripleStore();
    }

    private static void execHistogram() {
        try {
            RepositoryConnection conn = getFedRepository(getRepository(1980)).getConnection();

            TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, testQ1);

            TupleQueryResult result = query.evaluate();


        } catch (MalformedQueryException | QueryEvaluationException | RepositoryException | IOException mqe) {
            mqe.printStackTrace();
        }

    }

    private static void execTripleStore() {

    }

    private static Repository getFedRepository(Repository actual) throws RepositoryException {
        TestSail sail = new TestSail(actual);
        histogram = sail.getHistogram();
        Repository repo = new SailRepository(sail);
        repo.initialize();
        return repo;
    }

    private static void consumeIteration(Iteration<BindingSet,QueryEvaluationException> iter) throws QueryEvaluationException {
        while (iter.hasNext())
            iter.next();

        Iterations.closeCloseable(iter);
    }

    private static Repository getRepository(int year) throws RepositoryException, IOException {
        Properties properties = new Properties();

        File journal = new File(tripleStorePath + year + ".jnl");

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

       private static List<String> loadAgrovocTerms(String path) {
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
