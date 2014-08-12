package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryResult;
import gr.demokritos.iit.irss.semagrow.base.NumRectangle;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import gr.demokritos.iit.irss.semagrow.rdf.qfr.RDFQueryRecord;
import gr.demokritos.iit.irss.semagrow.tools.NumericalMapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
