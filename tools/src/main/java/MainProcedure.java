import config.LogConfigImpl;
import exception.IntegrationException;
import gr.demokritos.iit.irss.semagrow.api.QueryLogException;
import qfr.QueryLogRemover;
import histogram.LoadHistogram;
import log.LogWriterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qfr.QueryLogManager;

import java.io.File;

/**
 * Created by kzam on 5/21/15.
 */
public class MainProcedure {

    private QueryLogManager manager;
    private static LoadHistogram histogram;

    static final Logger logger = LoggerFactory.getLogger(MainProcedure.class);

    //private final static String baseDir = "/var/tmp/log/";
    //private final static String histDir = "/var/tmp/";
    private static LogConfigImpl config;

    public MainProcedure(LogConfigImpl config) {
        this.config = config;

        manager = new QueryLogManager(this.config.getLogDir(), this.config.getQfrPrefix());

        histogram = new LoadHistogram(this.config);
    }

    public void refine() {
        LogWriterImpl writer = LogWriterImpl.getInstance();
        writer.start();

        File[] qfrFiles = manager.getQfrFiles();

        if(qfrFiles.length == 0) {
            writer.write("No files for refinement");
            logger.info("No files for refinement");

            System.out.println("No files for refinement");
        }

        for(int i=0; i<qfrFiles.length; i++) {
            // refine histogram based on the metadata of a log file

            try {

                histogram.refine(qfrFiles[i]);

            } catch(ArrayIndexOutOfBoundsException e) {

                if(i == qfrFiles.length-1)
                    new IntegrationException(e);
                else
                    continue;
            } catch (java.lang.ClassCastException e) {

                if(i == qfrFiles.length-1)
                    new IntegrationException(e);
                else
                    continue;
            } catch (QueryLogException e) {

                if(i == qfrFiles.length-1)
                    new IntegrationException(e);
                else
                    continue;
            } finally {
                if(config.isDelete()) {
                    deleteLog(qfrFiles[i]);
                }
            }
        }

        //  if(config.isDelete())
        //   deleteLogs(qfrFiles);

        writer.close();
    }

    private void deleteLog(File qfr) {
        QueryLogRemover remover = QueryLogRemover.getInstance();

        remover.deleteQfr(qfr);

    }

    // delete the log files
    private void deleteLogs(File[] qfrFiles) {
        QueryLogRemover remover = QueryLogRemover.getInstance();

        for(int i=0; i<qfrFiles.length; i++) {
            remover.deleteQfr(qfrFiles[i]);
        }
    }


    public static void main(String [] args)
    {
        LogConfigImpl config = new LogConfigImpl();
        config.setHistDir("/var/tmp/");
        config.setLogDir("/var/tmp/log/");
        config.setQfrPrefix("qfr");
        config.setDelete(false);

        MainProcedure procedure = new MainProcedure(config);

        procedure.refine();

    }
}
