package gr.demokritos.iit.irss.semagrow.stholesOrig.io.json;

import gr.demokritos.iit.irss.semagrow.base.NumRectangle;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import gr.demokritos.iit.irss.semagrow.stholesOrig.STHolesOrigBucket;
import gr.demokritos.iit.irss.semagrow.stholesOrig.STHolesOrigHistogram;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by nickozoulis on 11/10/2014.
 */
public class JSONDeserializer {

    private STHolesOrigHistogram<NumRectangle> histogram;


    public JSONDeserializer(String path) {
        histogram = new STHolesOrigHistogram<NumRectangle>();
        histogram.setRoot(readJSON(path));
    }


    public STHolesOrigHistogram<NumRectangle> getHistogram() {
        return this.histogram;
    }


    private STHolesOrigBucket<NumRectangle> readJSON(String path) {
        JSONParser parser = new JSONParser();
        Object obj;
        JSONObject jsonObject;
        STHolesOrigBucket<NumRectangle> rootBucket = null;

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


    private STHolesOrigBucket<NumRectangle> getBucket(JSONObject b) {
        STHolesOrigBucket<NumRectangle> bucket = null;
        JSONObject jsonObject;
        NumRectangle box;
        long frequency;
        Collection<STHolesOrigBucket> children;

        // Get box
        box = getBox(b.get("box"));

        // Get frequency
        frequency = (Long)(b.get("frequency"));

        // Instantiate root Bucket
        bucket = new STHolesOrigBucket<NumRectangle>(box, frequency);

        // Get children buckets and set their parent
        bucket.getChildren().addAll(
                getChildren(b.get("children"), bucket));

        return bucket;
    }


    private Collection<STHolesOrigBucket<NumRectangle>> getChildren
            (Object childrenObj, STHolesOrigBucket<NumRectangle> parent) {
        Collection<STHolesOrigBucket<NumRectangle>> children = new ArrayList<STHolesOrigBucket<NumRectangle>>();
        JSONArray array = (JSONArray)childrenObj;
        JSONObject temp;
        STHolesOrigBucket<NumRectangle> tempBucket;

        Iterator<JSONObject> iterator = array.iterator();

        while (iterator.hasNext()) {
            temp = (JSONObject)iterator.next();

            // Get Bucket
            tempBucket = getBucket((JSONObject) temp.get("bucket"));
            // And set its parent
            tempBucket.setParent(parent);

            children.add(tempBucket);
        }

        return children;
    }


    private NumRectangle getBox(Object boxObj) {
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
    }
}
