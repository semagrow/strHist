package gr.demokritos.iit.irss.semagrow.rdf.parsing.vocab;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Created by angel on 10/11/14.
 */
public class POWDERS {

    static ValueFactory vf = ValueFactoryImpl.getInstance();

    public static final String NAMESPACE = "http://www.w3.org/2007/05/powder-s#";
    public static final String PREFIX = "powders";
    public static final URI ISSUEDBY = vf.createURI(NAMESPACE, "issuedby");
}
