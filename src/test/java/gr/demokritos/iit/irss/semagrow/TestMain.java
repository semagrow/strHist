package gr.demokritos.iit.irss.semagrow;

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

        // create a local Sesame Native Store
        File dataDir = new File(tempNSData); //"src\\main\\resources\\temp\\"
        Repository nativeRep = new SailRepository(new NativeStore(dataDir));
        nativeRep.initialize();


        File directory = new File(inputFolder); //  "C:\\Users\\Nick\\Downloads\\rdf_output\\RDF_Output";
        File[] files = directory.listFiles();

        for (File folder : files) {
            System.out.println(folder.getName());
            parseFolder(folder, nativeRep, outputFolder);
        }
    }

    private static void parseFolder(File folder, Repository nativeRep, String outputFolder)
            throws RepositoryException, IOException, RDFParseException, RDFHandlerException {
        File[] files = folder.listFiles();

        //"src\\main\\resources\\data\\"
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outputFolder + "_" + folder.getName() +".ttl"));

        for (File file : files) {
            System.out.println(file.getName());

            RepositoryConnection conn = nativeRep.getConnection();
            conn.add(file, file.getName(), RDFFormat.forFileName(file.getName()));

            URI property = ValueFactoryImpl.getInstance().createURI("http://purl.org/dc/terms/subject");
            RepositoryResult<Statement> statements = conn.getStatements(null, property, null, true);

            Rio rio = new Rio();
            rio.write((Iterable<Statement>) statements, out, RDFFormat.NTRIPLES);

            conn.close();
        }

        out.close();
    }
}
