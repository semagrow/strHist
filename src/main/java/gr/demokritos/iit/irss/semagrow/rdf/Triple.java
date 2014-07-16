package gr.demokritos.iit.irss.semagrow.rdf;

import gr.demokritos.iit.irss.semagrow.api.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.api.Point;
import gr.demokritos.iit.irss.semagrow.api.PrefixRange;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;

/**
 * Created by angel on 7/14/14.
 */
public class Triple implements Point {

    private URI subject;

    private URI predicate;

    private Literal object;

    public Triple(URI subject, URI  ) {
        this.subjectRange = subjectRange;
        this.predicateRange = predicateRange;
        this.objectRange = objectRange;
    }
}
