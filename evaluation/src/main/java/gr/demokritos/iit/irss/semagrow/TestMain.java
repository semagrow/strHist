package gr.demokritos.iit.irss.semagrow;

import info.aduna.iteration.Iterations;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.sail.nativerdf.NativeStore;

import java.io.*;

/**
 * Created by Nick on 06-Aug-14.
 */
public class TestMain {

    public static void main(String[] args) throws RepositoryException, IOException, RDFParseException, RDFHandlerException {

        String tempNSData = args[0];
        String inputFolder = args[1];
        String outputFolder = args[2];


        File directory = new File(inputFolder); //  "C:\\Users\\Nick\\Downloads\\rdf_output\\RDF_Output";
        File[] files = directory.listFiles();

        for (File folder : files) {

            // create a local Sesame Native Store
            File dataDir = new File(tempNSData + "/" + folder.getName()); //"src\\main\\resources\\temp\\"
            if (!dataDir.exists()) {
                Repository nativeRep = new SailRepository(new NativeStore(dataDir));
                nativeRep.initialize();

                System.out.println(folder.getName());
                parseFolder(folder, nativeRep, outputFolder);
            } else {
                System.out.println("File " + dataDir.getName() + " already exists. Skipping.");
            }
        }
    }

    private static void parseFolder(File folder, Repository nativeRep, String outputFolder)
            throws RepositoryException, IOException, RDFParseException, RDFHandlerException {
        File[] files = folder.listFiles();

        //"src\\main\\resources\\data\\"
        OutputStream out = new FileOutputStream(outputFolder + "_" + folder.getName() +".ttl");

        RepositoryConnection conn = nativeRep.getConnection();
        for (File file : files) {
            System.out.println(file.getName());
            try {
                conn.add(file, file.getName(), RDFFormat.forFileName(file.getName()));
            } catch(Exception e) {
                System.out.println(e.toString());
                System.out.println("skipping " + file.getName());
            }
        }

        URI property = ValueFactoryImpl.getInstance().createURI("http://purl.org/dc/terms/subject");
        RepositoryResult<Statement> statements = conn.getStatements(null, property, null, true);
        Rio rio = new Rio();
        rio.write(Iterations.asList(statements), out, RDFFormat.NTRIPLES);

        conn.close();
        out.close();
    }
}
