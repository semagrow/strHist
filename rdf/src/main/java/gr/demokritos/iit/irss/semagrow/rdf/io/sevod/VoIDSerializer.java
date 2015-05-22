package gr.demokritos.iit.irss.semagrow.rdf.io.sevod;

import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.base.range.CalendarRange;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import gr.demokritos.iit.irss.semagrow.base.range.PrefixRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.RDFURIRange;
import gr.demokritos.iit.irss.semagrow.rdf.io.json.JSONDeserializer;
import gr.demokritos.iit.irss.semagrow.rdf.io.vocab.POWDERS;
import gr.demokritos.iit.irss.semagrow.rdf.io.vocab.SEVOD;
import gr.demokritos.iit.irss.semagrow.rdf.io.vocab.VOID;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesBucket;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import org.openrdf.model.*;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.DCTERMS;
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
public class VoIDSerializer {

    static ValueFactory vf = ValueFactoryImpl.getInstance();

    static class ELEON {
        static final String NAMESPACE = "http://eleon.iit.demokritos.gr/user#";

    }

    private static int rangeCounter;
    private static final String RANGE = "range";

    private int level;
    private Model model;

    public static String histogramNamespace = "http://www.semagrow.eu/metadata/histogram/",
                   eleonUser = "http://eleon.iit.demokritos.gr/user#irss2014hist";

    private String outputPath;

    private RDFFormat format;

    private Map<String,String> namespaceTable;


    public VoIDSerializer(String format, String outputPath) {

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
        model.setNamespace(DCTERMS.PREFIX, DCTERMS.NAMESPACE);
        model.setNamespace(VOID.PREFIX, VOID.NAMESPACE);
        model.setNamespace(SEVOD.PREFIX, SEVOD.NAMESPACE);
        model.setNamespace(XMLSchema.PREFIX, XMLSchema.NAMESPACE);
        model.setNamespace(POWDERS.PREFIX, POWDERS.NAMESPACE);
        model.setNamespace("agris", "http://agris.fao.org/aos/records/");
        model.setNamespace("", histogramNamespace);


        for (Namespace ns : model.getNamespaces())
            namespaceTable.put(ns.getName(), ns.getPrefix());
    }


    public void serialize(STHolesHistogram<RDFRectangle> histogram) throws RDFHandlerException {

        // Eleon load fix: Add a root node with name 'datasetTop'
        Resource bucketResource = SEVOD.ROOT;
        model.add(bucketResource, RDF.TYPE, VOID.DATASET);
        model.add(bucketResource, DCTERMS.TITLE, createLiteral(bucketResource.toString()));
        model.add(bucketResource, VOID.SUBSET, createResource(1));
        model.add(bucketResource, DCTERMS.CREATOR, createLiteral("nick"));
        eleon(bucketResource);

        serializeBucket(histogram.getRoot(), createResource(++level), 0, true);
        writeToFile(outputPath);
    }


    private void eleon(Resource bucketResource) {
        model.add(bucketResource, SEVOD.FACET, SEVOD.FACET_PROPERTY);
        model.add(bucketResource, POWDERS.ISSUEDBY, createURI(eleonUser));
    }


    private void writeToFile(String outputPath) throws RDFHandlerException {

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outputPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        Rio.write(model, out, format);

    }


    /**
     * Serialize bucket to VoID.
     * @param bucket Histogram's bucket to be serialized.
     * @param bucketResource Subject of the Statement, depicts the lvl of the bucket.
     */
    private void serializeBucket(STHolesBucket<RDFRectangle> bucket, Resource bucketResource, int num, boolean isRoot) {

        if (!isRoot)
            bucketResource = createResource(bucketResource.toString(), num);

        model.add(bucketResource, RDF.TYPE, VOID.DATASET);
        model.add(bucketResource, VOID.TRIPLES, createLiteral(bucket.getStatistics().getFrequency()));
        model.add(bucketResource, VOID.DISTINCTSUBJECTS, createLiteral(bucket.getStatistics().getDistinctCount().get(0)));
        model.add(bucketResource, VOID.PROPERTIES, createLiteral(bucket.getStatistics().getDistinctCount().get(1)));
        model.add(bucketResource, VOID.DISTINCTOBJECTS, createLiteral(bucket.getStatistics().getDistinctCount().get(2)));
        eleon(bucketResource);

        // -- Title handling
        String subjectStr = "";
        if (bucket.getBox().getSubjectRange().isInfinite())
            subjectStr = "?s";
        else {
            // Subject Range
            for (String s : bucket.getBox().getSubjectRange().getPrefixList()) {
                model.add(bucketResource, VOID.URIREGEXPATTERN, createLiteral(s));
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
            for (URI s : bucket.getBox().getPredicateRange().getItems()) {
                model.add(bucketResource, VOID.PROPERTY, s);
                try {
                    predicateStr = getStrFromUri(s) + "  ";
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String objectStr = "";
        if (bucket.getBox().getObjectRange().isEmpty())
            objectStr = "?o";
        else {
            for (Map.Entry<URI, RangeLength<?>> entry : bucket.getBox().getObjectRange().getLiteralRange().getRanges().entrySet()) {
                URI key = entry.getKey();

                if (key.equals(XMLSchema.INTEGER) || key.equals(XMLSchema.INT) || entry.getKey().equals(XMLSchema.LONG)) {

                    objectStr += ((IntervalRange)entry.getValue()).toString();
                } else if (key.equals(XMLSchema.DATETIME)) {
                    CalendarRange cr = (CalendarRange)entry.getValue();

                    objectStr += " [" + cr.getBegin() + "-" + cr.getEnd() + "] ";
                } else if (key.equals(XMLSchema.STRING)) {

                    PrefixRange pr = null;
                    Object obj = entry.getValue();

                    if (obj instanceof RDFURIRange) {
                        RDFURIRange rdfuriRange = (RDFURIRange)obj;
                        pr = new PrefixRange(rdfuriRange.getPrefixList());
                    } else if (obj instanceof PrefixRange) {
                        pr = (PrefixRange)obj;
                    }

                    // TODO: Change, in histogram we consider each PrefixRange as always a URL
                    for (String s : pr.getPrefixList())
                        objectStr += " <" + s + "> ";
                }
            }
        }

        // Title is the subject-predicate-object
        model.add(bucketResource, DCTERMS.TITLE, createLiteral(subjectStr + predicateStr + objectStr));
        // -- End of title handling

        addRangesToModel(bucketResource, bucket.getBox().getObjectRange().getLiteralRange().getRanges());

        for (String prefix : bucket.getBox().getObjectRange().getUriRange().getPrefixList()) {
            model.add(bucketResource, SEVOD.OBJECTREGEXPATTERN, vf.createLiteral(prefix));
        }


        // Declare each child as subset and serialize recursively each bucket.
        int count = 0;
        for (STHolesBucket<RDFRectangle> b : bucket.getChildren()) {
            count++;
            model.add(bucketResource, VOID.SUBSET, createResource(bucketResource.toString(), count));
            serializeBucket(b, bucketResource, count, false);
        }
    }


    private void addRangesToModel(Resource bucketResource, Map<URI, RangeLength<?>> rangeMap) {

        for (Map.Entry<URI, RangeLength<?>> entry : rangeMap.entrySet()) {
            URI key = entry.getKey();

            if (key.equals(XMLSchema.INTEGER) || key.equals(XMLSchema.INT) || entry.getKey().equals(XMLSchema.LONG)) {

                URI rangeURI = createURI(histogramNamespace, RANGE + "_" + rangeCounter++);
                IntervalRange ir = ((IntervalRange)entry.getValue());
                model.add(bucketResource, SEVOD.INTINTERVAL, rangeURI);
                model.add(rangeURI, SEVOD.FROM, createLiteral(ir.getLow()));
                model.add(rangeURI, SEVOD.TO, createLiteral(ir.getHigh()));
            } else if (key.equals(XMLSchema.DATETIME)) {

                URI rangeURI = createURI(histogramNamespace, RANGE + "_" + rangeCounter++);
                CalendarRange cr = (CalendarRange)entry.getValue();
                model.add(bucketResource, SEVOD.DATEINTERVAL, rangeURI);
                model.add(rangeURI, SEVOD.FROM, createLiteral(cr.getBegin()));
                model.add(rangeURI, SEVOD.TO, createLiteral(cr.getEnd()));
            } else if (key.equals(XMLSchema.STRING)) {
                PrefixRange pr = null;
                Object obj = entry.getValue();

                if (obj instanceof RDFURIRange) {
                    RDFURIRange rdfuriRange = (RDFURIRange)obj;
                    pr = new PrefixRange(rdfuriRange.getPrefixList());
                } else if (obj instanceof PrefixRange) {
                    pr = (PrefixRange)obj;
                }

                for (String s : pr.getPrefixList())
                    model.add(bucketResource, SEVOD.STRINGOBJECTREGEXPATTERN, createLiteral(s));
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

    public static Resource createResource(String nameSpace) {
        return ValueFactoryImpl.getInstance().createURI(nameSpace);
    }

    public static URI createURI(String nameSpace, String localName) {
        return ValueFactoryImpl.getInstance().createURI(nameSpace, localName);
    }

    public static URI createURI(String s) {
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

/*
    public static void main(String[] args) {
        STHolesHistogram<RDFRectangle> histogram =
                new JSONDeserializer("/home/nickozoulis/git/sthist/rdf/src/main/resources/histJSON_1980.txt").
                        getHistogram();

        new VoIDSerializer("application/x-turtle", "/home/nickozoulis/git/sthist/rdf/src/main/resources/histVOID_1980.ttl").serialize(histogram);

        histogram =
                new VoIDeserializer("/home/nickozoulis/git/sthist/rdf/src/main/resources/histVOID_1980.ttl").
                        getHistogram();

        new VoIDSerializer("application/x-turtle", "/home/nickozoulis/git/sthist/rdf/src/main/resources/new_histVOID_1980.ttl").serialize(histogram);


    }
    */

}

