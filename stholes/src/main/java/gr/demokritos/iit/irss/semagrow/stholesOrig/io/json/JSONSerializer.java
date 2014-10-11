package gr.demokritos.iit.irss.semagrow.stholesOrig.io.json;

import com.cedarsoftware.util.io.JsonWriter;
import gr.demokritos.iit.irss.semagrow.base.NumRectangle;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import gr.demokritos.iit.irss.semagrow.stholesOrig.STHolesOrigBucket;
import gr.demokritos.iit.irss.semagrow.stholesOrig.STHolesOrigHistogram;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by nickozoulis on 11/10/2014.
 */
public class JSONSerializer {

    public JSONSerializer(STHolesOrigHistogram histogram, String outputPath) {
        writeJSON(histogram, outputPath);
    }


    public void writeJSON(STHolesOrigHistogram histogram, String outputPath) {
        FileWriter fw;

        try {
            fw = new FileWriter(outputPath);

            // Write root bucket and its children via recursive calls.
            fw.write(JsonWriter.formatJson(serialize(histogram).toJSONString()));

            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private JSONObject serialize(STHolesOrigHistogram histogram) {
        return serialize(histogram.getRoot());
    }


    private JSONObject serialize(STHolesOrigBucket<NumRectangle> bucket) {
        JSONObject jSONObj = new JSONObject();
        jSONObj.put("box", getJSONBox((NumRectangle)bucket.getBox()));
        jSONObj.put("frequency", bucket.getFrequency());
        jSONObj.put("childrenNumber", bucket.getChildren().size());

        JSONArray array = new JSONArray();
        for (STHolesOrigBucket child : bucket.getChildren())
            array.add(serialize(bucket));
        jSONObj.put("children", array);

        JSONObject jsonBucket = new JSONObject();
        jsonBucket.put("bucket", jSONObj);

        return jsonBucket;
    }

    private JSONObject getJSONBox(NumRectangle box) {
        JSONObject rectangle = new JSONObject();
        JSONArray array = new JSONArray();

        for (IntervalRange ir : box.getDims())
            array.add((getJSONIntervalRange(ir)));

        rectangle.put("rectangle", array);
        return rectangle;
    }


    private JSONObject getJSONIntervalRange(IntervalRange intervalRange) {
        JSONObject range = new JSONObject();

        range.put("low", intervalRange.getLow());
        range.put("high", intervalRange.getHigh());

        return range;
    }

}
