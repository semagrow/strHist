package gr.demokritos.iit.irss.semagrow.api;

import gr.demokritos.iit.irss.semagrow.rdf.Stat;

/**
 * Created by angel on 7/11/14.
 */
public interface QueryResult<R extends Rectangle<R>> {

    //long getCardinality();

    Stat getCardinality(R rect);

}
