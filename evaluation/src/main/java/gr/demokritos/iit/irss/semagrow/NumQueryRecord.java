package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryResult;
import gr.demokritos.iit.irss.semagrow.base.NumRectangle;

import java.io.Serializable;

/**
 * Created by Nick on 11-Aug-14.
 */
public class NumQueryRecord implements QueryRecord<NumRectangle, Long>, Serializable {

    private NumQuery numQuery;



    public NumQueryRecord(NumQuery numQuery) {
        this.numQuery = numQuery;
    }



    public String getQuery() {
        return numQuery.toString();
    }// getQuery

    public NumQuery getNumQuery() { return numQuery; }


    public NumRectangle getRectangle() {
        return new NumRectangle(numQuery.getQueryStatements());
    }// getRectangle



    public QueryResult getResultSet() {
        return new NumQueryResult(numQuery);
    }

}
