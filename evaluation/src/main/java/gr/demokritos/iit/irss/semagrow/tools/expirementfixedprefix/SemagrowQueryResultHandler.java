package gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;

import java.util.List;

/**
 * Created by katerina on 24/9/2015.
 */
public class SemagrowQueryResultHandler implements TupleQueryResultHandler {

    private long count = 0;
    private long startTime = System.currentTimeMillis();

    public long getCount() { return count; }
    public long getTime() { return startTime; }

    @Override
    public void handleBoolean(boolean b) throws QueryResultHandlerException {

    }

    @Override
    public void handleLinks(List<String> list) throws QueryResultHandlerException {

    }

    @Override
    public void startQueryResult(List<String> list) throws TupleQueryResultHandlerException {

    }

    @Override
    public void endQueryResult() throws TupleQueryResultHandlerException {
        startTime = System.currentTimeMillis() - startTime;
    }

    @Override
    public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
        count++;
    }
}
