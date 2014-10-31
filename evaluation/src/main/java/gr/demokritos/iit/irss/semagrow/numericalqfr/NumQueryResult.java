package gr.demokritos.iit.irss.semagrow.numericalqfr;

import gr.demokritos.iit.irss.semagrow.api.qfr.QueryResult;
import gr.demokritos.iit.irss.semagrow.base.NumRectangle;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 11-Aug-14.
 */
public class NumQueryResult implements QueryResult<NumRectangle, Long>, Serializable {

    private NumQuery numQuery;
    private List<NumRectangle> resultNumRectangles;


    public NumQueryResult(NumQuery numQuery) {

        this.numQuery = numQuery;
        resultNumRectangles = instantiateNumRectangles();
    }


    @Override
    public Long getCardinality(NumRectangle rect) {

        long cardinality = 0;

        for (NumRectangle nr : resultNumRectangles)
            if (rect.contains(nr))
                cardinality++;

        return cardinality;
    }// getCardinality


    public List<NumRectangle> getRectangles() {
        return getRectangles(null);
    }

    @Override
    public List<NumRectangle> getRectangles(NumRectangle rect) {

        List<NumRectangle> list = new ArrayList<NumRectangle>();

        for (NumRectangle nr : resultNumRectangles)
            if (rect.contains(nr))
                list.add(nr);

        return list;
    }// getRectangles


    private List<NumRectangle> instantiateNumRectangles() {

        ArrayList<NumRectangle> numRectangles = new ArrayList<NumRectangle>();

        for (List<IntervalRange> resultBindings : numQuery.getQueryResults())
            numRectangles.add(new NumRectangle(resultBindings));

        return numRectangles;
    }// instantiateNumRectangles


    public List<NumRectangle> getResultNumRectangles() {
        return resultNumRectangles;
    }

}
