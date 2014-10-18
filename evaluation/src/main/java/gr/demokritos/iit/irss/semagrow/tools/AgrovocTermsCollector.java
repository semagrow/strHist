package gr.demokritos.iit.irss.semagrow.tools;

import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;
import org.openrdf.model.Value;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by efi on 18/10/2014.
 */
public class AgrovocTermsCollector {

    private static String endpointURL = "http://202.45.139.84:10035/catalogs/fao/repositories/agris";
    private static String outputPath = "evaluation/src/test/resources/agrovoc_terms_full.txt";

    private static String q =  "prefix dcterms: <http://purl.org/dc/terms/> " +
            "select distinct ?o { " +
            "?s dcterms:subject ?o .}";

    public AgrovocTermsCollector() {

    }

    private void getAndSaveAgrovocTerms() {


        try {
            RepositoryConnection conn = getRepository(endpointURL).getConnection();

            try {
                TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, q);

                TupleQueryResult  result = query.evaluate();


                try {

                        BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath));
                        List<String> bindingNames = result.getBindingNames();
                        while (result.hasNext()) {

                            BindingSet bindingSet = result.next();
                            Value firstValue = bindingSet.getValue(bindingNames.get(0));
                            System.out.println("firstValue " + firstValue.toString());
                            bw.write(firstValue.toString());
                            bw.newLine();
                        }

                         bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    result.close();
                }

            }
            finally {
                conn.close();
            }


        } catch (QueryEvaluationException mqe) {
            mqe.printStackTrace();
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    private static void consumeIteration(Iteration<BindingSet,QueryEvaluationException> iter) throws QueryEvaluationException {
        while (iter.hasNext())
            iter.next();

        Iterations.closeCloseable(iter);
    }

    private static Repository getRepository(String endpointURL) throws RepositoryException {

        Repository repo = new SPARQLRepository(endpointURL);
        repo.initialize();
        return repo;
    }

    public static void main (String[] args) throws RepositoryException {

        AgrovocTermsCollector c = new AgrovocTermsCollector();
        c.getAndSaveAgrovocTerms();
        System.out.println("Finished");
    }


}
