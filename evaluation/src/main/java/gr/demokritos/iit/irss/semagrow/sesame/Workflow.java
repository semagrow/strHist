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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Created by angel on 10/11/14.
 */
public class Workflow {

//    private static String q = "prefix dc: <http://purl.org/dc/elements/1.1/> " +
//            "prefix semagrow: <http://www.semagrow.eu/rdf/> " +
//            "select * { " +
//            "?pub dc:title ?title ." +
//            "?pub semagrow:year \"%s\" ." +
//            "?pub semagrow:origin \"%s\" . }";
//    // Use it like this : String.format(q, "2012", "US");
//
//    private static int startDate, endDate;
//    private static String tripleStorePath = "/mnt/ocfs2/IDF_data/journals/exp_triples/histogram_data/data_";
//    public static String logOutputPath;

    static String q = "select * {<http://agris.fao.org/aos/records/XF7590017> ?p ?o}";
    static final String tripleStorePath = "/home/nickozoulis/data_";
    static final String logOutputPath = "/home/nickozoulis/strhist_exp_logs/semagrow_logs.log";

    public static RDFSTHolesHistogram histogram;

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
        for (int i=1975; i<=1975; i++) {
            // -- Query Evaluation
            try {
                RepositoryConnection conn = getFedRepository(getRepository(i)).getConnection();

                TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, q);

                TupleQueryResult result = query.evaluate();
                consumeIteration(result);

            } catch (MalformedQueryException | QueryEvaluationException mqe) {
                mqe.printStackTrace();
            }

            // -- Histogram Training

//            // The evaluation of the query will write logs (query feedback).
            List<RDFQueryRecord> listQueryRecords = new LogParser(logOutputPath).parse();
//
            histogram.refine(listQueryRecords);
            System.out.println(histogram.getRoot().toString());
            new JSONSerializer(histogram, "/home/nickozoulis/strhist_exp_logs/histJSON.txt");
//            Maybe write histogram to file in void or json format

            // -- Histogram Testing

//            Compare with actual cardinalities using ActualCardinalityEstimator


        }
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

}
