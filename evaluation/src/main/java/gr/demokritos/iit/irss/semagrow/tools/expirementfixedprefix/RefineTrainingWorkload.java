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
    private static int year;

    public static void main(String[] args) {
        OptionParser parser = new OptionParser("y:o:");
        OptionSet options = parser.parse(args);

        if (options.hasArgument("y") && options.hasArgument("o")) {
            year = Integer.parseInt(options.valueOf("y").toString());
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
        Collection<QueryLogRecord> logs = Utils.parseFeedbackLog("/var/tmp/" + year + "/" + year + "_log.ser");
        Collection<QueryRecord> queryRecords = Utils.adaptLogs(logs, year, executors);

        logger.info("Starting refining histogram: " + year);

        // Refine histogram according to the feedback
        refineHistogram(queryRecords.iterator(), year, 0);
    }

    private static RDFSTHolesHistogram refineHistogram(Iterator<QueryRecord> listQueryRecords, int year, int iteration) {
        RDFSTHolesHistogram histogram;

        if (iteration == 0)
            histogram = Utils.loadPreviousHistogram(outputPath, year);
        else
            histogram = Utils.loadCurrentHistogram(outputPath, year);

        logger.info("Refining histogram " + year);
        ((STHolesHistogram)histogram).refine(listQueryRecords);
        logger.info("Refinement is over.");

        Utils.serializeHistogram(histogram, outputPath, year);

        return histogram;
    }

}
