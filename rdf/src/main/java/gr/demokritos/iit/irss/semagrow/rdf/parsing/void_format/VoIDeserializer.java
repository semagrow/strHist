package gr.demokritos.iit.irss.semagrow.rdf.parsing.void_format;

import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.base.Stat;
import gr.demokritos.iit.irss.semagrow.base.range.CalendarRange;
import gr.demokritos.iit.irss.semagrow.base.range.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import gr.demokritos.iit.irss.semagrow.base.range.PrefixRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFLiteralRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.parsing.HistogramIO;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesBucket;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import org.openrdf.model.*;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by nickozoulis on 8/10/2014.
 */
public class VoIDeserializer {

    private Model model;
    private STHolesBucket<RDFRectangle> rootBucket;

    public VoIDeserializer(String path) {

        model = readModelFromFile(path);

        // Root node is set by Eleon as (svd:datasetTop, void:subset, rootResource).
        Resource root = model.filter(VoIDSerializer.createResource(VoIDSerializer.eleonRootNamespace),
                VoIDSerializer.subset, null).objectResource();

        rootBucket = initBucket(root, null);

        System.out.println(rootBucket);
    }


    private STHolesBucket<RDFRectangle> initBucket(Resource res, STHolesBucket<RDFRectangle> parent) {
        System.out.println(res);
        STHolesBucket<RDFRectangle> bucket = new STHolesBucket<RDFRectangle>();
        bucket.setParent(parent);

        // Filling bucket fields.
        bucket.setStatistics(getBucketStats(res));
        bucket.setBox(getRectangle(res));

        for (Value v : getNodeChildren(res))
            bucket.getChildren().add(initBucket((Resource)v, bucket));

        return bucket;
    }


    public STHolesBucket<RDFRectangle> getRootBucket() {
        return this.rootBucket;
    }


    private RDFRectangle getRectangle(Resource res) {
        return new RDFRectangle(
                getPrefixRange(res, VoIDSerializer.uriRegexPattern),
                getExplicitSetRange(res, VoIDSerializer.property),
                getRDFLiteralRange(res));
    }


    private PrefixRange getPrefixRange(Resource res, URI predicate) {

        ArrayList<String> arrList = new ArrayList<String>();

        Set<Value> set = model.filter(res, predicate, null).objects();

        for (Value v : set)
            arrList.add(v.stringValue());

        return new PrefixRange(arrList);
    }


    private ExplicitSetRange getExplicitSetRange(Resource res, URI predicate) {

        Set<String> hashSet = new HashSet<String>();

        Set<Value> set = model.filter(res, predicate, null).objects();

        for (Value v : set)
            hashSet.add(v.toString());

        return new ExplicitSetRange(hashSet);
    }


    private RDFLiteralRange getRDFLiteralRange(Resource res) {
        Map <URI, RangeLength<?>> objectRanges = new HashMap<URI, RangeLength<?>>();

        IntervalRange integerInterval = getIntInterval(res);
        if (integerInterval != null)
            objectRanges.put(XMLSchema.INTEGER, integerInterval);

        CalendarRange calendarRange = getCalendarRange(res);
        if (calendarRange != null)
            objectRanges.put(XMLSchema.DATETIME, calendarRange);

        PrefixRange prefixRange = getPrefixRange(res, VoIDSerializer.stringObjectRegexPattern);
        if (!prefixRange.isEmpty())
            objectRanges.put(XMLSchema.STRING, prefixRange);

        return new RDFLiteralRange(objectRanges);
    }


    private CalendarRange getCalendarRange(Resource res) {

        Model m = model.filter(res, VoIDSerializer.dateInterval, null);
        if (!m.isEmpty()) {

            SimpleDateFormat parserSDF = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy");
            Resource dateRange = m.objectResource();
            Date from = null, to = null;
            try {
                from = parserSDF.parse(model.filter(dateRange, VoIDSerializer.from, null).objectString());
                to = parserSDF.parse(model.filter(dateRange, VoIDSerializer.to, null).objectString());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return new CalendarRange(from, to);
        } else
            return null;
    }


    private IntervalRange getIntInterval(Resource res) {

        Model m = model.filter(res, VoIDSerializer.integerInterval, null);
        if (!m.isEmpty()) {

            Resource intRange = m.objectResource();

            int from = Integer.parseInt(model.filter(intRange, VoIDSerializer.from, null).objectString());
            int to = Integer.parseInt(model.filter(intRange, VoIDSerializer.to, null).objectString());

            return new IntervalRange<Integer>(from, to);
        } else
            return null;
    }


    private Stat getBucketStats(Resource res) {

        Long frequency = Long.parseLong(model.filter(res, VoIDSerializer.triples, null).objectString());

        List<Long> distinctCount = new ArrayList<Long>();
        distinctCount.add(Long.parseLong(model.filter(res, VoIDSerializer.distinctSubjects, null).objectString()));
        distinctCount.add(Long.parseLong(model.filter(res, VoIDSerializer.properties, null).objectString()));
        distinctCount.add(Long.parseLong(model.filter(res, VoIDSerializer.distinctObjects, null).objectString()));

        return new Stat(frequency, distinctCount);
    }


    private Set<Value> getNodeChildren(Resource res) {
        return model.filter(res, VoIDSerializer.subset, null).objects();
    }


    private Model readModelFromFile(String path) {

        InputStream inputStream = null;
        try {
            inputStream  = new FileInputStream(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        RDFFormat format = Rio.getParserFormatForFileName(path);

        Model model = null;
        try {
            model =  Rio.parse(inputStream, "", format);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RDFParseException e) {
            e.printStackTrace();
        }

        return model;
    }


    public static void main(String[] args) {
        STHolesBucket<RDFRectangle> rootBucket =
                new VoIDeserializer("/home/nickozoulis/git/sthist/rdf/src/main/resources/histVoID.ttl").
                        getRootBucket();

        STHolesHistogram<RDFRectangle> histogram = new STHolesHistogram();
        histogram.setRoot(rootBucket);

        new HistogramIO("/home/nickozoulis/git/sthist/rdf/src/main/resources/histJSON.txt", histogram).write();

    }

}
