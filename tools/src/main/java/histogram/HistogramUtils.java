package histogram;

import gr.demokritos.iit.irss.semagrow.api.QueryLogRecord;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.base.Stat;
import gr.demokritos.iit.irss.semagrow.base.range.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.file.FileManager;
import gr.demokritos.iit.irss.semagrow.file.ResultMaterializationManager;
import gr.demokritos.iit.irss.semagrow.qfr.QueryRecordAdapter;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.RDFSTHolesHistogram;
import gr.demokritos.iit.irss.semagrow.rdf.RDFURIRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFValueRange;
import gr.demokritos.iit.irss.semagrow.rdf.io.json.JSONDeserializer;
import gr.demokritos.iit.irss.semagrow.rdf.io.json.JSONSerializer;
import gr.demokritos.iit.irss.semagrow.rdf.io.sevod.VoIDSerializer;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesBucket;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import org.openrdf.model.URI;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by kzam on 5/21/15.
 */
public class HistogramUtils {
    private static String logDir;
    static final Logger logger = LoggerFactory.getLogger(HistogramUtils.class);
    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    public HistogramUtils(ExecutorService executor, String logDir) {
        this.executor = executor;
        this.logDir = logDir;
    }

    public static Collection<QueryRecord> adaptLogs(Collection<QueryLogRecord> logs) {
        Collection<QueryRecord> queryRecords = new LinkedList<QueryRecord>();
        Iterator iter = logs.iterator();

        while (iter.hasNext()) {
            try {
                QueryLogRecord record = (QueryLogRecord) iter.next();


                if(isSinglePattern(record))
                    queryRecords.add(new QueryRecordAdapter(record, getMateralizationManager()));
            } catch (Exception e) {
                //throw new QueryLogException(e);
            }
        }
        return queryRecords;
    }

    private static boolean isSinglePattern(QueryLogRecord record) {
        TupleExpr expr = record.getQuery();
        Collection<StatementPattern> patterns = StatementPatternCollector.process(expr);

        if(patterns.size() != 1) {
            logger.info("Only single-patterns supported");
            return false;
        }

        return true;
    }


    public static ResultMaterializationManager getMateralizationManager() {
        File baseDir = new File(logDir);
        TupleQueryResultFormat resultFF = TupleQueryResultFormat.TSV;

        TupleQueryResultWriterRegistry registry = TupleQueryResultWriterRegistry.getInstance();
        TupleQueryResultWriterFactory writerFactory = registry.get(resultFF);
        return new FileManager(baseDir, writerFactory, executor);
    }

    public static RDFSTHolesHistogram loadPreviousHistogram(String logFolder) {
        File jsonHist = new File(logFolder + "histJSON.txt");

        if (!jsonHist.exists())
            logger.info("Creating a new histogram.");
        else
            logger.info("Deserializing previous histogram.");

        return (!jsonHist.exists())
                ? new RDFSTHolesHistogram( (STHolesBucket) getFixedRoot())
                : new JSONDeserializer(logFolder + "histJSON.txt").getHistogram();
    }

    public static void serializeHistogram(STHolesHistogram histogram, String path) {
        logger.info("Serializing histogram to JSON in : " + path + "histJSON.txt");
        new JSONSerializer(histogram, path + "histJSON.txt");
        logger.info("Serializing histogram to VOID in : " + path + "histVOID.txt");
        new VoIDSerializer("application/x-turtle", path + "histVOID.ttl").serialize(histogram);
    }


    //?????
    private static STHolesBucket getFixedRoot() {
        RDFRectangle box = new RDFRectangle(new RDFURIRange(), new ExplicitSetRange<URI>(), new RDFValueRange());

        List<Long> list = new ArrayList<Long>();
        list.add((long)2576877);//2004: 2318520
        list.add((long)1);
        list.add((long)20692);//2004: 19709

        Stat stats = new Stat((long) 17447544, list);// 2004: 15371754

        return new STHolesBucket(box, stats);
    }
}
