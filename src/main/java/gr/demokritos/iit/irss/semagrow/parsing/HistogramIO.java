package gr.demokritos.iit.irss.semagrow.parsing;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.cedarsoftware.util.io.JsonWriter;
import gr.demokritos.iit.irss.semagrow.api.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.api.PrefixRange;
import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.rdf.RDFLiteralRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.Stat;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesBucket;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class HistogramIO<R extends Rectangle<R>> {

	private String path;
	private STHolesBucket<R> rootBucket;

    public STHolesHistogram getHistogram() {
        return histogram;
    }

   	public HistogramIO(String path, STHolesHistogram histogram) {
		setPath(path);
		setHistogram(histogram);
	}


	public void write() {
		writeJSOn();
//		writeNonBinary();
//		writeBinary();
	}


    public static STHolesBucket read(String path) {
        return readJSON(path);
    }


	/**
	 * Writes the histogram into a file in JSOn format.
	 */
	private void writeJSOn() {
		FileWriter fw;

        try {
            fw = new FileWriter(getPath() + ".txt");

            // Write root bucket and its children via chained toJSON calls.
            fw.write(JsonWriter.formatJson(histogram.toJSON().toJSONString()));

            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}


    public static STHolesBucket readJSON(String path) {
        JSONParser parser = new JSONParser();
        Object obj;
        JSONObject jsonObject;
        STHolesBucket rootBucket = null;

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


    private static STHolesBucket getBucket(JSONObject b) {
        STHolesBucket bucket = null;
        JSONObject jsonObject;
        RDFRectangle box;
        Stat statistics;
        Collection<STHolesBucket> children;

        // Get box
        box = getBox(b.get("box"));

        // Get statistics
        statistics = getStatistics(b.get("statistics"));

        // Instantiate root Bucket
        bucket = new STHolesBucket(box, statistics);
        bucket.setParent(bucket);

        // Get children buckets and set their parent
        bucket.getChildren().addAll(
                getChildren(b.get("children"), bucket));

        return bucket;
    }// getBucket


    private static Collection<STHolesBucket> getChildren(Object childrenObj, STHolesBucket parent) {
        Collection<STHolesBucket> children = new ArrayList<STHolesBucket>();
        JSONArray array = (JSONArray)childrenObj;
        JSONObject temp;
        STHolesBucket tempBucket;

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
        RDFLiteralRange literalRange = null;
        JSONObject jsonObject = (JSONObject)objectObj;

        String type = (String)jsonObject.get("type");
        JSONObject value = (JSONObject)jsonObject.get("value");

        if (type.equals("intervalRange")) {
            literalRange = new RDFLiteralRange((Long)value.get("low"), (Long)value.get("high"));

        } else  if (type.equals("calendarRange")) {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

            Date dateLow = null, dateHigh = null;

            try {
                dateLow = format.parse((String)value.get("begin"));
                dateHigh = format.parse((String)value.get("end"));
            } catch (java.text.ParseException e) {e.printStackTrace();}

            literalRange = new RDFLiteralRange(dateLow, dateHigh);
        }

        return literalRange;
    }// getObject


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


	/**
	 * Writes the histogram into a file in a non human-readable form.
	 */
	public void writeBinary() {
		ObjectOutputStream oos = null;		
		
		try {			
			oos = new ObjectOutputStream(new FileOutputStream(getPath() + ".ser"));
			
			oos.writeObject(getRootBucket());			
			
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}// writeBinary


	/**
	 * Writes the histogram into a file in a human-readable form.
	 */
	private void writeNonBinary() {
		BufferedWriter bw = null;
		
		try {
			bw = new BufferedWriter(new FileWriter(getPath() + ".txt"));

			// Write root bucket.			
			bw.write(rootBucket.toString());
			
			bw.write("\n{\n");
			// Get root's children.
			Collection<STHolesBucket<R>> children = rootBucket.getChildren();

			System.err.println("Children: " + children.size());

			// Foreach child, call write method recursively.
			for (STHolesBucket<R> child : children) {
				System.err.println("Children of child>> " + child.getChildren().size());
				write(child, bw);
			}
			
			bw.write("\n}\n");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}// writeNonBinary	


	/**
	 * Recursively writes a bucket and its children if exist.
	 */
	private void write(STHolesBucket<R> bucket, BufferedWriter bw) {
		
		try { 
			bw.write("\n{\n");
			bw.write(bucket.toString());
			
			for (STHolesBucket<R> child : bucket.getChildren()) 			
				write(child, bw);		
			
			bw.write("\n}\n");
		} catch (IOException e) {e.printStackTrace();}	

	}// write
	
	
	/**
	 * Reads the binary file and instantiates the rootBucket of the Histogram.	
	 */
	public STHolesBucket<R> readBinary(String path) {
		STHolesBucket<R> rootBucket = null;
		
		ObjectInputStream ois = null;
		
		try {
			ois = new ObjectInputStream(new FileInputStream(path));
						
			rootBucket = (STHolesBucket<R>)ois.readObject();			
			
			ois.close();
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return rootBucket;
	}// readBinary
	

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

    public void setHistogram(STHolesHistogram histogram) {
        this.histogram = histogram;
    }

    private STHolesHistogram histogram;

}
