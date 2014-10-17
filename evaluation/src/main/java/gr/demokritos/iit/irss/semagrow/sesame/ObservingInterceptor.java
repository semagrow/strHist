package gr.demokritos.iit.irss.semagrow.sesame;

import eu.semagrow.stack.modules.api.evaluation.QueryEvaluationSession;
import eu.semagrow.stack.modules.sails.semagrow.evaluation.iteration.ObservingIteration;
import gr.demokritos.iit.irss.semagrow.logging.LoggerWithQueue;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by angel on 6/30/14.
 * @author Angelos Charalampidis
 * @author Giannis Mouchakis
 */
public class ObservingInterceptor {
	
	BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(1000000);
    LoggerWithQueue logWritter = new LoggerWithQueue(queue);
    Thread logWritterThread = new Thread(logWritter);
    
    final Logger logger = LoggerFactory.getLogger(ObservingInterceptor.class);

    public String session;

    public ObservingInterceptor(String session) { this.session = session; }

    public CloseableIteration<BindingSet, QueryEvaluationException>
        afterExecution(URI endpoint, TupleExpr expr, BindingSet bindings, CloseableIteration<BindingSet, QueryEvaluationException> result) {

        QueryMetadata metadata = createMetadata(endpoint, expr, bindings.getBindingNames());
        return observe(metadata, result);
    }

    public CloseableIteration<BindingSet, QueryEvaluationException>
        afterExecution(URI endpoint, TupleExpr expr, CloseableIteration<BindingSet, QueryEvaluationException> bindingIter, CloseableIteration<BindingSet, QueryEvaluationException> result) {

        List<BindingSet> bindings = Collections.<BindingSet>emptyList();

        try {
            bindings = Iterations.asList(bindingIter);
        } catch (Exception e) {

        }

//        bindingIter = new CollectionIteration<BindingSet, QueryEvaluationException>(bindings);

        Set<String> bindingNames = (bindings.size() == 0) ? new HashSet<String>() : bindings.get(0).getBindingNames();

        QueryMetadata metadata = createMetadata(endpoint, expr, bindingNames);

        return observe(metadata, result);
    }


    public CloseableIteration<BindingSet, QueryEvaluationException>
        observe(QueryMetadata metadata, CloseableIteration<BindingSet, QueryEvaluationException> iter) {
    	if ( ! logWritterThread.isAlive()) {
        	logWritterThread.start();
        }
        return new QueryObserver(metadata, iter);
    }


    protected QueryMetadata createMetadata(URI endpoint, TupleExpr expr, Set<String> bindingNames) {
        return new QueryMetadata(session, endpoint, expr, bindingNames);
    }

    private class QueryMetadata {

        private String session;

        private TupleExpr query;

        private URI endpoint;

        private List<String> bindingNames;

        public QueryMetadata(String session, URI endpoint, TupleExpr query) {
            this.session = session;
            this.endpoint = endpoint;
            this.query = query;
            this.bindingNames = new LinkedList<String>();
        }

        public QueryMetadata(String session, URI endpoint, TupleExpr query, Collection<String> bindingNames) {
            this.session = session;
            this.endpoint = endpoint;
            this.query = query;
            this.bindingNames = new LinkedList<String>(bindingNames);
        }

        public URI getEndpoint() { return endpoint; }

        public TupleExpr getQuery() { return query; }

        public String getSessionId() { return session; }

        public List<String> getBindingNames() { return bindingNames; }
    }

    protected class QueryObserver extends ObservingIteration<BindingSet,QueryEvaluationException> {

        private QueryMetadata metadata;
        
        public QueryObserver(QueryMetadata metadata, Iteration<BindingSet, QueryEvaluationException> iter) {
            super(iter);
            this.metadata = metadata;
            try {
				queue.put(metadata.getSessionId());
				queue.put(Long.toString(System.currentTimeMillis()));
				queue.put(metadata.getEndpoint());
				queue.put("@");
				queue.put(metadata.getQuery());
				queue.put("@");
				queue.put(metadata.bindingNames);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }

		@Override
		public void observe(BindingSet bindings) {
			try {
				queue.put(bindings.getBindingNames().size());
				queue.put(bindings);
				for (String name : metadata.bindingNames) {
					queue.put(bindings.getValue(name).stringValue());
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void observeExceptionally(QueryEvaluationException x) {
			//logWritter.finish();//TODO:log this? 
			
		}
		
        /*
        @Override
        public void handleClose() throws QueryEvaluationException {
        	
        }
        */
		
    }

}
