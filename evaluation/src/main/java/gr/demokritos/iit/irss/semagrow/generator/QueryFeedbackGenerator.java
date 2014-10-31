package gr.demokritos.iit.irss.semagrow.generator;

import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryFeedbackProvider;

/**
 * Created by angel on 10/31/14.
 */
public interface QueryFeedbackGenerator<R extends Rectangle<R>,S>
        extends QueryFeedbackProvider<R,S>
{
    void generate();
}
