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

    private RDFQueryRecord rdfQueryRecord;
    private boolean isPrefix;


    /**
     * Convert Constructor
     * @param rdfQueryRecord
     * @param isPrefix
     */
    public NumQueryRecord(RDFQueryRecord rdfQueryRecord, boolean isPrefix) {
        this.rdfQueryRecord = rdfQueryRecord;
        this.isPrefix = isPrefix;
    }


    @Override
    public String getQuery() {
        if (isPrefix) {

            List<Integer> listRange = new NumericalMapper(RDFtoNumRectangleConverter.uniqueSubjectData)
                    .getPrefixRange(rdfQueryRecord.getLogQuery().getQueryStatements().get(0).getValue());

            return new IntervalRange(listRange.get(0), listRange.get(1)).toString();
        } else {

            int subjectRow = new NumericalMapper(RDFtoNumRectangleConverter.uniqueSubjectData)
                    .getSubjectRow(rdfQueryRecord.getLogQuery().getQueryStatements().get(0).getValue());

            return new IntervalRange(subjectRow, subjectRow).toString();
        }
    }// getQuery


    @Override
    public NumRectangle getRectangle() {

        ArrayList <IntervalRange> arrayList = new ArrayList<IntervalRange>();

        if (isPrefix) {

            List<Integer> listRange = new NumericalMapper(RDFtoNumRectangleConverter.uniqueSubjectData)
                    .getPrefixRange(rdfQueryRecord.getLogQuery().getQueryStatements().get(0).getValue());

            arrayList.add(new IntervalRange(listRange.get(0), listRange.get(1)));
        } else {

            int subjectRow = new NumericalMapper(RDFtoNumRectangleConverter.uniqueSubjectData)
                    .getSubjectRow(rdfQueryRecord.getLogQuery().getQueryStatements().get(0).getValue());

            arrayList.add(new IntervalRange(subjectRow, subjectRow));
        }

        return new NumRectangle(arrayList);
    }// getRectangle


    @Override
    public QueryResult getResultSet() {
        return new NumQueryResult(rdfQueryRecord.getResultSet(), isPrefix);
    }

}
