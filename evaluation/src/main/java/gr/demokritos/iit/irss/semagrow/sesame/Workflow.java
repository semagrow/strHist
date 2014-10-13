package gr.demokritos.iit.irss.semagrow.sesame;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by angel on 10/11/14.
 */
public class Workflow {


    static public void main(String[] args) {

        //TODO:
        // for all different versions of agris repository
        // Repository actualRepository = getRepository()
        // Repository repo = getFedRepository(actualRepository).

        // for all queries q in the set
        // RepositoryConnection conn = repo.getConnection();
        // TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, q);
        // query.evaluate()
        // the evaluation of the query will write logs (query feedback).
        // histogram.refine(query feedback).
        // compare with actual cardinalities using ActualCardinalityEstimator
    }


    private Repository getFedRepository(Repository actual) throws RepositoryException {
        Sail sail = new TestSail(actual);
        Repository repo =  new SailRepository(sail);
        repo.initialize();
        return repo;
    }

    // get the actual repository that provide the data
    private Repository getRepository() throws RepositoryException, IOException {

        /*
        // use one of our pre-configured option-sets or "modes"
        Properties properties =
                sampleCode.loadProperties("fullfeature.properties");

        // create a backing file for the database
        File journal = null;

        journal = File.createTempFile("bigdata", ".jnl");

        properties.setProperty(
                BigdataSail.Options.FILE,
                journal.getAbsolutePath()
        );

        // instantiate a sail and a Sesame repository
        BigdataSail sail = new BigdataSail(properties);
        Repository repo = new BigdataSailRepository(sail);
        repo.initialize();
        return repo;
        */
        return null;
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
