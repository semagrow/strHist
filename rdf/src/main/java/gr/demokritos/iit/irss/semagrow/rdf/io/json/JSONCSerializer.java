package gr.demokritos.iit.irss.semagrow.rdf.io.json;

import com.cedarsoftware.util.io.JsonWriter;
import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.base.Stat;
import gr.demokritos.iit.irss.semagrow.base.range.CalendarRange;
import gr.demokritos.iit.irss.semagrow.base.range.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import gr.demokritos.iit.irss.semagrow.base.range.PrefixRange;
import gr.demokritos.iit.irss.semagrow.rdf.*;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesBucket;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesCircleHistogram;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * Created by katerina on 23/11/2015.
 */
public class JSONCSerializer {

    public JSONCSerializer(STHolesCircleHistogram<RDFCircle> histogram, String outputPath) throws IOException {
        writeJSON(histogram, outputPath);
    }


    /**
     * Writes the histogram into a file in jSON format.
     */
    private void writeJSON(STHolesCircleHistogram<RDFCircle> histogram, String outputPath) throws IOException {
        FileWriter fw;


        fw = new FileWriter(outputPath);

        // Write root bucket and its children via recursive calls.
        fw.write(JsonWriter.formatJson(serialize(histogram).toJSONString()));

        fw.close();

    }


    private JSONObject serialize(STHolesCircleHistogram<RDFCircle> histogram) {
        JSONObject jsonRoot = serialize(histogram.getRoot());
        jsonRoot.put("numOfBuckets", histogram.getBucketsNum());
        return jsonRoot;
    }


    private JSONObject serialize(STHolesBucket<RDFCircle> bucket) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("box", getJSONBox(bucket.getBox()));
        jsonObject.put("statistics", getJSONStats(bucket.getStatistics()));
        jsonObject.put("childrenNumber", bucket.getChildren().size());

        JSONArray array = new JSONArray();
        for (STHolesBucket b : bucket.getChildren())
            array.add(serialize(b));
        jsonObject.put("children", array);

        JSONObject jsonBucket = new JSONObject();
        jsonBucket.put("bucket", jsonObject);

        return jsonBucket;
    }


    private JSONObject getJSONStats(Stat stats) {
        JSONObject statistics = new JSONObject();

        statistics.put("triples", stats.getFrequency());
        statistics.put("distinctSubjects", stats.getDistinctCount().get(0));
        statistics.put("distinctPredicates", stats.getDistinctCount().get(1));
        statistics.put("distinctObjects", stats.getDistinctCount().get(2));

        return statistics;
    }


    private JSONObject getJSONBox(RDFCircle box) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("subject", getJSONSubject(box.getSubjectRange()));
        jsonObject.put("predicate", getJSONPredicate(box.getPredicateRange()));
        jsonObject.put("object", getJSONObject(box.getObjectRange()));

        return jsonObject;
    }


    private JSONObject getJSONObject(RDFValueRange objectRange) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("rdfURIRange", getJSONRDFURIRange(objectRange.getUriRange()));
        jsonObject.put("rdfLiteralRange", getJSONRDFLiteralRange(objectRange.getLiteralRange()));

        return jsonObject;
    }

    private JSONObject getJSONRDFLiteralRange(RDFLiteralRange literalRange) {
        JSONObject object;
        JSONArray array = new JSONArray();

        for (Map.Entry<URI,RangeLength<?>> entry : literalRange.getRanges().entrySet()) {
            object = new JSONObject();

            if (entry.getKey().equals(XMLSchema.INTEGER) || entry.getKey().equals(XMLSchema.INT) || entry.getKey().equals(XMLSchema.LONG)) {
                object.put("intervalRange", getJSONIntervalRange((IntervalRange)entry.getValue()));

            } else if (entry.getKey().equals(XMLSchema.DATETIME)) {
                object.put("calendarRange", getJSONCalendarRange((CalendarRange)entry.getValue()));

            } else if (entry.getKey().equals(XMLSchema.STRING)) {
                PrefixRange pr = null;
                Object obj = entry.getValue();

                if (obj instanceof RDFURIRange) {
                    RDFURIRange uriRange = (RDFURIRange)obj;
                    pr = new PrefixRange(uriRange.getPrefixList());
                } else if (obj instanceof PrefixRange) {
                    pr = (PrefixRange)obj;
                }

                object.put("prefixRange", getJSONPrefixRange(pr));
            }

            array.add(object);
        }// for

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("array", array);

        return jsonObject;
    }

    private JSONObject getJSONRDFURIRange(RDFURIRange uriRange) {
        JSONObject object;
        JSONArray array = new JSONArray();

        for (String s : uriRange.getPrefixList()) {
            object = new JSONObject();
            object.put("rdfUri", s);

            array.add(object);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("array", array);

        return jsonObject;
    }


    private JSONObject getJSONPrefixRange(PrefixRange prefixRange) {
        return getJSONSubject(prefixRange);
    }


    private JSONObject getJSONCalendarRange(CalendarRange calendarRange) {
        JSONObject range = new JSONObject();

        range.put("begin", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+hh:mm").format(calendarRange.getBegin()));
        range.put("end", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+hh:mm").format(calendarRange.getEnd()));

        return range;
    }


    private JSONObject getJSONIntervalRange(IntervalRange intervalRange) {
        JSONObject range = new JSONObject();

        range.put("low", intervalRange.getLow());
        range.put("high", intervalRange.getHigh());

        return range;
    }


    private JSONObject getJSONPredicate(ExplicitSetRange<URI> predicateRange) {
        JSONObject predicate;
        JSONArray array = new JSONArray();

        for (URI p : predicateRange.getItems()) {
            predicate = new JSONObject();
            predicate.put("value", p.stringValue());
            predicate.put("type", "uri");
            array.add(predicate);
        }

        JSONObject jSONObj = new JSONObject();
        jSONObj.put("array", array);

        return jSONObj;
    }


    private JSONObject getJSONSubject(RDFStrRange subjectRange) {
        JSONObject jsonObject;

        JSONArray array = new JSONArray();


        jsonObject = new JSONObject();
        jsonObject.put("center", subjectRange.getCenter());
        jsonObject.put("type", "url");
        jsonObject.put("radius", subjectRange.getRadius());
        array.add(jsonObject);


        JSONObject jSONObj = new JSONObject();
        jSONObj.put("array", array);


        return jSONObj;
    }

    private JSONObject getJSONSubject(PrefixRange subjectRange) {
        JSONObject jsonObject;

        JSONArray array = new JSONArray();

        for (String p : subjectRange.getPrefixList()) {
            jsonObject = new JSONObject();
            jsonObject.put("value", p);
            jsonObject.put("type", "url");
            array.add(jsonObject);
        }

        JSONObject jSONObj = new JSONObject();
        jSONObj.put("array", array);


        return jSONObj;
    }
}
