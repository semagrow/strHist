package gr.demokritos.iit.irss.semagrow.impl.rdf;

import gr.demokritos.iit.irss.semagrow.api.QueryLogException;
import gr.demokritos.iit.irss.semagrow.api.QueryLogFactory;
import gr.demokritos.iit.irss.semagrow.api.QueryLogHandler;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

import java.io.OutputStream;

/**
 * Created by angel on 10/21/14.
 */
public class RDFQueryLogFactory implements QueryLogFactory {

    private RDFWriterFactory writerFactory;

    public RDFQueryLogFactory(RDFWriterFactory writerFactory) {
        this.writerFactory = writerFactory;
    }

    @Override
    public QueryLogHandler getQueryRecordLogger(OutputStream out) {

        RDFWriter writer = writerFactory.getWriter(out);

        QueryLogHandler handler = new RDFQueryLogHandler(writer);
        try {
            handler.startQueryLog();
        } catch (QueryLogException e) {
            e.printStackTrace();
        }
        return handler;
    }
}
