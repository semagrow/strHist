package gr.demokritos.iit.irss.semagrow.rdf.io.json;

import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.base.Stat;
import gr.demokritos.iit.irss.semagrow.base.range.CalendarRange;
import gr.demokritos.iit.irss.semagrow.base.range.CircleRange;
import gr.demokritos.iit.irss.semagrow.base.range.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import gr.demokritos.iit.irss.semagrow.rdf.*;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesBucket;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by katerina on 23/11/2015.
 */
public class JSONCDeserializer {

    private RDFCircleSTHolesHistogram histogram;


    public JSONCDeserializer(String path) throws ParseException {
        histogram = new RDFCircleSTHolesHistogram();
        histogram.setRoot(readJSON(path));
    }


    public RDFCircleSTHolesHistogram getHistogram() {
        return this.histogram;
    }


    private STHolesBucket<RDFCircle> readJSON(String path) throws ParseException {
        JSONParser parser = new JSONParser();
        Object obj;
        JSONObject jsonObject;
        STHolesBucket<RDFCircle> rootBucket = null;

        try {
            obj = parser.parse(new FileReader(path));
            jsonObject = (JSONObject) obj;

            obj = jsonObject.get("bucket");
            JSONObject bucket = (JSONObject)obj;

            rootBucket = getBucket(bucket);

            histogram.setBucketNum((Long)jsonObject.get("numOfBuckets"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rootBucket;
    }


    private STHolesBucket<RDFCircle> getBucket(JSONObject b) {
        STHolesBucket<RDFCircle> bucket = null;
        JSONObject jsonObject;
        RDFCircle box;
        Stat statistics;
        Collection<STHolesBucket> children;

        // Get box
        box = getBox(b.get("box"));

        // Get statistics
        statistics = getStatistics(b.get("statistics"));

        // Instantiate root Bucket
        bucket = new STHolesBucket<RDFCircle>(box, statistics);

        // Get children buckets and set their parent
        bucket.getChildren().addAll(
                getChildren(b.get("children"), bucket));

        return bucket;
    }


    private Collection<STHolesBucket<RDFCircle>> getChildren(Object childrenObj, STHolesBucket parent) {
        Collection<STHolesBucket<RDFCircle>> children = new ArrayList<STHolesBucket<RDFCircle>>();
        JSONArray array = (JSONArray)childrenObj;
        JSONObject temp;
        STHolesBucket<RDFCircle> tempBucket;

        Iterator<JSONObject> iterator = array.iterator();

        while (iterator.hasNext()) {
            temp = (JSONObject)iterator.next();

            // Get Bucket
            tempBucket = getBucket((JSONObject)temp.get("bucket"));
            // And set its parent
            tempBucket.setParent(parent);

            children.add(tempBucket);
        }

        return children;
    }


    private RDFCircle getBox(Object boxObj) {
        JSONObject jsonObject;
        RDFStrRange subjectRange;
        ExplicitSetRange<URI> predicateRange;
        RDFValueRange objectRange;

        jsonObject = (JSONObject)boxObj;

        subjectRange = getSubject(jsonObject.get("subject"));
        predicateRange = getPredicate(jsonObject.get("predicate"));
        objectRange = getObject(jsonObject.get("object"));

        return new RDFCircle(subjectRange, predicateRange, objectRange);
    }


    private RDFValueRange getObject(Object objectObj) {
        JSONObject jsonObject = (JSONObject)objectObj;

        return new RDFValueRange(
                getRDFURIRange(jsonObject.get("rdfURIRange")),
                getRDFLiteralRange(jsonObject.get("rdfLiteralRange")));
    }


    private RDFURIRange getRDFURIRange(Object objectObj) {
        JSONObject jsonObject = (JSONObject)objectObj, temp;
        JSONArray array = (JSONArray)jsonObject.get("array");
        Iterator<JSONObject> iterator = array.iterator();
        ArrayList<String> list = new ArrayList<String>();

        while (iterator.hasNext()) {
            temp = iterator.next();

            list.add((String)temp.get("rdfUri"));
        }

        return (list.isEmpty()) ? new RDFURIRange() : new RDFURIRange(list);
    }


    private RDFLiteralRange getRDFLiteralRange(Object objectObj) {
        Map<URI,RangeLength<?>> ranges = new HashMap<URI, RangeLength<?>>();
        RDFLiteralRange literalRange = null;
        JSONObject jsonObject = (JSONObject)objectObj, temp;
        JSONArray array = (JSONArray)jsonObject.get("array");
        Iterator<JSONObject> iterator = array.iterator();

        while (iterator.hasNext()) {
            temp = iterator.next();

            if (temp.get("intervalRange") != null) {
                getObjectIntervalRange(temp.get("intervalRange"), ranges);

            } else if (temp.get("prefixRange") != null) {
                ranges.put(XMLSchema.STRING, getSubject(temp.get("prefixRange")));

            } else if (temp.get("calendarRange") != null) {
                getObjectCalendarRange(temp.get("calendarRange"), ranges);
            }
        }// while

        return (ranges.isEmpty()) ? new RDFLiteralRange() : new RDFLiteralRange(ranges);
    }


    private void getObjectIntervalRange(Object obj, Map<URI,RangeLength<?>> ranges) {
        JSONObject jsonObject = (JSONObject)obj;

        long low = (Long) jsonObject.get("low");
        long high = (Long) jsonObject.get("high");

        ranges.put(XMLSchema.INT, new IntervalRange((int) low, (int) high));
    }


    private void getObjectCalendarRange(Object obj, Map<URI,RangeLength<?>> ranges) {
        JSONObject jsonObject = (JSONObject)obj;

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        Date dateBegin = null, dateEnd = null;

        try {
            dateBegin = format.parse((String) jsonObject.get("begin"));
            dateEnd = format.parse((String) jsonObject.get("end"));
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        ranges.put(XMLSchema.DATETIME, new CalendarRange(dateBegin, dateEnd));
    }// getObjectCalendarRange


    private RDFStrRange getSubject(Object subjectObj) {
        String center = "";
        double radius = 0.0;
        JSONObject jsonObject = (JSONObject)subjectObj, temp = null;
        JSONArray array = (JSONArray)jsonObject.get("array");
        Iterator<JSONObject> iterator = array.iterator();

        while (iterator.hasNext()) {
            temp = iterator.next();

            if (((String)temp.get("type")).equals("url")) {
                center = (String) temp.get("center");
                radius = (Double) temp.get("radius");
            }
        }

        return new RDFStrRange(center, radius);
    }


    private ExplicitSetRange<URI> getPredicate(Object predicateObj) {
        Set<URI> predicateSet = new HashSet<URI>();
        JSONObject jsonObject = (JSONObject)predicateObj, temp = null;
        JSONArray array = (JSONArray)jsonObject.get("array");
        Iterator<JSONObject> iterator = array.iterator();

        while (iterator.hasNext()) {
            temp = iterator.next();

            if (((String)temp.get("type")).equals("uri")) {
                URI p = ValueFactoryImpl.getInstance().createURI((String) temp.get("value"));
                predicateSet.add(p);
            }
        }

        return (predicateSet.isEmpty()) ? new ExplicitSetRange<URI>() : new ExplicitSetRange<URI>(predicateSet);
    }


    private Stat getStatistics(Object statsObj) {
        Stat statistics = null;
        JSONObject jsonObject;
        Long frequency;
        List<Long> distinctCount = new ArrayList<Long>();

        jsonObject = (JSONObject)statsObj;

        frequency = (Long)jsonObject.get("triples");

        distinctCount.add((Long)jsonObject.get("distinctSubjects"));
        distinctCount.add((Long)jsonObject.get("distinctPredicates"));
        distinctCount.add((Long)jsonObject.get("distinctObjects"));

        return new Stat(frequency, distinctCount);
    }
}
