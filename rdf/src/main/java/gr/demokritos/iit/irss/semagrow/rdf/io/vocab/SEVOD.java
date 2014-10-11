package gr.demokritos.iit.irss.semagrow.rdf.io.vocab;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Created by angel on 10/11/14.
 */
public class SEVOD {

    static ValueFactory vf = ValueFactoryImpl.getInstance();

    public static final String NAMESPACE = "http://rdf.iit.demokritos.gr/2013/sevod#";
    public static final String PREFIX = "svd";
    public static final URI INTINTERVAL = vf.createURI(SEVOD.NAMESPACE, "intInterval");
    public static final URI DATEINTERVAL = vf.createURI(SEVOD.NAMESPACE, "dateInterval");
    public static final URI FROM = vf.createURI(SEVOD.NAMESPACE, "from");
    public static final URI TO = vf.createURI(SEVOD.NAMESPACE, "to");
    public static final URI SUBJECTREGEXPATTERN = vf.createURI(SEVOD.NAMESPACE, "subjectRegexPattern");
    public static final URI OBJECTREGEXPATTERN = vf.createURI(SEVOD.NAMESPACE, "objectRegexPattern");
    public static final URI STRINGOBJECTREGEXPATTERN = vf.createURI(SEVOD.NAMESPACE, "stringObjectRegexPattern");


    public static final URI FACET_PROPERTY = vf.createURI(SEVOD.NAMESPACE, "propertyFacet");
    public static final URI FACET = vf.createURI(SEVOD.NAMESPACE, "facet");
    public static final URI ROOT = vf.createURI(SEVOD.NAMESPACE, "datasetTop");
}
