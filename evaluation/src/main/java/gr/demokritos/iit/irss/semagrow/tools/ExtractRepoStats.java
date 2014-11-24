package gr.demokritos.iit.irss.semagrow.tools;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nickozoulis on 18/11/2014.
 */
public class ExtractRepoStats {

    static final Logger logger = LoggerFactory.getLogger(ExtractRepoStats.class);
    private static ExecutorService executors;
    private static int year;
    private static String inputPath;
    private static final String prefixes = "prefix dc: <http://purl.org/dc/terms/> prefix semagrow: <http://www.semagrow.eu/rdf/> ";
    private final static String queryTriples = prefixes + "select * where {?sub dc:subject ?obj}";
    private final static String queryDistinctSubjects = prefixes + "select distinct ?sub where {?sub dc:subject ?obj}";
    private final static String queryDistinctPredicates = prefixes + "";
    private final static String queryDistinctObjects = prefixes + "select distinct ?obj where {?sub dc:subject ?obj}";


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
        executors = Executors.newCachedThreadPool();
        queryStore(Utils.getRepository(year, inputPath));
        executors.shutdown();
    }

    private static void queryStore(Repository repo) throws IOException, RepositoryException {
        RepositoryConnection conn;

        try {
            conn = repo.getConnection();

            TupleQuery query;
            TupleQueryResult res;
            long c;

            query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryDistinctSubjects);
            res = query.evaluate();

            c = 0;
            logger.info("Counting distinct subjects..");
            while (res.hasNext()) {
                c++;
                res.next();
            }
            logger.info("Distinct subjects:" + c);

            query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryDistinctObjects);
            res = query.evaluate();
            c = 0;
            logger.info("Counting distinct objects..");
            while (res.hasNext()) {
                c++;
                res.next();
            }
            logger.info("Distinct objects:" + c);

            query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryTriples);
            res = query.evaluate();
            c = 0;
            logger.info("Counting triples for year: " + year);
            while (res.hasNext()) {
                c++;
                res.next();
            }
            logger.info("Triples:" + c);

        } catch (NumberFormatException e) {e.printStackTrace();
        } catch (RepositoryException e) {e.printStackTrace();
        } catch (MalformedQueryException e) {e.printStackTrace();
        } catch (QueryEvaluationException e) {e.printStackTrace();
        } catch (Exception e) {e.printStackTrace();}

        repo.shutDown();
    }

}
