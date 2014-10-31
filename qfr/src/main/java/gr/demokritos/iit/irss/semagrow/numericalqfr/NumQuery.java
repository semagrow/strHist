package gr.demokritos.iit.irss.semagrow.numericalqfr;

import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Nick on 11-Aug-14.
 */
public class NumQuery implements Serializable {

    private List<IntervalRange> queryStatements;
    private List<List<IntervalRange>> queryResults;


    public NumQuery(List<IntervalRange> queryStatements, List<List<IntervalRange>> queryResults) {
        this.queryStatements = queryStatements;
        this.queryResults = queryResults;
    }


    public List<IntervalRange> getQueryStatements() {
        return queryStatements;
    }

    public List<List<IntervalRange>> getQueryResults() {
        return queryResults;
    }


    @Override
    public String toString() {

        String s = "";

        for (int i = 0; i < queryStatements.size(); i++) {
            s += "[" + queryStatements.get(i).getLow() + "-" + queryStatements.get(i).getHigh() + "]\t";
        }

        return s;
    }

}
