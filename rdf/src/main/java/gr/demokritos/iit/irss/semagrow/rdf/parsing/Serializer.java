package gr.demokritos.iit.irss.semagrow.rdf.parsing;

import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesBucket;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import org.openrdf.model.*;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by nickozoulis on 30/9/2014.
 */
public class Serializer<R extends Rectangle<R>> {

    static class VOID {
        static String NAMESPACE = "http://rdfs.org/ns/void#";
    }

    private int level;
    private Model model;
    private String histogramNamespace = "http://www.semagrow.eu/metadata/histogram/";
    private String outputPath;
    private RDFFormat format;

    private final URI dataset = createURI(VOID.NAMESPACE, "Dataset"),
                      subset = createURI(VOID.NAMESPACE, "subset"),
                      triples = createURI(VOID.NAMESPACE, "triples"),
                      distinctObjects = createURI(VOID.NAMESPACE, "distinctObjects"),
                      distinctSubjects = createURI(VOID.NAMESPACE, "distinctSubjects"),
                      properties = createURI(VOID.NAMESPACE, "properties"),
                      uriRegexPattern = createURI(VOID.NAMESPACE, "uriRegexPattern");


    public Serializer(String format, String outputPath) {

        level = 0;
        this.outputPath = outputPath + "histVoID.txt";
        this.format = Rio.getWriterFormatForMIMEType(format);
        model = new TreeModel();
    }


    public void serialize(STHolesHistogram<R> histogram) {

        serializeBucket(histogram.getRoot(), createResource(++level), 0, true);
        write(outputPath);
    }


    private void write(String outputPath) {

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outputPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Rio.write(model, out, format);
        } catch (RDFHandlerException e) {
            e.printStackTrace();
        }
    }


    /**
     * Serialize bucket
     * @param bucket Histogram's bucket to be serialized.
     * @param bucketResource Subject of the Statement, depicts the lvl of the bucket.
     */
    private void serializeBucket(STHolesBucket<R> bucket, Resource bucketResource, int num, boolean isRoot) {

        if (!isRoot)
            bucketResource = createResource(bucketResource.toString(), num);

        model.add(bucketResource, RDF.TYPE, dataset);
        model.add(bucketResource, triples, createLiteral(bucket.getStatistics().getFrequency()));
        model.add(bucketResource, distinctSubjects, createLiteral(bucket.getStatistics().getDistinctCount().get(0)));
        model.add(bucketResource, properties, createLiteral(bucket.getStatistics().getDistinctCount().get(1)));
        model.add(bucketResource, distinctObjects, createLiteral(bucket.getStatistics().getDistinctCount().get(2)));

        //TODO: Add ranges to the model

        // Declare each child as subset and serialize recursively each bucket.
        int count = 0;
        for (STHolesBucket<R> b : bucket.getChildren()) {
            count++;
            model.add(bucketResource, subset, createResource(bucketResource.toString(), count));
            serializeBucket(b, bucketResource, count, false);
        }
    }


    private Resource createResource(int level) {
        return ValueFactoryImpl.getInstance().createURI(histogramNamespace, level + "");
    }


    private Resource createResource(String nameSpaceAndLevel, int num) {
        return ValueFactoryImpl.getInstance().createURI(nameSpaceAndLevel + "_" + num);
    }

    private URI createURI(String nameSpace, String localName) {
        return ValueFactoryImpl.getInstance().createURI(nameSpace, localName);
    }


    private Literal createLiteral(Long l) {
        return ValueFactoryImpl.getInstance().createLiteral(l);
    }

}

