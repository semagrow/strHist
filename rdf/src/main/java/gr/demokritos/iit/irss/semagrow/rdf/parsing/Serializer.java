package gr.demokritos.iit.irss.semagrow.rdf.parsing;

import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.base.range.CalendarRange;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import gr.demokritos.iit.irss.semagrow.base.range.PrefixRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesBucket;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import org.openrdf.model.*;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.turtle.TurtleUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nickozoulis on 30/9/2014.
 */
public class Serializer {

    static class VOID {
        static final String NAMESPACE = "http://rdfs.org/ns/void#";
    }

    private static int rangeCounter;
    private static final String RANGE = "range";

    private int level;
    private Model model;
    private String histogramNamespace = "http://www.semagrow.eu/metadata/histogram/",
                   // Eleon centered variables
                   eleonRootNamespace = "http://rdf.iit.demokritos.gr/2013/sevod#datasetTop",
                   eleonFacet = "http://rdf.iit.demokritos.gr/2013/sevod#facet",
                   eleonFacetProperty = "http://rdf.iit.demokritos.gr/2013/sevod#propertyFacet",
                   eleonIssuedBy = "http://www.w3.org/2007/05/powder-s#issuedby",
                   eleonUser = "http://eleon.iit.demokritos.gr/user#irss2014hist";

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
                      title = createURI("http://purl.org/dc/terms/title"),
                      sevod = createURI("http://rdf.iit.demokritos.gr/2013/sevod#"),
                      integerInterval = createURI(sevod.toString(), "intInterval"),
                      dateInterval = createURI(sevod.toString(), "dateInterval"),
                      from = createURI(sevod.toString(), "from"),
                      to = createURI(sevod.toString(), "to"),
                      stringObjectRegexPattern = createURI(sevod.toString(), "stringObjectRegexPattern");


    private Map<String,String> namespaceTable;


    public Serializer(String format, String outputPath) {

        level = 0;
        this.outputPath = outputPath;
        this.format = Rio.getWriterFormatForMIMEType(format);
        model = new TreeModel();
        namespaceTable = new HashMap<String, String>();
        setModelNamespaces();
    }


    private void setModelNamespaces() {

        model.setNamespace(RDF.PREFIX, RDF.NAMESPACE);
        model.setNamespace(RDFS.PREFIX, RDFS.NAMESPACE);
        model.setNamespace("void", VOID.NAMESPACE);
        model.setNamespace("agris", "http://agris.fao.org/aos/records/");
        model.setNamespace("dc", "http://purl.org/dc/terms/");
        model.setNamespace("svd", "http://rdf.iit.demokritos.gr/2013/sevod#");
        model.setNamespace(XMLSchema.PREFIX, XMLSchema.NAMESPACE);
        model.setNamespace("", histogramNamespace);
        model.setNamespace("powder", "http://www.w3.org/2007/05/powder-s#");

        for (Namespace ns : model.getNamespaces())
            namespaceTable.put(ns.getName(), ns.getPrefix());
    }


    public void serialize(STHolesHistogram<RDFRectangle> histogram) {

        // Eleon load fix: Add a root node with name 'datasetTop'
        Resource bucketResource = createResource(eleonRootNamespace);
        model.add(bucketResource, RDF.TYPE, dataset);
        model.add(bucketResource, title, createLiteral(bucketResource.toString()));
        model.add(bucketResource, subset, createResource(1));
        model.add(bucketResource, createURI("http://purl.org/dc/terms/creator"), createLiteral("nick"));
        eleon(bucketResource);

        serializeBucket(histogram.getRoot(), createResource(++level), 0, true);
        writeToFile(outputPath);
    }


    private void eleon(Resource bucketResource) {
        model.add(bucketResource, createURI(eleonFacet), createURI(eleonFacetProperty));
        model.add(bucketResource, createURI(eleonIssuedBy), createURI(eleonUser));
    }


    private void writeToFile(String outputPath) {

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outputPath + "histRangesVoID.ttl");
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
        model.add(bucketResource, triples, createLiteral(bucket.getStatistics().getFrequency()));
        model.add(bucketResource, distinctSubjects, createLiteral(bucket.getStatistics().getDistinctCount().get(0)));
        model.add(bucketResource, properties, createLiteral(bucket.getStatistics().getDistinctCount().get(1)));
        model.add(bucketResource, distinctObjects, createLiteral(bucket.getStatistics().getDistinctCount().get(2)));
        eleon(bucketResource);

        // -- Title handling
        String subjectStr = "";
        if (bucket.getBox().getSubjectRange().getPrefixList().isEmpty())
            subjectStr = "?s";
        else {
            // Subject Range
            for (String s : bucket.getBox().getSubjectRange().getPrefixList()) {
                model.add(bucketResource, uriRegexPattern, createLiteral(s));
                try {
                    subjectStr += getStrFromUri(createURI(s)) + "  ";
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String predicateStr = "";
        if (bucket.getBox().getPredicateRange().getItems().isEmpty())
            predicateStr = "?p";
        else {
            // Predicate Ranges
            for (String s : bucket.getBox().getPredicateRange().getItems()) {
                model.add(bucketResource, property, createURI(s));
                try {
                    predicateStr = getStrFromUri(createURI(s)) + "  ";
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String objectStr = "";
        if (bucket.getBox().getObjectRange().isEmpty())
            objectStr = "?o";
        else {
            for (Map.Entry<URI, RangeLength<?>> entry : bucket.getBox().getObjectRange().getRanges().entrySet()) {
                URI key = entry.getKey();

                if (key.equals(XMLSchema.INTEGER) || entry.getKey().equals(XMLSchema.LONG)) {

                    objectStr += ((IntervalRange)entry.getValue()).toString();
                } else if (key.equals(XMLSchema.DATETIME)) {
                    CalendarRange cr = (CalendarRange)entry.getValue();

                    objectStr += " [" + cr.getBegin() + "-" + cr.getEnd() + "] ";
                } else if (key.equals(XMLSchema.STRING)) {

                    PrefixRange pr = (PrefixRange)entry.getValue();
                    // TODO: Change, in histogram we consider each PrefixRange as always a URL
                    for (String s : pr.getPrefixList())
                        objectStr += " <" + s + "> ";
                }
            }
        }

        // Title is the subject-predicate-object
        model.add(bucketResource, title, createLiteral(subjectStr + predicateStr + objectStr));
        // -- End of title handling

        addRangesToModel(bucketResource, bucket.getBox().getObjectRange().getRanges());

        // Declare each child as subset and serialize recursively each bucket.
        int count = 0;
        for (STHolesBucket<RDFRectangle> b : bucket.getChildren()) {
            count++;
            model.add(bucketResource, subset, createResource(bucketResource.toString(), count));
            serializeBucket(b, bucketResource, count, false);
        }
    }


    private void addRangesToModel(Resource bucketResource, Map<URI, RangeLength<?>> rangeMap) {

        for (Map.Entry<URI, RangeLength<?>> entry : rangeMap.entrySet()) {
            URI key = entry.getKey();

            if (key.equals(XMLSchema.INTEGER) || entry.getKey().equals(XMLSchema.LONG)) {

                URI rangeURI = createURI(histogramNamespace, RANGE + "_" + rangeCounter++);
                IntervalRange ir = ((IntervalRange)entry.getValue());
                model.add(bucketResource, integerInterval, rangeURI);
                model.add(rangeURI, from, createLiteral(ir.getLow()));
                model.add(rangeURI, to, createLiteral(ir.getHigh()));
            } else if (key.equals(XMLSchema.DATETIME)) {

                URI rangeURI = createURI(histogramNamespace, RANGE + "_" + rangeCounter++);
                CalendarRange cr = (CalendarRange)entry.getValue();
                model.add(bucketResource, dateInterval, rangeURI);
                model.add(rangeURI, from, createLiteral(cr.getBegin()));
                model.add(rangeURI, to, createLiteral(cr.getEnd()));
            } else if (key.equals(XMLSchema.STRING)) {

                PrefixRange pr = (PrefixRange)entry.getValue();
                for (String s : pr.getPrefixList())
                    model.add(bucketResource, stringObjectRegexPattern, createLiteral(s));
            }
        }
    }


    private String getStrFromUri(URI uri) throws IOException {
        String str = "";
        String uriString = uri.toString();

        // Try to find a prefix for the URI's namespace
        String prefix = null;

        int splitIdx = TurtleUtil.findURISplitIndex(uriString);
        if (splitIdx > 0) {
            String namespace = uriString.substring(0, splitIdx);
            prefix = namespaceTable.get(namespace);
        }

        if (prefix != null) {
            // Namespace is mapped to a prefix; write abbreviated URI
            str += prefix;
            str += ":";
            str += uriString.substring(splitIdx);
        }
        else {
            // Write full URI
            str += "<";
            str += TurtleUtil.encodeURIString(uriString);
            str += ">";
        }

        return str;
    }


    private String getLastPrefix(String s) {
        String[] splits = s.split("/");
        return splits[splits.length - 1];
    }


    private Resource createResource(int level) {
        return ValueFactoryImpl.getInstance().createURI(histogramNamespace, level + "");
    }


    private Resource createResource(String nameSpaceAndLevel, int num) {
        return ValueFactoryImpl.getInstance().createURI(nameSpaceAndLevel + "_" + num);
    }

    private Resource createResource(String nameSpace) {
        return ValueFactoryImpl.getInstance().createURI(nameSpace);
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

    private Literal createLiteral(int i) {
        return ValueFactoryImpl.getInstance().createLiteral(i);
    }

    private Literal createLiteral(Date d) {
        return ValueFactoryImpl.getInstance().createLiteral(d);
    }

}

