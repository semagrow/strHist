package gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix;

import gr.demokritos.iit.irss.semagrow.api.QueryLogRecord;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.rdf.RDFSTHolesHistogram;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import gr.demokritos.iit.irss.semagrow.tools.Utils;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nickozoulis on 20/11/2014.
 */
public class RefineTrainingWorkload {

    static final Logger logger = LoggerFactory.getLogger(RefineTrainingWorkload.class);
    private static ExecutorService executors;

    // Setup Parameters
    private static String outputPath;

    public static void main(String[] args) {
        OptionParser parser = new OptionParser("o:");
        OptionSet options = parser.parse(args);

        if (options.hasArgument("o")) {
            outputPath = options.valueOf("o").toString();

            execute();
        } else {
            logger.error("Invalid arguments");
            System.exit(1);
        }
    }

    private static void execute() {
        executors = Executors.newCachedThreadPool();
        refine();
        executors.shutdown();
    }

    private static void refine() {
        // Load Feedback
        Collection<QueryLogRecord> logs = Utils.parseFeedbackLog("/var/tmp/_log.ser");
        Collection<QueryRecord> queryRecords = Utils.adaptLogs(logs, executors);

        logger.info("Starting refining histogram: ");

        // Refine histogram according to the feedback
        refineHistogram(queryRecords.iterator(), 0);
    }

    private static RDFSTHolesHistogram refineHistogram(Iterator<QueryRecord> listQueryRecords, int iteration) {
        RDFSTHolesHistogram histogram;

        if (iteration == 0)
            histogram = Utils.loadPreviousHistogram(outputPath);
        else
            histogram = Utils.loadCurrentHistogram(outputPath);

        logger.info("Refining histogram ");
        ((STHolesHistogram)histogram).refine(listQueryRecords);
        logger.info("Refinement is over.");

        Utils.serializeHistogram(histogram, outputPath);

        return histogram;
    }

}
