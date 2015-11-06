package gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix;

import eu.semagrow.commons.utils.FileUtils;
import eu.semagrow.config.SemagrowRepositoryConfig;
import eu.semagrow.query.SemagrowTupleQuery;
import eu.semagrow.repository.SemagrowRepository;
import gr.demokritos.iit.irss.semagrow.tools.AnalysisMetrics;
import gr.demokritos.iit.irss.semagrow.tools.QueryEvaluatorStructure;
import gr.demokritos.iit.irss.semagrow.tools.QueryEvaluatorStructureImpl;
import org.openrdf.model.Graph;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.query.*;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultParserRegistry;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.*;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.sail.config.SailConfigException;

import java.io.*;
import java.util.List;

/**
 * Created by katerina on 24/9/2015.
 */

/**
 * make the queries through Semagrow with no use of strhist
 */
public class ActualQueryExecutor {


    private SemagrowRepository repository;
    private RepositoryConnection conn = null;
    private String repo;

    public ActualQueryExecutor(String repo) {
        try {
            this.repo = repo;
            initRepo();
        } catch (RepositoryConfigException e) {
            e.printStackTrace();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }


    private void initRepo() throws RepositoryConfigException, RepositoryException {
        SemagrowRepositoryConfig config = getConfig();
        RepositoryFactory repoFactory = RepositoryRegistry.getInstance().get(config.getType());

        //SemagrowRepositoryConfig repoConfig = getConfig( QueryTest.repoConfigFile );

        repository = (SemagrowRepository) repoFactory.getRepository(config);
        repository.initialize();
    }

    private SemagrowRepositoryConfig getConfig() {

        try {
            File file = FileUtils.getFile(this.repo);
            Graph configGraph = parseConfig(file);
            RepositoryConfig repConf = RepositoryConfig.create(configGraph, null);
            repConf.validate();
            RepositoryImplConfig implConf = repConf.getRepositoryImplConfig();
            return (SemagrowRepositoryConfig)implConf;
        } catch (RepositoryConfigException e) {
            e.printStackTrace();
            return new SemagrowRepositoryConfig();
        } catch (SailConfigException | IOException | NullPointerException e) {
            e.printStackTrace();
            return new SemagrowRepositoryConfig();
        }
    }

    protected Graph parseConfig(File file) throws SailConfigException, IOException {

        RDFFormat format = Rio.getParserFormatForFileName(file.getAbsolutePath());
        if (format==null)
            throw new SailConfigException("Unsupported file format: " + file.getAbsolutePath());
        RDFParser parser = Rio.createParser(format);
        Graph model = new GraphImpl();
        parser.setRDFHandler(new StatementCollector(model));
        InputStream stream = new FileInputStream(file);

        try {
            parser.parse(stream, file.getAbsolutePath());
        } catch (Exception e) {
            throw new SailConfigException("Error parsing file!");
        }

        stream.close();
        return model;
    }

    public void shutdown() throws Exception {
        if (repository != null && repository.isInitialized())
            repository.shutDown();
    }

    public void startConnection() {
        if(conn == null) {
            //TupleQueryResultParserRegistry registry = TupleQueryResultParserRegistry.getInstance();
            //registry.remove(registry.get(TupleQueryResultFormat.JSON));

            try {

                conn = repository.getConnection();
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeConnection() {
        try {
            conn.close();
        } catch (RepositoryException e) {
            e.printStackTrace();
        } finally {
            conn = null;
        }
    }


    public QueryEvaluatorStructure runSemagrowTest(String testQuery, AnalysisMetrics metrics) throws Exception {

        SemagrowTupleQuery q = (SemagrowTupleQuery) conn.prepareTupleQuery(QueryLanguage.SPARQL, testQuery);
        QueryEvaluatorStructure eval = new QueryEvaluatorStructureImpl();
        eval.setPlan(q.getDecomposedQuery());

            //q.addIncludedSource(ValueFactoryImpl.getInstance().createURI("http://143.233.226.36:8891/sparql"));
            //q.addIncludedSource(ValueFactoryImpl.getInstance().createURI("http://4store.ipb.ac.rs:8003/sparql/"));

        //System.out.println(q.getDecomposedQuery());



            //TupleQueryResult result = q.evaluate();

        SemagrowQueryResultHandler semagrowHandler = new SemagrowQueryResultHandler();
        q.evaluate(semagrowHandler);
        eval.setResultCount(semagrowHandler.getCount());
        eval.setTime(semagrowHandler.getTime());


        return eval;
        //semagrowHandler.getCount();
            //Iterations.toString(result, "\n");
            //Iterations.closeCloseable(result);

    }
}
