package gr.demokritos.iit.irss.semagrow.sesame;

import gr.demokritos.iit.irss.semagrow.api.Histogram;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.RDFSTHolesHistogram;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailBase;

/**
 * Created by angel on 10/11/14.
 */
public class TestSail extends SailBase {


    private Repository actualRepo;

    private RDFSTHolesHistogram histogram;

    public TestSail(Repository actual) {
        actualRepo = actual;
    }

    public RepositoryConnection getRepositoryConnection() throws RepositoryException {
        return actualRepo.getConnection();
    }

    public RDFSTHolesHistogram getHistogram() {
        if (histogram == null)
            histogram = new RDFSTHolesHistogram();

        return histogram;
    }

    @Override
    protected void shutDownInternal() throws SailException { }

    @Override
    protected SailConnection getConnectionInternal() throws SailException {
        return new TestSailConnection(this);
    }

    @Override
    public boolean isWritable() throws SailException {
        return false;
    }

    @Override
    public ValueFactory getValueFactory() { return ValueFactoryImpl.getInstance(); }
}
