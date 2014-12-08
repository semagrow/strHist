package gr.demokritos.iit.irss.semagrow.tools.deprecated;

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
import org.openrdf.rio.Rio;
import org.openrdf.sail.nativerdf.NativeStore;

import java.io.File;

/**
 * Created by Nick on 08-Aug-14.
 */
public class CreateNativeStore {

    public static void main(String[] args) throws RepositoryException {

        String dataFolder = args[0];
        String nativeStoreFolder = args[1];

//        String dataFolder = "C:\\Users\\Nick\\Downloads\\filtered\\";
//        String nativeStoreFolder = "src\\main\\resources\\native_store\\";

        // Create a local Sesame Native Store.
        Repository nativeRep = new SailRepository(new NativeStore(new File(nativeStoreFolder)));
        nativeRep.initialize();

        File[] files = new File(dataFolder).listFiles();

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
    }
}
