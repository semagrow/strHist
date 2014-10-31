package gr.demokritos.iit.irss.semagrow.generator;

import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.sesame.TestSail;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/** Generates query feedback by asking a "real" repository
 * Created by angel on 10/31/14.
 */
public class RepositoryQueryFeedbackGenerator implements QueryFeedbackGenerator {

    private Repository repository;
    private Repository wrappedRepository;

    private Logger logger = LoggerFactory.getLogger(RepositoryQueryFeedbackGenerator.class);

    public RepositoryQueryFeedbackGenerator(Repository repository) {
        this.wrappedRepository = repository;
        initialize();
    }

    private void initialize() {
        repository = new SailRepository(new TestSail(wrappedRepository, 0));
    }

    public void generate() {
        try {
            RepositoryConnection connection = repository.getConnection();
            String sparqlQuery = "";

            TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
            TupleQueryResult result = query.evaluate();
            consumeIteration(result);
            connection.close();

        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Iterator<QueryRecord> getQueryRecordIterator() {
        return null;
    }


    private void consumeIteration(Iteration<BindingSet,QueryEvaluationException> iter)
            throws QueryEvaluationException
    {
        int n = 0;
        logger.debug("Consuming items.");
        while (iter.hasNext()) {
            iter.next();
            ++n;
        }
        logger.debug("Iterated over " + n + " items." );
        Iterations.closeCloseable(iter);
    }
}
