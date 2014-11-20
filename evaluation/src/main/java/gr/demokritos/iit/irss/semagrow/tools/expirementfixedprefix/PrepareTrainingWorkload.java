package gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix;

import gr.demokritos.iit.irss.semagrow.impl.QueryLogInterceptor;
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

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nickozoulis on 10/11/2014.
 */
public class PrepareTrainingWorkload {
    /*
        Variables for local run
     */
//    private static int year = 1977, numOfQueries = 200;
//    private static String inputPath = "/home/nickozoulis/Desktop/res_prefix/";

    static final Logger logger = LoggerFactory.getLogger(PrepareTrainingWorkload.class);
    private static URI endpoint = ValueFactoryImpl.getInstance().createURI("http://histogramnamespace/example");
    private static final String DISTINCTPath = "/var/tmp/distinct/";
    private static String prefixes = "prefix dc: <http://purl.org/dc/terms/> prefix semagrow: <http://www.semagrow.eu/rdf/> ";
    private static String query = prefixes + "select * where {?sub dc:subject ?obj . filter regex(str(?sub), \"^%s\")}";
    private static QueryLogInterceptor interceptor;
    private static ExecutorService executors;

    private static String inputPath;
    private static int year, numOfQueries;


    public static void main(String[] args) throws IOException, RepositoryException {
        OptionParser parser = new OptionParser("y:i:b:");
        OptionSet options = parser.parse(args);

        if (options.hasArgument("y") && options.hasArgument("i") && options.hasArgument("b")) {
            year = Integer.parseInt(options.valueOf("y").toString());
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

        interceptor = new QueryLogInterceptor(Utils.getHandler(year), Utils.getMateralizationManager(year, executors));
        queryStore(Utils.getRepository(year, inputPath));

        executors.shutdown();
    }

    private static void queryStore(Repository repo) throws IOException, RepositoryException {
        int subjectsNum = Utils.countLineNumber(DISTINCTPath + "subjects_" + year + ".txt");
        String trimmedSubject;

        logger.info("Starting quering triple store: " + year);
        RepositoryConnection conn = null;

        int trimPos = 5;

        for (int j=0; j<numOfQueries; j++) {
            logger.info("Query No: " + j);
            try {
                conn = repo.getConnection();

                if (j % 25 == 0) trimPos++;

                trimmedSubject = Utils.trimSubject(Utils.loadDistinctSubject(Utils.randInt(0, subjectsNum), year, DISTINCTPath), trimPos);
                String q = String.format(query, trimmedSubject);
                TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, q);
                logger.info("Query: " + q);

                // Get TupleExpr
                ParsedTupleQuery psq = QueryParserUtil.parseTupleQuery(QueryLanguage.SPARQL, q, "http://example.org/");
                TupleExpr tupleExpr = psq.getTupleExpr();

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

}

