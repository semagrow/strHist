package gr.demokritos.iit.irss.semagrow.rdf.io.json;

import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.base.Stat;
import gr.demokritos.iit.irss.semagrow.base.range.CalendarRange;
import gr.demokritos.iit.irss.semagrow.base.range.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import gr.demokritos.iit.irss.semagrow.base.range.PrefixRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFLiteralRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.RDFURIRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFValueRange;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesBucket;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
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
 * Created by angel on 10/11/14.
 * @author nickozoulis
 */
public class JSONDeserializer {

    private STHolesHistogram<RDFRectangle> histogram;


    public JSONDeserializer(String path) {
        histogram = new STHolesHistogram<RDFRectangle>();
        histogram.setRoot(readJSON(path));
    }


    public STHolesHistogram<RDFRectangle> getHistogram() {
        return this.histogram;
    }


    private STHolesBucket<RDFRectangle> readJSON(String path) {
        JSONParser parser = new JSONParser();
        Object obj;
        JSONObject jsonObject;
        STHolesBucket<RDFRectangle> rootBucket = null;

        try {
            obj = parser.parse(new FileReader(path));
            jsonObject = (JSONObject) obj;

            obj = jsonObject.get("bucket");
            JSONObject bucket = (JSONObject)obj;

            rootBucket = getBucket(bucket);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return rootBucket;
    }


    private STHolesBucket<RDFRectangle> getBucket(JSONObject b) {
        STHolesBucket<RDFRectangle> bucket = null;
        JSONObject jsonObject;
        RDFRectangle box;
        Stat statistics;
        Collection<STHolesBucket> children;

        // Get box
        box = getBox(b.get("box"));

        // Get statistics
        statistics = getStatistics(b.get("statistics"));

        // Instantiate root Bucket
        bucket = new STHolesBucket<RDFRectangle>(box, statistics);

        // Get children buckets and set their parent
        bucket.getChildren().addAll(
                getChildren(b.get("children"), bucket));

        return bucket;
    }


    private Collection<STHolesBucket<RDFRectangle>> getChildren(Object childrenObj, STHolesBucket parent) {
        Collection<STHolesBucket<RDFRectangle>> children = new ArrayList<STHolesBucket<RDFRectangle>>();
        JSONArray array = (JSONArray)childrenObj;
        JSONObject temp;
        STHolesBucket<RDFRectangle> tempBucket;

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


    private RDFRectangle getBox(Object boxObj) {
        JSONObject jsonObject;
        RDFURIRange subjectRange;
        ExplicitSetRange<URI> predicateRange;
        RDFValueRange objectRange;

        jsonObject = (JSONObject)boxObj;

        subjectRange = getSubject(jsonObject.get("subject"));
        predicateRange = getPredicate(jsonObject.get("predicate"));
        objectRange = getObject(jsonObject.get("object"));

        return new RDFRectangle(subjectRange, predicateRange, objectRange);
    }


    private RDFValueRange getObject(Object objectObj) {
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

        return new RDFValueRange(null,new RDFLiteralRange(ranges));
    }


    private void getObjectIntervalRange(Object obj, Map<URI,RangeLength<?>> ranges) {
        JSONObject jsonObject = (JSONObject)obj;

        long low = (Long) jsonObject.get("low");
        long high = (Long) jsonObject.get("high");

        ranges.put(XMLSchema.INTEGER, new IntervalRange((int) low, (int) high));
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

        ranges.put(XMLSchema.INTEGER, new CalendarRange(dateBegin, dateEnd));
    }// getObjectCalendarRange


    private RDFURIRange getSubject(Object subjectObj) {
        ArrayList<String> prefixList = new ArrayList<String>();
        JSONObject jsonObject = (JSONObject)subjectObj, temp = null;
        JSONArray array = (JSONArray)jsonObject.get("array");
        Iterator<JSONObject> iterator = array.iterator();

        while (iterator.hasNext()) {
            temp = iterator.next();

            if (((String)temp.get("type")).equals("url"))
                prefixList.add((String)temp.get("value"));
        }

        return new RDFURIRange(prefixList);
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

        return new ExplicitSetRange<URI>(predicateSet);
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


    public static void main(String[] args) {
        STHolesHistogram<RDFRectangle> histogram =
                new JSONDeserializer("/home/nickozoulis/git/sthist/rdf/src/main/resources/histJSON.txt").
                        getHistogram();

        new JSONSerializer(histogram, "/home/nickozoulis/git/sthist/rdf/src/main/resources/histJSONN.txt");
    }
}
