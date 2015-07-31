package gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix;

import gr.demokritos.iit.irss.semagrow.sesame.QueryLogInterceptor;
import gr.demokritos.iit.irss.semagrow.tools.Utils;
import info.aduna.iteration.CloseableIteration;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.*;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nickozoulis on 10/11/2014.
 */
public class PrepareTrainingWorkload {

    static final Logger logger = LoggerFactory.getLogger(PrepareTrainingWorkload.class);
    private static URI endpoint = ValueFactoryImpl.getInstance().createURI("http://histogramnamespace/example");
    private static String prefixes = "prefix dc: <http://purl.org/dc/terms/> prefix semagrow: <http://www.semagrow.eu/rdf/> ";
    private static QueryLogInterceptor interceptor;
    private static ExecutorService executors;

    // Setup Parameters
    private static String inputPath;
    private static int numOfQueries;
    // Sparql query to be evaluated
    private static String query = prefixes + "select * where {?sub dc:subject ?obj . filter regex(str(?sub), \"^%s\")}";

    public static void main(String[] args) throws IOException, RepositoryException {
        OptionParser parser = new OptionParser("i:b:");
        OptionSet options = parser.parse(args);

        if (options.hasArgument("i") && options.hasArgument("b")) {
            inputPath = options.valueOf("i").toString();
            numOfQueries = Integer.parseInt(options.valueOf("b").toString());

            executeExperiment();
        } else {
            logger.error("Invalid arguments");
            System.exit(1);
        }
    }

    private static void executeExperiment() throws IOException, RepositoryException {
        executors = Executors.newCachedThreadPool();

        interceptor = new QueryLogInterceptor(Utils.getHandler(), Utils.getMateralizationManager(executors));
        queryStore(Utils.getRepository(inputPath));

        executors.shutdown();
    }

    private static void queryStore(Repository repo) throws IOException, RepositoryException {
        List<String> subjects = loadRandomSubjects();

        logger.info("Starting querying triple store: ");
        RepositoryConnection conn;

        int trimPos = 2;
        String trimmedSubject;

        for (int j=0; j<subjects.size(); j++) {
            logger.info("Query No: " + j);
            try {
                conn = repo.getConnection();

                // This line controls the rate of how fast the querying prefixes should expand.
                // For example if batch is 100 queries, then with {j mod 25} we would end up with 4 different
                // prefix depths.
                if (j % 25 == 0) trimPos++;

                trimmedSubject = Utils.trimSubject(subjects.get(j), trimPos);
                String q = String.format(query, trimmedSubject);
                TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, q);
                logger.info("Query: " + q);

                // Get TupleExpr
                ParsedTupleQuery psq = QueryParserUtil.parseTupleQuery(QueryLanguage.SPARQL, q, "http://example.org/");
                TupleExpr tupleExpr = psq.getTupleExpr();

                // Intercepts the query results.
                CloseableIteration<BindingSet, QueryEvaluationException> result =
                        interceptor.afterExecution(endpoint, tupleExpr, tupleQuery.getBindings(), tupleQuery.evaluate());
                Utils.consumeIteration(result);

                conn.close();
            } catch (MalformedQueryException | RepositoryException | QueryEvaluationException mqe) {
                mqe.printStackTrace();
            }
        }

        repo.shutDown();
    }

    /**
     * Loads distinct repo subjects, from which the training workload will be created.
     * @return
     */
    private static List<String> loadRandomSubjects() {
        List<String> list = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader("/var/tmp/train_subjects/file.txt"));
            String line = "";

            while ((line = br.readLine()) != null)
                list.add(line.trim());

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

}

