package gr.demokritos.iit.irss.semagrow.sesame;

import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by angel on 10/11/14.
 */
public class Workflow {

//    static final String TSHistogramPath = "/mnt/ocfs2/IDF_data/journals/exp_triples/histogram_data/data_";
//
//    static String q = "prefix dc: <http://purl.org/dc/elements/1.1/> " +
//            "prefix semagrow: <http://www.semagrow.eu/rdf/> " +
//            "select * { " +
//            "?pub dc:title ?title ." +
//            "?pub semagrow:year \"1975\" ." +
//            "?pub semagrow:origin \"US\" . }";

    static String q = "select * {<http://agris.fao.org/aos/records/XF7590017> ?p ?o}";
    static final String TSHistogramPath = "/home/nickozoulis/data_";


    static public void main(String[] args) throws RepositoryException, IOException {

        // For all dates from 1975 to 2013
        for (int i=1975; i<=2013; i++) {
            try {
//                RepositoryConnection conn = getFedRepository(getRepository(i)).getConnection();
                RepositoryConnection conn = getRepository(i).getConnection();

                TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, q);

                TupleQueryResult res = query.evaluate();

//                while (res.hasNext()) {
//                    BindingSet bs = res.next();
//                    System.out.println("-- "+bs.toString());
//                }



            } catch (MalformedQueryException mqe) {mqe.printStackTrace();
            } catch (QueryEvaluationException e) {e.printStackTrace();}

            // the evaluation of the query will write logs (query feedback).
            //TODO: Parse the logs and fill an Iterable<RDFQueryRecord> ??

//            STHolesHistogram<RDFRectangle> histogram = new STHolesHistogram();
//            histogram.refine(Iterable);

//            maybe write histogram to file in void or json format

//            compare with actual cardinalities using ActualCardinalityEstimator

            break;//TODO: Remove after testing.
        }

    }


    private static Repository getFedRepository(Repository actual) throws RepositoryException {
        Sail sail = new TestSail(actual);
        Repository repo = new SailRepository(sail);
        repo.initialize();
        return repo;
    }


    private static Repository getRepository(int year) throws RepositoryException, IOException {
        Properties properties = new Properties();

        File journal = new File(TSHistogramPath + year + ".jnl");

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
