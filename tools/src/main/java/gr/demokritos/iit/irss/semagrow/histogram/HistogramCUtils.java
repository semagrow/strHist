package gr.demokritos.iit.irss.semagrow.histogram;

import eu.semagrow.querylog.api.QueryLogRecord;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.base.Stat;
import gr.demokritos.iit.irss.semagrow.base.range.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.exception.HistogramException;
import gr.demokritos.iit.irss.semagrow.file.FileManager;
import gr.demokritos.iit.irss.semagrow.file.ResultMaterializationManager;
import gr.demokritos.iit.irss.semagrow.qfr.QueryRecordCAdapter;
import gr.demokritos.iit.irss.semagrow.rdf.*;
import gr.demokritos.iit.irss.semagrow.rdf.io.json.JSONCDeserializer;
import gr.demokritos.iit.irss.semagrow.rdf.io.json.JSONCSerializer;
import gr.demokritos.iit.irss.semagrow.rdf.io.sevod.VoIDCSerializer;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesBucket;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesCircleHistogram;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import org.json.simple.parser.ParseException;
import org.openrdf.model.URI;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by katerina on 24/11/2015.
 */
public class HistogramCUtils {

    private static String logDir;
    static final Logger logger = LoggerFactory.getLogger(HistogramCUtils.class);
    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    public HistogramCUtils(ExecutorService executor, String logDir) {
        this.executor = executor;
        this.logDir = logDir;
    }

    public static Collection<QueryRecord> adaptLogs(Collection<QueryLogRecord> logs) {
        Collection<QueryRecord> queryRecords = new LinkedList<QueryRecord>();
        Iterator iter = logs.iterator();

        while (iter.hasNext()) {
            QueryLogRecord record = (QueryLogRecord) iter.next();

            if(isSinglePattern(record))
                queryRecords.add(new QueryRecordCAdapter(record, getMateralizationManager()));

        }
        return queryRecords;
    }

    private static boolean isSinglePattern(QueryLogRecord record) {
        TupleExpr expr = record.getExpr();

        Collection<StatementPattern> patterns = StatementPatternCollector.process(expr);

        if(patterns.size() != 1) {
            System.out.println("Only single-patterns supported");
            //       logger.info("Only single-patterns supported");
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

    public static RDFCircleSTHolesHistogram loadPreviousHistogram(String logFolder) {
        File jsonHist = new File(logFolder + "histJSON.txt");

        if (!jsonHist.exists()) {
            //       logger.info("Creating a new histogram.");
            System.out.println("Creating a new histogram");
        }
        else {
            //        logger.info("Deserializing previous histogram.");
            System.out.println("Deserializing previous histogram");
        }

        if (!jsonHist.exists()) {
            return new RDFCircleSTHolesHistogram( (STHolesBucket) getFixedRoot());
        }
        else {
            try {
                return new JSONCDeserializer(logFolder + "histJSON.txt").getHistogram();
            } catch (ParseException e) {
                return deserializeBackup(logFolder);
            }
        }

    }

    private static RDFCircleSTHolesHistogram deserializeBackup(String logFolder) {
        RDFCircleSTHolesHistogram rdfstHolesHistogram = null;

        File file1 = new File(logFolder + "histJSON.txt");

        if(file1.exists())
            getBackup(logFolder, true);

        try {
            rdfstHolesHistogram =  new JSONCDeserializer(logFolder + "histJSON.txt").getHistogram();
        } catch (ParseException e1) {
            new HistogramException("Problem in deserializing JSON back up file");
        }
        return rdfstHolesHistogram;
    }

    public static void serializeHistogram(STHolesCircleHistogram histogram, String path) {
        //      logger.info("Serializing histogram to JSON in : " + path + "histJSON.txt");
        System.out.println("Serializing histogram to JSON in : " + path + "histJSON.txt");

        File file1 = new File(path + "histJSON.txt");

        if(file1.exists())
            keepBackup(path, true);

        try {
            new JSONCSerializer(histogram, path + "histJSON.txt");

        } catch (IOException e) {
            System.err.println("Problem with JSON serialization of histogram "+e.getMessage());
            getBackup(path, true);
        }

        keepBackup(path, false);


        //    logger.info("Serializing histogram to VOID in : " + path + "histVOID.ttl");
        System.out.println("Serializing histogram to VOID in : " + path + "histVOID.ttl");
        try {
            new VoIDCSerializer("application/x-turtle", path + "histVOID.ttl").serialize(histogram);

        } catch (RDFHandlerException e) {
            System.err.println("Problem with VOID serialization of histogram");
            getBackup(path, false);
        }
    }

    /** depending on the type of a file (JSON or VOID) keep a back up before the update of the respective file.
     *  With backup we avoid to miss histograms' files when an unexpected error occurs at writing time
     */
    private static void keepBackup(String path, boolean type) {

        File file1, file2 = null;
        if(type) {
            file1 = new File(path + "histJSON.txt");
            file2 = new File(path + "histJSONtemp.txt");
        } else {
            file1 = new File(path + "histVOID.ttl");
            file2 = new File(path + "histVOIDtemp.ttl");
        }

        FileChannel src = null;
        FileChannel dest = null;
        try {

            src = new FileInputStream(file1).getChannel();
            dest = new FileOutputStream(file2).getChannel();
            dest.transferFrom(src, 0, src.size());

        } catch (FileNotFoundException e) {
            System.err.println("File not found "+e.getMessage());
            return;
        } catch (IOException e) {
            new HistogramException(e);
        } finally {
            try {
                if(src != null)
                    src.close();
                if(dest != null)
                    dest.close();
            } catch (IOException e) {
                new HistogramException(e);
            }
        }
    }

    private static void getBackup(String path, boolean type) {
        File file1, file2 = null;
        if(type) {
            System.out.println("get JSON backup");

            file1 = new File(path + "histJSON.txt");
            file2 = new File(path + "histJSONtemp.txt");

            if(! file2.renameTo(file1)) {
                new HistogramException("Error in getting JSON backup file");
            }

        } else {
            System.out.println("get VOID backup");

            file1 = new File(path + "histVOID.ttl");
            file2 = new File(path + "histVOIDtemp.ttl");

            if(! file2.renameTo(file1)) {
                new HistogramException("Error in getting VOID backup file");
            }

        }

    }

    //?????
    private static STHolesBucket getFixedRoot() {
        //RDFRectangle box = new RDFRectangle(new RDFURIRange(), new ExplicitSetRange<URI>(), new RDFValueRange());
        RDFCircle box = new RDFCircle(new RDFStrRange(), new ExplicitSetRange<URI>(), new RDFValueRange());

        List<Long> list = new ArrayList<Long>();
        list.add((long)2576877);//2004: 2318520
        list.add((long)1);
        list.add((long)20692);//2004: 19709

        Stat stats = new Stat((long) 17447544, list);// 2004: 15371754

        return new STHolesBucket(box, stats);
    }
}
