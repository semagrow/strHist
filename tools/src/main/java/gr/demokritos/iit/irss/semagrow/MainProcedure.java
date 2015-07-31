package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.config.LogConfigImpl;
import gr.demokritos.iit.irss.semagrow.exception.IntegrationException;
import eu.semagrow.querylog.api.QueryLogException;
import gr.demokritos.iit.irss.semagrow.qfr.QueryLogRemover;
import gr.demokritos.iit.irss.semagrow.histogram.LoadHistogram;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gr.demokritos.iit.irss.semagrow.qfr.QueryLogManager;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;

/**
 * Created by kzam on 5/21/15.
 */
public class MainProcedure {

    private QueryLogManager manager;
    private static LoadHistogram histogram;

    private LogConfigImpl config;

    private static Logger logger = LoggerFactory.getLogger(MainProcedure.class);

    public MainProcedure(LogConfigImpl config) {
        this.config = config;

        manager = new QueryLogManager(this.config.getLogDir(), this.config.getQfrPrefix());

        histogram = new LoadHistogram(this.config);
    }

    public void refine() throws IntegrationException {

        File[] qfrFiles = manager.getQfrFiles();

        if(qfrFiles.length == 0) {

            logger.info("No files for refinement");
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
                    throw new IntegrationException(e);
                }
            } catch (java.lang.ClassCastException e) {

                if(i == qfrFiles.length-1) {
                    if(config.isDelete()) {
                        deleteLog(qfrFiles[i]);
                    }
                    throw new IntegrationException(e);
                }
            } catch (NotImplementedException e) {
                if(i == qfrFiles.length-1) {
                    if(config.isDelete()) {
                        deleteLog(qfrFiles[i]);
                    }
                    throw new IntegrationException(e);
                }

            }
            catch (QueryLogException e) {

                if(i == qfrFiles.length-1) {
                    if(config.isDelete()) {
                        deleteLog(qfrFiles[i]);
                    }
                    throw new IntegrationException(e);
                }

            } finally {
                if(config.isDelete()) {
                    deleteLog(qfrFiles[i]);
                }
            }
        }

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

            try {
                procedure.refine();
            } catch (IntegrationException e) {
                logger.error("Integration exception", getErrorMsg(e), e);
                System.exit(getErrorCode(e));
            }

        } else {
            logger.error("Invalid arguments");
            System.exit(1);
        }
    }

    public static int getErrorCode(Exception e) {

        if(e instanceof java.lang.ClassCastException) {
            // problem with bnodes in strhist
            System.err.println("Error with BNodes: cannot be cast to URI");
            return 2;
        } else if(e instanceof ArrayIndexOutOfBoundsException) {
            // problem with objects in strhist
            System.err.println("Error with Object-Range");
            return 3;
        } else if(e instanceof NotImplementedException) {
            // problem with nom implemented methods
            System.err.println("Error with non-implemented parts");
            return 3;
        } else if(e instanceof QueryLogException) {
            // problem in parsing a log file
            System.err.println("Error in parsing a log file");
            return 4;
        } else if (e instanceof IOException) {
            // problem in handling lastQfr file
            System.err.println("Error in handling lastQfr file");
            return 5;
        } else {
            return 1;
        }
    }

    public static String getErrorMsg(Exception e) {
        if(e instanceof java.lang.ClassCastException) {
            // problem with bnodes in strhist
            return "Error with BNodes: cannot be cast to URI";
        } else if(e instanceof ArrayIndexOutOfBoundsException) {
            // problem with objects in strhist
            return "Error with Object-Range";
        } else if(e instanceof NotImplementedException) {
            // problem with nom implemented methods
            return "Error with non-implemented parts";
        } else if(e instanceof QueryLogException) {
            // problem in parsing a log file
            return "Error in parsing a log file";
        } else if (e instanceof IOException) {
            // problem in handling lastQfr file
            return "Error in handling lastQfr file";
        } else {
            return "General Integration Exception";
        }
    }
}

