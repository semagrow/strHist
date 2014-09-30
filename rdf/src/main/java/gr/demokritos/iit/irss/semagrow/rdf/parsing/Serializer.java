package gr.demokritos.iit.irss.semagrow.rdf.parsing;

import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesBucket;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
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
public class Serializer {

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
                      property = createURI(VOID.NAMESPACE, "property"),
                      uriRegexPattern = createURI(VOID.NAMESPACE, "uriRegexPattern"),
                      title = createURI("http://purl.org/dc/terms/title");


    public Serializer(String format, String outputPath) {

        level = 0;
        this.outputPath = outputPath + "histVoID.ttl";
        this.format = Rio.getWriterFormatForMIMEType(format);
        model = new TreeModel();
    }


    public void serialize(STHolesHistogram<RDFRectangle> histogram) {

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
     * Serialize bucket to VoID.
     * @param bucket Histogram's bucket to be serialized.
     * @param bucketResource Subject of the Statement, depicts the lvl of the bucket.
     */
    private void serializeBucket(STHolesBucket<RDFRectangle> bucket, Resource bucketResource, int num, boolean isRoot) {

        if (!isRoot)
            bucketResource = createResource(bucketResource.toString(), num);

        model.add(bucketResource, RDF.TYPE, dataset);
        model.add(bucketResource, title, createLiteral(bucketResource.toString()));
        model.add(bucketResource, triples, createLiteral(bucket.getStatistics().getFrequency()));
        model.add(bucketResource, distinctSubjects, createLiteral(bucket.getStatistics().getDistinctCount().get(0)));
        model.add(bucketResource, properties, createLiteral(bucket.getStatistics().getDistinctCount().get(1)));
        model.add(bucketResource, distinctObjects, createLiteral(bucket.getStatistics().getDistinctCount().get(2)));

        // Subject's Range
        for (String s : bucket.getBox().getSubjectRange().getPrefixList())
            model.add(bucketResource, uriRegexPattern, createLiteral(s));
        // Predicate's Ranges
        for (String s : bucket.getBox().getPredicateRange().getItems())
            model.add(bucketResource, property, createURI(s));

        //TODO: Object's Ranges

        // Declare each child as subset and serialize recursively each bucket.
        int count = 0;
        for (STHolesBucket<RDFRectangle> b : bucket.getChildren()) {
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

    private URI createURI(String s) {
        return ValueFactoryImpl.getInstance().createURI(s);
    }

    private Literal createLiteral(Long l) {
        return ValueFactoryImpl.getInstance().createLiteral(l);
    }

    private Literal createLiteral(String s) {
        return ValueFactoryImpl.getInstance().createLiteral(s);
    }

}

