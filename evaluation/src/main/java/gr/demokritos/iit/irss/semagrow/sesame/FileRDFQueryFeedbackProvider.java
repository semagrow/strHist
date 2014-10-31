package gr.demokritos.iit.irss.semagrow.sesame;


import gr.demokritos.iit.irss.semagrow.api.QueryLogParser;
import gr.demokritos.iit.irss.semagrow.file.ResultMaterializationManager;
import gr.demokritos.iit.irss.semagrow.qfr.RDFQueryFeedbackProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

/**
 * Created by angel on 10/31/14.
 */
public class FileRDFQueryFeedbackProvider extends RDFQueryFeedbackProvider {

    private final File file;

    public FileRDFQueryFeedbackProvider(File file, QueryLogParser queryLogParser, ResultMaterializationManager materializationManager, ExecutorService executorService) {
        super(queryLogParser, materializationManager, executorService);

        this.file = file;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }
}
