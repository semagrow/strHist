package gr.demokritos.iit.irss.semagrow.qfr;

import org.openrdf.model.impl.ValueFactoryImpl;
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

        QueryLogHandler handler = new RDFQueryLogHandler(writer, ValueFactoryImpl.getInstance());
        try {
            handler.startQueryLog();
        } catch (QueryLogException e) {
            e.printStackTrace();
        }
        return handler;
    }
}
