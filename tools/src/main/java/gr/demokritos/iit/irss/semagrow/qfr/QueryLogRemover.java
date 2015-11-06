package gr.demokritos.iit.irss.semagrow.qfr;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Created by kzam on 5/21/15.
 */
public class QueryLogRemover {
    static final Logger logger = LoggerFactory.getLogger(QueryLogRemover.class);

    private static final QueryLogRemover instance = new QueryLogRemover();

    public static QueryLogRemover getInstance() {
        return instance;
    }

    public void deleteQfr(File file) {

        if(! file.delete()) {
            logger.error("Problem in deleting file {}", file);
        }
    //    logger.info("File {} deleted successfully", file);
    }

    public void deleteResults(URI filename) throws URISyntaxException {

        File f = new File(convertbackURI(filename));
        if(! f.delete()) {

            logger.error("Problem in deleting file {}", f);
        }
    //    logger.info("File {} deleted successfully", f);
    }

    private static java.net.URI convertbackURI(URI uri) throws URISyntaxException {
        return new java.net.URI(uri.stringValue());
    }
}
