package gr.demokritos.iit.irss.semagrow.rdf.parsing.json_format;

import com.cedarsoftware.util.io.JsonWriter;
import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.base.NumRectangle;
import gr.demokritos.iit.irss.semagrow.base.Stat;
import gr.demokritos.iit.irss.semagrow.base.range.CalendarRange;
import gr.demokritos.iit.irss.semagrow.base.range.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import gr.demokritos.iit.irss.semagrow.base.range.PrefixRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFLiteralRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.RDFSTHolesHistogram;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesBucket;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import gr.demokritos.iit.irss.semagrow.stholesOrig.STHolesOrigBucket;
import gr.demokritos.iit.irss.semagrow.stholesOrig.STHolesOrigHistogram;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class HistogramIO<R extends Rectangle<R>> {

	private String path;
	private STHolesBucket<R> rootBucket;
    private STHolesHistogram histogram;
    private STHolesOrigHistogram histogramOrig;

   	public HistogramIO(String path, STHolesHistogram histogram) {
		setPath(path);
		setHistogram(histogram);
	}

    public HistogramIO(String filename, STHolesOrigHistogram<NumRectangle> h) {
        setPath(filename);
        this.histogramOrig = h;
    }


    public void write() {
        if (histogram != null)
            writeJSOn();
        else if (histogramOrig != null)
            writeJSOnOrig();
    }


    public static RDFSTHolesHistogram
           read(String path) {

        STHolesBucket<RDFRectangle> root = readJSON(path);
        RDFSTHolesHistogram h = new RDFSTHolesHistogram();
        h.setRoot(root);
        return h;
    }


    public static STHolesOrigHistogram readOrig(String path) {

        STHolesOrigBucket<NumRectangle> root = readJSONOrig(path);
        STHolesOrigHistogram h = new STHolesOrigHistogram();
        h.setRoot(root);
        return h;
    }


	/**
	 * Writes the histogram into a file in jSON format.
	 */
	private void writeJSOn() {
		FileWriter fw;

        try {
            fw = new FileWriter(getPath()/* + ".txt"*/);

            // Write root bucket and its children via chained toJSON calls.
            fw.write(JsonWriter.formatJson(histogram.toJSON().toJSONString()));

            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}


    /**
     * Writes the histogramOrig into a file in jSON format.
     */
    private void writeJSOnOrig() {
        FileWriter fw;

        try {
            fw = new FileWriter(getPath()/* + ".txt"*/);

            // Write root bucket and its children via chained toJSON calls.
            fw.write(JsonWriter.formatJson(histogramOrig.toJSON().toJSONString()));

            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static STHolesOrigBucket<NumRectangle> readJSONOrig(String path) {

        JSONParser parser = new JSONParser();
        Object obj;
        JSONObject jsonObject;
        STHolesOrigBucket<NumRectangle> rootBucket = null;

        try {
            obj = parser.parse(new FileReader(path));
            jsonObject = (JSONObject) obj;

            obj = jsonObject.get("bucket");
            JSONObject bucket = (JSONObject)obj;

            rootBucket = getBucketOrig(bucket);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return rootBucket;
    }// readJSONOrig


    private static STHolesOrigBucket<NumRectangle> getBucketOrig(JSONObject b) {

        STHolesOrigBucket<NumRectangle> bucket = null;
        JSONObject jsonObject;
        NumRectangle box;
        long frequency;
        Collection<STHolesOrigBucket> children;

        // Get box
        box = getBoxOrig(b.get("box"));

        // Get frequency
        frequency = (Long)(b.get("frequency"));

        // Instantiate root Bucket
        bucket = new STHolesOrigBucket<NumRectangle>(box, frequency);

        // Get children buckets and set their parent
        bucket.getChildren().addAll(
                getChildrenOrig(b.get("children"), bucket));

        return bucket;
    }// getBucketOrig


    private static Collection<STHolesOrigBucket<NumRectangle>> getChildrenOrig
            (Object childrenObj, STHolesOrigBucket<NumRectangle> parent) {

        Collection<STHolesOrigBucket<NumRectangle>> children = new ArrayList<STHolesOrigBucket<NumRectangle>>();
        JSONArray array = (JSONArray)childrenObj;
        JSONObject temp;
        STHolesOrigBucket<NumRectangle> tempBucket;

        Iterator<JSONObject> iterator = array.iterator();

        while (iterator.hasNext()) {
            temp = (JSONObject)iterator.next();

            // Get Bucket
            tempBucket = getBucketOrig((JSONObject) temp.get("bucket"));
            // And set its parent
            tempBucket.setParent(parent);

            children.add(tempBucket);
        }

        return children;

    }// getChildrenOrig


    private static NumRectangle getBoxOrig(Object boxObj) {

        JSONObject jsonObject;
        JSONArray jsonArray;

        jsonObject = (JSONObject)boxObj;
        jsonArray = (JSONArray)jsonObject.get("rectangle");
        List<IntervalRange> list = new ArrayList<IntervalRange>();

        for (Object obj : jsonArray) {

            jsonObject = (JSONObject)obj;

            long low = (Long) jsonObject.get("low");
            long high = (Long) jsonObject.get("high");
            list.add(new IntervalRange((int)low, (int)high));
        }

        return new NumRectangle(list);
    }// getBoxOrig


    public static STHolesBucket<RDFRectangle> readJSON(String path) {
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
    }// readJSON


    private static STHolesBucket<RDFRectangle> getBucket(JSONObject b) {
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
    }// getBucket


    private static Collection<STHolesBucket<RDFRectangle>> getChildren(Object childrenObj, STHolesBucket parent) {
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
    }// getChildren


    private static RDFRectangle getBox(Object boxObj) {
        JSONObject jsonObject;
        PrefixRange subjectRange;
        ExplicitSetRange<String> predicateRange;
        RDFLiteralRange objectRange;

        jsonObject = (JSONObject)boxObj;

        subjectRange = getSubject(jsonObject.get("subject"));
        predicateRange = getPredicate(jsonObject.get("predicate"));
        objectRange = getObject(jsonObject.get("object"));

        return new RDFRectangle(subjectRange, predicateRange, objectRange);
    }// getBox


    private static RDFLiteralRange getObject(Object objectObj) {
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

        return new RDFLiteralRange(ranges);
    }// getObject


    private static void getObjectIntervalRange(Object obj, Map<URI,RangeLength<?>> ranges) {
        JSONObject jsonObject = (JSONObject)obj;

        long low = (Long) jsonObject.get("low");
        long high = (Long) jsonObject.get("high");

        ranges.put(XMLSchema.INTEGER, new IntervalRange((int) low, (int) high));
    }// getObjectIntervalRange


    private static void getObjectCalendarRange(Object obj, Map<URI,RangeLength<?>> ranges) {
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


    private static PrefixRange getSubject(Object subjectObj) {
        ArrayList<String> prefixList = new ArrayList<String>();
        JSONObject jsonObject = (JSONObject)subjectObj, temp = null;
        JSONArray array = (JSONArray)jsonObject.get("array");
        Iterator<JSONObject> iterator = array.iterator();

        while (iterator.hasNext()) {
            temp = iterator.next();

            if (((String)temp.get("type")).equals("url"))
                prefixList.add((String)temp.get("value"));
        }

        return new PrefixRange(prefixList);
    }// getSubject


    private static ExplicitSetRange getPredicate(Object predicateObj) {
        Set<String> predicateSet = new HashSet<String>();
        JSONObject jsonObject = (JSONObject)predicateObj, temp = null;
        JSONArray array = (JSONArray)jsonObject.get("array");
        Iterator<JSONObject> iterator = array.iterator();

        while (iterator.hasNext()) {
            temp = iterator.next();

            if (((String)temp.get("type")).equals("uri"))
                predicateSet.add((String)temp.get("value"));
        }

        return new ExplicitSetRange(predicateSet);
    }// getPredicate


    private static Stat getStatistics(Object statsObj) {
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
    }// getStatistics


	/*
	 * Getters & Setters.
	 */
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public STHolesBucket<R> getRootBucket() {
		return rootBucket;
	}
	public void setRootBucket(STHolesBucket<R> rootBucket) {
		this.rootBucket = rootBucket;
	}
    public STHolesHistogram getHistogram() {
        return histogram;
    }
    public void setHistogram(STHolesHistogram histogram) {
        this.histogram = histogram;
    }

}
