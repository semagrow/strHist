package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.api.qfr.QueryResult;
import gr.demokritos.iit.irss.semagrow.base.NumRectangle;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import gr.demokritos.iit.irss.semagrow.rdf.parsing.BindingSet;
import gr.demokritos.iit.irss.semagrow.rdf.qfr.RDFQueryResult;
import gr.demokritos.iit.irss.semagrow.tools.NumericalMapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 11-Aug-14.
 */
public class NumQueryResult implements QueryResult<NumRectangle, Long>, Serializable {

    private RDFQueryResult rdfQueryResult;
    private boolean isPrefix;
    private List<NumRectangle> resultNumRectangles;


    public NumQueryResult(RDFQueryResult rdfQueryResult, boolean isPrefix) {
        this.rdfQueryResult = rdfQueryResult;
        this.isPrefix = isPrefix;

        // Convert all resultRDFRectangles to resultNUMRectangles.
        resultNumRectangles = instantiateNumRectangles();
    }// Constructor


    @Override
    public Long getCardinality(NumRectangle rect) {

        long cardinality = 0;

        for (NumRectangle nr : resultNumRectangles)
            if (rect.contains(nr))
                cardinality++;

        return cardinality;
    }// getCardinality


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
        NumRectangle numRectangle = null;
        ArrayList <IntervalRange> listIntervalRange;

        // Foreach BindingSet get the Subject(Prefix/Subject) and instantiate a NumRectangle.
        for (BindingSet bs : rdfQueryResult.getBindingSets()) {

            listIntervalRange = new ArrayList<IntervalRange>();
            if (isPrefix) {

                List<Integer> listRange = new NumericalMapper(RDFtoNumRectangleConverter.uniqueSubjectData)
                        .getPrefixRange(bs.getBindings().get(0).getValue());

                listIntervalRange.add(new IntervalRange(listRange.get(0), listRange.get(1)));
            } else {

                int subjectRow = new NumericalMapper(RDFtoNumRectangleConverter.uniqueSubjectData)
                        .getSubjectRow(bs.getBindings().get(0).getValue());

                listIntervalRange.add(new IntervalRange(subjectRow, subjectRow));
            }

            numRectangle = new NumRectangle(listIntervalRange);
            numRectangles.add(numRectangle);
        }// for

        return numRectangles;
    }// instantiateNumRectangles


    public List<NumRectangle> getResultNumRectangles() {
        return resultNumRectangles;
    }

}
