package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.config.LogConfigImpl;
import gr.demokritos.iit.irss.semagrow.exception.IntegrationException;
import gr.demokritos.iit.irss.semagrow.api.QueryLogException;
import gr.demokritos.iit.irss.semagrow.qfr.QueryLogRemover;
import gr.demokritos.iit.irss.semagrow.histogram.LoadHistogram;
import gr.demokritos.iit.irss.semagrow.log.LogWriterImpl;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gr.demokritos.iit.irss.semagrow.qfr.QueryLogManager;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;

/**
 * Created by kzam on 5/21/15.
 */
public class MainProcedure {

    private QueryLogManager manager;
    private static LoadHistogram histogram;

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

            System.out.println("No files for refinement");
        }

        for(int i=0; i<qfrFiles.length; i++) {
            // refine histogram based on the metadata of a log file

            try {

                histogram.refine(qfrFiles[i]);

            } catch(ArrayIndexOutOfBoundsException e) {

                if(i == qfrFiles.length-1) {
                    if(config.isDelete()) {
                        deleteLog(qfrFiles[i]);
                    }
                    new IntegrationException(e);
                }
                else
                    continue;
            } catch (java.lang.ClassCastException e) {

                if(i == qfrFiles.length-1) {
                    if(config.isDelete()) {
                        deleteLog(qfrFiles[i]);
                    }
                new IntegrationException(e);
            }
            else
                    continue;
            } catch (NotImplementedException e) {
                if(i == qfrFiles.length-1) {
                    if(config.isDelete()) {
                        deleteLog(qfrFiles[i]);
                    }
                    new IntegrationException(e);
                }
                else
                    continue;
            }
            catch (QueryLogException e) {

                if(i == qfrFiles.length-1) {
                    if(config.isDelete()) {
                        deleteLog(qfrFiles[i]);
                    }
                    new IntegrationException(e);
                }
                else
                    continue;
            } finally {
                if(config.isDelete()) {
                    deleteLog(qfrFiles[i]);
                }
            }
        }


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
        OptionParser parser = new OptionParser("h:l:p:d::");
        OptionSet options = parser.parse(args);

        if (options.hasArgument("h") && options.hasArgument("l") && options.hasArgument("p")) {

            LogConfigImpl config = new LogConfigImpl();
            config.setHistDir(options.valueOf("h").toString());
            config.setLogDir(options.valueOf("l").toString());
            config.setQfrPrefix(options.valueOf("p").toString());

            if(options.has("d"))
                config.setDelete(true);
            else
                config.setDelete(false);


            MainProcedure procedure = new MainProcedure(config);

            procedure.refine();

        } else {
            System.err.println("Invalid arguments");
            System.exit(1);
        }
    }
}
