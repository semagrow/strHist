package gr.demokritos.iit.irss.semagrow;

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

        s += "Subject: " + queryStatements.get(0).toString() + "\n";
        s += "Predicate: " + queryStatements.get(1).toString() + "\n";
        s += "Object: " + queryStatements.get(2).toString() + "\n";

        return s;
    }

}
