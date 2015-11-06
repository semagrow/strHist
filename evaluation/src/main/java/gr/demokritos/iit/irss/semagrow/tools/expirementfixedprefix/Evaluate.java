package gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix;

import eu.semagrow.commons.utils.FileUtils;
import eu.semagrow.config.SemagrowRepositoryConfig;
import eu.semagrow.core.impl.planner.Plan;
import eu.semagrow.core.impl.planner.PlanVisitorBase;
import eu.semagrow.query.SemagrowTupleQuery;
import eu.semagrow.repository.SemagrowRepository;
import gr.demokritos.iit.irss.semagrow.rdf.RDFSTHolesHistogram;
import gr.demokritos.iit.irss.semagrow.tools.AnalysisMetrics;
import gr.demokritos.iit.irss.semagrow.tools.QueryEvaluatorStructure;
import gr.demokritos.iit.irss.semagrow.tools.Utils;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.openrdf.model.Graph;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.query.*;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.algebra.helpers.VarNameCollector;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultParserRegistry;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.*;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.sail.config.SailConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nickozoulis on 20/11/2014.
 */
public class Evaluate {

    static final Logger logger = LoggerFactory.getLogger(Evaluate.class);
    static final OpenOption[] options = {StandardOpenOption.CREATE, StandardOpenOption.APPEND};
    private static String prefixes = "prefix dc: <http://purl.org/dc/terms/> prefix semagrow: <http://www.semagrow.eu/rdf/> ";
    private static Hashtable<String, Long> hashTable;
    private AnalysisMetrics metrics = new AnalysisMetrics();

    // Setup Parameters
    private static final String DISTINCTPath = "/var/tmp/distinct/";
    private static String inputPath, outputPath;


    public static void main(String[] args) throws IOException, RepositoryException {
        OptionParser parser = new OptionParser("i:o:");
        OptionSet options = parser.parse(args);

        if (options.hasArgument("i") && options.hasArgument("o")) {
            inputPath = options.valueOf("i").toString();
            outputPath = options.valueOf("o").toString();

            //executeExperiment();
            Evaluate ev = new Evaluate();
            ev.executeQuery();
        } else {
            logger.error("Invalid arguments");
            System.exit(1);
        }
    }


    private static void executeExperiment ()throws IOException, RepositoryException {
        // evaluate(Utils.getRepository(inputPath));
    }

    private void executeQuery() throws IOException {


        Path path = Paths.get(outputPath, "results_.csv");
        BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8, options);
        bw.write("Prefix, Actual, Est, AbsErr%\n\n");

        RDFSTHolesHistogram histogram = loadHistogram(1);

        // Evaluate a point query on histogram and triple store.
        evaluateWithSampleTestQueries1(histogram, bw, 0.01);

        bw.flush();
        bw.close();

    }

    private void evaluate(Repository repo) throws IOException, RepositoryException {
        // Load Evaluations
        logger.info("Loading point query evaluations: ");
        loadPointQueryEvaluations();

        logger.info("Starting evaluation: ");
        RepositoryConnection conn;

        try {
            conn = repo.getConnection();

            Path path = Paths.get(outputPath, "results_.csv");
            BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8, options);
            bw.write("Year, Prefix, Act, Est, AbsErr%\n\n");

            RDFSTHolesHistogram histogram = loadHistogram(1);

            // Evaluate a point query on histogram and triple store.
            evaluateWithSampleTestQueries1(histogram, bw, 0.01);

            bw.flush();
            bw.close();
            conn.close();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        repo.shutDown();
    }

    private static RDFSTHolesHistogram loadHistogram(int iteration) {
        RDFSTHolesHistogram histogram;

        if (iteration == 0)
            histogram = Utils.loadPreviousHistogram(outputPath);
        else
            histogram = Utils.loadCurrentHistogram(outputPath);

        return histogram;
    }

    private static void evaluateWithSampleTestQueries(RepositoryConnection conn,
                                                      RDFSTHolesHistogram histogram,
                                                      BufferedWriter bw,
                                                      double percentage) {

        logger.info("Executing test queries of year: ");

        String testQuery;
        Set samplingRows = Utils.getSamplingRows(DISTINCTPath + "subjects_.txt", percentage);
        Iterator iter = samplingRows.iterator();

        while (iter.hasNext()) {
            try {
                Integer i = (Integer) iter.next();
                String subject = Utils.loadDistinctSubject(i, DISTINCTPath);

                testQuery = prefixes + " select * where {<%s> dc:subject ?o}";
                testQuery = String.format(testQuery, subject);

                evaluateTestQuery(conn, histogram, testQuery, bw);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            bw.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void evaluateWithSampleTestQueries1(RDFSTHolesHistogram histogram,
                                                BufferedWriter bw,
                                                double percentage) {

        logger.info("Executing test queries: ");

        String testQuery;
        QueryEvaluatorStructure actualEval, histEval;

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader("/home/katerina/logs/testQ"));

            ActualQueryExecutor actual = new ActualQueryExecutor("repository.ttl");
            actual.startConnection();

            ActualQueryExecutor hist = new ActualQueryExecutor("repository_hist.ttl");
            hist.startConnection();
            String line;

            while ((line = in.readLine()) != null) {
                testQuery = "select ?o where { " + line + " }";

               // System.out.println("Run normally.... ");
                actualEval = actual.runSemagrowTest(testQuery, metrics);
                //System.out.println("Results = " + actualEval.getResultCount() + "\n -------------------------------------------------- \n");

                //System.out.println("Run hist evaluation.... ");
                histEval = hist.runSemagrowTest(testQuery, metrics);
                //System.out.println("\n -------------------------------------------------- \n");

                evaluateTestQuery1(histogram, testQuery, actualEval.getResultCount(), bw);

                metrics.setActual_execution_time(actualEval.getTime());
                metrics.setEstimate_execution_time(histEval.getTime());
                metrics.setActual_results(actualEval.getResultCount());

                evaluateQuery(actualEval, histEval);

                System.out.println(metrics.toString());


                try {
                    bw.write("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                metrics.initialize();

            }
            actual.closeConnection();
            actual.shutdown();

            hist.closeConnection();
            hist.shutdown();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void evaluateQuery(QueryEvaluatorStructure actualEval, QueryEvaluatorStructure histEval) {


        System.out.println("ACTUAL: \n"+actualEval.getPlan().toString());
        System.out.println("HIST: \n"+histEval.getPlan().toString());
        if (actualEval.getPlan().equals(histEval.getPlan())) {
            //System.out.println("Equal plans ");
            metrics.setEqual_plan(true);

        } else {
            //System.out.println("Not Equal plans");
            metrics.setEqual_plan(false);
        }

        actualEval.getPlan().visit(new PlanVisitorBase<RuntimeException>() {

            @Override
            public void meet(Plan plan) throws RuntimeException {

                metrics.setActual_cardinality(plan.getProperties().getCardinality());
                metrics.setActual_cpu_cost(plan.getProperties().getCost().getOverallCost());

                plan.visitChildren(new PlanVisitorBase<RuntimeException>() {
                    @Override
                    public void meet(Plan plan) throws RuntimeException {
                        //System.out.println("child plan with cost " + plan.getProperties().getCost());
                        plan.setParentNode(plan);
                    }

                });
            }

        });

        histEval.getPlan().visit(new PlanVisitorBase<RuntimeException>() {

            @Override
            public void meet(Plan plan) throws RuntimeException {

                metrics.setEstimate_cardinality(plan.getProperties().getCardinality());
                metrics.setEstimate_cpu_cost(plan.getProperties().getCost().getOverallCost());

                plan.visitChildren(new PlanVisitorBase<RuntimeException>() {
                    @Override
                    public void meet(Plan plan) throws RuntimeException {
                        //System.out.println("child plan with cost " + plan.getProperties().getCost());
                        plan.setParentNode(plan);
                    }

                });
            }

        });
    }

    private void evaluateTestQuery1(RDFSTHolesHistogram histogram,
                                           String testQuery, long actualResults, BufferedWriter bw) {
        String prefix = getPrefix(testQuery);

//        long actual = hashTable.get(prefix);
        long estimate = Utils.evaluateOnHistogram1(histogram, testQuery);
        long error;

        metrics.setEstimate_results(estimate);


        //System.out.println("Run histogram.... Results = " + estimate);

        if (actualResults == 0 && estimate == 0)
            error = 0;
        else
            error = (Math.abs(actualResults - estimate) * 100) / (Math.max(actualResults, estimate));

        metrics.setError(error);
        try {
            bw.write(prefix + ", " + actualResults + ", " + estimate + ", " + error + "%");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //System.out.println("\n ************************************** \n");


    }

    private static void evaluateTestQuery(RepositoryConnection conn, RDFSTHolesHistogram histogram,
                                          String testQuery, BufferedWriter bw) {
        String prefix = getPrefix(testQuery);

        long actual = hashTable.get(prefix);
        long estimate = Utils.evaluateOnHistogram(conn, histogram, testQuery);
        long error;

        if (actual == 0 && estimate == 0)
            error = 0;
        else
            error = (Math.abs(actual - estimate) * 100) / (Math.max(actual, estimate));

        try {
            bw.write(prefix + ", " + actual + ", " + estimate + ", " + error + "%");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getPrefix(String testQuery) {
        String pattern = "<(.*?)>";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(testQuery);

        while (m.find()) {
            if (m.group(0).contains("http://agris.fao.org/aos/records/")) {
                String[] splits = m.group(1).split("/");
                return splits[splits.length - 1];
            }
        }

        return "";
    }

    private static Hashtable loadPointQueryEvaluations() {
        hashTable = new Hashtable<>();

        String line = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(DISTINCTPath + "evals_" + 2014 + ".csv"));

            while ((line = br.readLine()) != null) {
                String[] splits = line.split(",");

                try {
                    if (splits.length == 2)
                        hashTable.put(splits[0].trim(), Long.parseLong(splits[1].trim()));
                } catch (NumberFormatException e) {
                }
            }

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return hashTable;
    }



}
