package gr.demokritos.iit.irss.semagrow.file;

import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;

/**
 * Created by angel on 10/20/14.
 */
public interface ResultMaterializationManager {

    CloseableIteration<BindingSet,QueryEvaluationException>
        getResult(URI handle) throws QueryEvaluationException;

    MaterializationHandle saveResult() throws QueryEvaluationException;

}
