package gr.demokritos.iit.irss.semagrow.sesame;

import gr.demokritos.iit.irss.semagrow.file.FileManager;
import gr.demokritos.iit.irss.semagrow.file.ResultMaterializationManager;
import gr.demokritos.iit.irss.semagrow.api.QueryLogException;
import gr.demokritos.iit.irss.semagrow.api.QueryLogHandler;
import gr.demokritos.iit.irss.semagrow.impl.serial.SerialQueryLogFactory;
import gr.demokritos.iit.irss.semagrow.rdf.RDFSTHolesHistogram;
import gr.demokritos.iit.irss.semagrow.rdf.io.json.JSONDeserializer;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by angel on 10/11/14.
 */
public class TestSail extends SailBase {

    static final Logger logger = LoggerFactory.getLogger(TestSail.class);
    private Repository actualRepo;

    private RDFSTHolesHistogram histogram;

    private ExecutorService executorService;

    private ResultMaterializationManager manager;

    private int year;

    public TestSail(Repository actual, int year) {
        this.year = year;
        this.histogram = instantiateHistogram();
        actualRepo = actual;
        executorService = Executors.newFixedThreadPool(10);
    }

    private RDFSTHolesHistogram instantiateHistogram() {
        File jsonHist = new File(Workflow.HISTPATH + "histJSON_" + (year - 1) + ".txt");

        logger.debug("HISTPATH: " + jsonHist.getPath());
        if (!jsonHist.exists())
            logger.debug("Creating a new histogram.");
        else
            logger.debug("Deserializing histogram: " + (year - 1));

        return (!jsonHist.exists())
                ? new RDFSTHolesHistogram()
                : new JSONDeserializer(jsonHist.getPath()).getHistogram();
    }

    public RepositoryConnection getRepositoryConnection() throws RepositoryException {
        return actualRepo.getConnection();
    }

    public RDFSTHolesHistogram getHistogram() {
        return this.histogram;
    }

    QueryLogHandler handler;

    public QueryLogHandler getQueryLogHandler() {

        RDFFormat rdfFF = RDFFormat.NTRIPLES;

        if (handler == null) {
//        RDFWriterRegistry writerRegistry = RDFWriterRegistry.getInstance();
//        RDFWriterFactory rdfWriterFactory = writerRegistry.get(rdfFF);
//        QueryLogFactory factory = new RDFQueryLogFactory(rdfWriterFactory);
            SerialQueryLogFactory factory = new SerialQueryLogFactory();
            try {
//                File qfrLog = File.createTempFile("qfr", ".log", new File("/var/tmp/"));
                File dir = new File("/var/tmp/" + year + "/");
                if (!dir.exists())
                    dir.mkdir();

                File qfrLog = new File("/var/tmp/" + year + "/" + year + "_log.ser");
                OutputStream out = new FileOutputStream(qfrLog, true);
                handler = factory.getQueryRecordLogger(out);
            } catch (IOException e) {e.printStackTrace();}
        }

        return handler;
    }


    public ResultMaterializationManager getMateralizationManager(){

        if (manager == null) {
            File baseDir = new File("/var/tmp/" + this.year + "/");
            if (!baseDir.exists())
                baseDir.mkdir();

            TupleQueryResultFormat resultFF = TupleQueryResultFormat.TSV;

            TupleQueryResultWriterRegistry registry = TupleQueryResultWriterRegistry.getInstance();
            TupleQueryResultWriterFactory writerFactory = registry.get(resultFF);
            manager = new FileManager(baseDir, writerFactory, getExecutorService());
        }
        return manager;
    }

    @Override
    protected void shutDownInternal() throws SailException {

        executorService.shutdown();

        if (handler != null)
            try {
                handler.endQueryLog();
            } catch (QueryLogException e) {
                throw new SailException(e);
            }

    }

    @Override
    protected SailConnection getConnectionInternal() throws SailException {
        try {
            return new TestSailConnection(this);
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    @Override
    public boolean isWritable() throws SailException {
        return false;
    }

    @Override
    public ValueFactory getValueFactory() { return ValueFactoryImpl.getInstance(); }

    public ExecutorService getExecutorService() { return executorService; }
}
