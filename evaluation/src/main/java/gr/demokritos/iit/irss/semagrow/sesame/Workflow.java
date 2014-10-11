package gr.demokritos.iit.irss.semagrow.sesame;

import org.openrdf.repository.Repository;
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


    static public void main(String[] args) {
        //TODO:
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
}
