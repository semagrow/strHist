package gr.demokritos.iit.irss.semagrow.tools;

import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by nickozoulis on 7/11/2014.
 */
public class FilterDistinctSubjects {

    /*
        Variables for local run.
     */
//    private static String inputPath = "/home/nickozoulis/", output = "/home/nickozoulis/semagrow/distinct.txt";
//    private static int startYear = 1980, endYear = 1980;

    static final Logger logger = LoggerFactory.getLogger(FilterDistinctSubjects.class);
    private static String prefixes = "prefix dc: <http://purl.org/dc/terms/> prefix semagrow: <http://www.semagrow.eu/rdf/> ";
    private static String query = prefixes + "select distinct ?s where {?s dc:subject ?o . }";

    private static String inputPath, output;
    private static int year;

    public static void main(String[] args) {
        OptionParser parser = new OptionParser("y:i:o:");
        OptionSet options = parser.parse(args);

        if (options.hasArgument("y") && options.hasArgument("i") && options.hasArgument("o")) {
            year = Integer.parseInt(options.valueOf("y").toString());
            inputPath = options.valueOf("i").toString();
            output = options.valueOf("o").toString();

            executeFiltering();
        } else {
            logger.error("Invalid arguments");
            System.exit(1);
        }
    }

    private static void executeFiltering() {
        queryStore(initStoreConnection(year));
    }

    private static void queryStore(Repository repo) {
        logger.info("Repetition for year: " + year);
        try {
            RepositoryConnection conn = repo.getConnection();
            logger.info("Repo initialized: " + repo.getDataDir());

            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);

            logger.info("Query repo..");
            TupleQueryResult result = q.evaluate();

            logger.info("Writing to file..");
            writeToFile(result);

            conn.close();
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (MalformedQueryException | QueryEvaluationException e) {
            e.printStackTrace();
        }
    }

    private static void writeToFile(Iteration<BindingSet,QueryEvaluationException> iter) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(output + "subjects_" + year + ".txt"));
            while (iter.hasNext()) {
                BindingSet bs = iter.next();

                bw.write(bs.getBinding("s").getValue().toString());
                bw.newLine();
            }
            Iterations.closeCloseable(iter);
            bw.close();
        } catch (QueryEvaluationException | IOException e) {
            e.printStackTrace();
        }
    }

    private static Repository initStoreConnection(int year) {
        Properties properties = new Properties();

        File journal = new File(inputPath + "bigdata_agris_data_" + year + ".jnl");

        properties.setProperty(
                BigdataSail.Options.FILE,
                journal.getAbsolutePath()
        );

        Repository repo = new BigdataSailRepository(new BigdataSail(properties));
        try {
            repo.initialize();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return repo;
    }

}
