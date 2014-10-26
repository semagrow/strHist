package gr.demokritos.iit.irss.semagrow.tools;

import gr.demokritos.iit.irss.semagrow.NumQuery;
import gr.demokritos.iit.irss.semagrow.NumQueryRecord;
import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.io.log.BindingSet;
import gr.demokritos.iit.irss.semagrow.rdf.io.log.RDFQueryRecord;
import org.openrdf.model.URI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by efi on 18/10/2014.
 */
public class RDFToNumQueryRecordConverter {


    /**
     * Empty Constructor
     */
    public RDFToNumQueryRecordConverter() {

    }

    public NumQueryRecord RDFToNumQueryRecord(RDFQueryRecord rdfRq) {


        List<IntervalRange> queryStatements = new ArrayList<IntervalRange>(3);
        List<List<IntervalRange>> queryResults = new ArrayList<List<IntervalRange>>();


        /*
            Convert Query Statements.
            IntervalRanges are copied. We assume that only objects
            are IntervalRanges. Other ranges are ignored
            substituted with default IntervalRanges.
        */


        queryStatements.add(new IntervalRange(Integer.MIN_VALUE, Integer.MAX_VALUE));  // subject
        queryStatements.add(new IntervalRange(Integer.MIN_VALUE, Integer.MAX_VALUE));  // predicate

        for (Map.Entry<URI, RangeLength<?>> entry :
                rdfRq.getRectangle().getObjectRange().getLiteralRange().getRanges().entrySet()) {        // object

            RangeLength<?> objRange = entry.getValue();

            if (objRange instanceof IntervalRange) {

                int low = ((IntervalRange) objRange).getLow();
                int high = ((IntervalRange) objRange).getHigh();
                queryStatements.add(new IntervalRange(low, high));

                break;

            } else {

                System.err.println("Converter supports only RDFRectangles," +
                        " whose objects are " +
                        " of IntervalRange type");
            }
        }

         /*
            Convert Query Results.
        */
        for (BindingSet bs : rdfRq.getQueryResult().getBindingSets()) {

            List<IntervalRange> bindingSet = new ArrayList<IntervalRange>(3);


            bindingSet.add(new IntervalRange(1, 1));     // subject
            bindingSet.add(new IntervalRange(1, 1));    // predicate

            int object = Integer.parseInt(bs.getBindings().get(2).getValue());
            bindingSet.add(new IntervalRange(object,object));// object

            queryResults.add(bindingSet);
        }

        return new NumQueryRecord(new NumQuery(queryStatements, queryResults));
    }

    public static void main(String args[]) {
        RDFToNumQueryRecordConverter c = new RDFToNumQueryRecordConverter();
    }
}
