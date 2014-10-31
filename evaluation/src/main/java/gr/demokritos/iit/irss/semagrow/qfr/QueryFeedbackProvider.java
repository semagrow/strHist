package gr.demokritos.iit.irss.semagrow.qfr;

import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;

import java.util.Iterator;


/**
 * Created by angel on 10/31/14.
 */
public interface QueryFeedbackProvider<R extends Rectangle<R>, S> {

    public Iterator<QueryRecord<R,S>> getQueryRecordIterator();

}
