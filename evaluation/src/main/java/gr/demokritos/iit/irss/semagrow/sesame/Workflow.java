package gr.demokritos.iit.irss.semagrow.sesame;

import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;

import gr.demokritos.iit.irss.semagrow.rdf.RDFSTHolesHistogram;
import gr.demokritos.iit.irss.semagrow.rdf.io.json.JSONSerializer;
import gr.demokritos.iit.irss.semagrow.rdf.io.log.LogParser;
import gr.demokritos.iit.irss.semagrow.rdf.io.log.RDFQueryRecord;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.openrdf.query.*;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailTupleQuery;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by angel on 10/11/14.
 */
public class Workflow {


    private static String prefixes = "prefix dc: <http://purl.org/dc/terms/> prefix semagrow: <http://www.semagrow.eu/rdf/> ";

    //TODO: Isws thelei count
    private static String testQ1 = prefixes + "select * {?x semagrow:year 1980 . }";
    private static String testQ2 = prefixes + "select * {?x semagrow:year 1980 . } filter regex(\".*US\"), str(?x)";

    // Use it like this : String.format(q, "2012", "US");




    public static RDFSTHolesHistogram histogram;

//    private static List<String> agroTerms = loadAgrovocTerms("/home/nickozoulis/agrovoc_terms.txt");
//    public static String logOutputPath = "/home/nickozoulis/strhist_exp_logs/";
//    private static String tripleStorePath = "/home/nickozoulis/Downloads/";
//    private static int term = 0;
//
//    private static int startDate = 1980, endDate = 1980;
//    public static Path path =  Paths.get(logOutputPath, "semagrow_logs.log");

    private static List<String> agroTerms;
    public static String logOutputPath;
    private static String tripleStorePath;
    private static int term = 0;

    private static int startDate, endDate ;
    public static Path path;
    static final OpenOption[] options = {StandardOpenOption.CREATE, StandardOpenOption.APPEND};

    /**
     * s = Starting date, e = Ending Date, l = LogOutput path
     * @param args
     * @throws RepositoryException
     * @throws IOException
     */
    static public void main(String[] args)
    throws RepositoryException, IOException
    {

        OptionParser parser = new OptionParser("s:e:l:t:a:");
        OptionSet options = parser.parse(args);


        /*******************************DEBUG**********************************
        if (true) {
            path = Paths.get("/home/efi/", "semagrow_logs2.log");
            List<RDFQueryRecord> listQueryRecords = new LogParser(path.toString()).parse();
            System.out.println("---<");
            histogram = new RDFSTHolesHistogram();
            if (listQueryRecords.size() > 0) {
                histogram.refine(listQueryRecords);
            }
            System.exit(0);

        }

        **********************************************************************/

        if( options.hasArgument("s") && options.hasArgument("e") && options.hasArgument("l") && options.hasArgument("t") && options.hasArgument("a") )
        {
            startDate = Integer.parseInt(options.valueOf("s").toString());
            endDate = Integer.parseInt(options.valueOf("e").toString());
            if (startDate > endDate) System.exit(1);

            path = Paths.get(options.valueOf("l").toString(), "semagrow_logs.log");
            tripleStorePath = options.valueOf("t").toString();
            agroTerms = loadAgrovocTerms(options.valueOf("a").toString());

            runMultiAnnualExperiment();
        }
        else if( options.hasArgument("l") && options.hasArgument("t") && options.hasArgument("a") )
        {
            path = Paths.get(options.valueOf("l").toString(), "semagrow_logs.log");
            
            String a = options.valueOf("a").toString();
            if( a.compareTo( "def" ) == 0 ) {
            	agroTerms = new ArrayList<String>( 1 );
            	agroTerms.add( "http://aims.fao.org/aos/agrovoc/c_6326" );
            }
            else {
            	agroTerms = loadAgrovocTerms( a );
            }
            
            tripleStorePath = options.valueOf("t").toString();
            if( tripleStorePath.compareTo("agris") == 0 ) {
                runRemoteExperiment( "http://202.45.139.84:10035/catalogs/fao/repositories/agris" );
            }
            else {
                runRemoteExperiment( tripleStorePath );
            }
        }
        else { System.exit(1); }
    }

    private static void runRemoteExperiment( String url )
    throws RepositoryException, IOException
    {
    	Repository agris = new org.openrdf.repository.sparql.SPARQLRepository( url );
        agris.initialize();
        Repository repo = getFedRepository( agris );
        RepositoryConnection conn = null;
        term = 0;
        
        // For now loop for some agroTerms
        for( String strTerm : agroTerms ) {
            System.out.println( term + " --- " +  strTerm );
            try {
                conn = repo.getConnection();
                String qq = prefixes + "select * {?u dc:subject <%s> }";
                String quer = String.format( qq, strTerm );
                ++term;
                TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, quer);
                TupleQueryResult result = query.evaluate();
                consumeIteration(result);
                conn.close();

            } catch (MalformedQueryException | QueryEvaluationException mqe) {
                mqe.printStackTrace();
            }

            // The evaluation of the query will write logs (query feedback).
            List<RDFQueryRecord> listQueryRecords = new LogParser(path.toString()).parse();
            System.out.println("---<");
            if (listQueryRecords.size() > 0) {
                histogram.refine(listQueryRecords);
            }
        }

        Path path = Paths.get(Workflow.path.toString(), "results.csv");
        BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8, options);
        execTestQueries(conn, bw, term);
        bw.close();
    }
   
    private static void runMultiAnnualExperiment()
    throws RepositoryException, IOException
    {
    	
        for (int i=startDate; i<=endDate; i++) {

            Repository repo = getFedRepository(getRepository(i));
            RepositoryConnection conn = null;
            term = 0;

            // For now loop for some agroTerms
            for (int j=0; j<150; j++) {
                System.out.println(term + " -- " + agroTerms.get(term));
                try {
                    conn = repo.getConnection();

                    String qq = prefixes + "select * {?u dc:subject <%s> }";
                    String quer = String.format(qq, agroTerms.get(term++));
                    TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, quer);

                    TupleQueryResult result = query.evaluate();
                    consumeIteration(result);
                    conn.close();

                } catch (MalformedQueryException | QueryEvaluationException mqe) {
                    mqe.printStackTrace();
                }

            }

            // The evaluation of the query will write logs (query feedback).
            List<RDFQueryRecord> listQueryRecords = new LogParser(path.toString()).parse();
            System.out.println("---<");
            if (listQueryRecords.size() > 0) {
                histogram.refine(listQueryRecords);
            }

            System.out.println(histogram.getRoot().toString());
            new JSONSerializer(histogram, Workflow.path.getParent().toString() + "histJSON.txt");

            Path path = Paths.get(Workflow.path.getParent().toString(), "results.csv");
            BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8, options);

            conn = repo.getConnection();
            execTestQueries(conn, bw, term);
            conn.close();

            bw.close();
        }
    }

    private static void execTestQueries(RepositoryConnection conn, BufferedWriter bw, int term) {
        try {
            bw.write(term + ", " + "Q1, " + execTripleStore(conn, testQ1) + ", " + execHistogram(conn, testQ1));
            bw.newLine();
            bw.write(term + ", " + "Q2, " + execTripleStore(conn, testQ2) + ", " + execHistogram(conn, testQ2));
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static long execHistogram(RepositoryConnection conn, String query) {
        SailTupleQuery sailQuery;
        try {
            sailQuery = (SailTupleQuery)conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            return new CardinalityEstimatorImpl(histogram).
                    getCardinality(sailQuery.getParsedQuery().getTupleExpr(), sailQuery.getBindings());

        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private static long execTripleStore(RepositoryConnection conn, String query) {

        try {

            ParsedTupleQuery q = QueryParserUtil.parseTupleQuery(QueryLanguage.SPARQL, query, "http://example.org/");
            return new ActualCardinalityEstimator(conn).
                    getCardinality(q.getTupleExpr(), EmptyBindingSet.getInstance());

        } catch (MalformedQueryException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private static Repository getFedRepository(Repository actual) throws RepositoryException {
        TestSail sail = new TestSail(actual);
        histogram = sail.getHistogram();
        Repository repo = new SailRepository(sail);
        repo.initialize();
        return repo;
    }

    private static void consumeIteration(Iteration<BindingSet,QueryEvaluationException> iter) throws QueryEvaluationException {
    	int n = 0;
        while (iter.hasNext()) {
            iter.next();
            ++n;
            System.out.println("Has next");
        }
        System.out.println("Iterated over " + Integer.toString(n) + " items" );
        Iterations.closeCloseable(iter);
    }


    private static Repository getRepository( int year )
    throws RepositoryException, IOException {
        Properties properties = new Properties();

        File journal = new File(tripleStorePath + "bigdata_agris_data_" + year + ".jnl");

        properties.setProperty(
                BigdataSail.Options.FILE,
                journal.getAbsolutePath()
        );

        // Instantiate a sail and a Sesame repository
        BigdataSail sail = new BigdataSail(properties);
        Repository repo = new BigdataSailRepository(sail);
        repo.initialize();

        return repo;
    }

       private static List<String> loadAgrovocTerms(String path) {
             List<String> list = new ArrayList<String>();

                        try {
                        BufferedReader br = new BufferedReader(new FileReader(path));
                        String text = "";

                                while ((text = br.readLine()) != null) {
                                String[] split = text.split(",");
                                list.add(split[1].trim());
//                                    list.add(text);
                            }

                                br.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

            return list;
        }



}
