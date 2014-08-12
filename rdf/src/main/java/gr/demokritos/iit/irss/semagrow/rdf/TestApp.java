package gr.demokritos.iit.irss.semagrow.rdf;

import gr.demokritos.iit.irss.semagrow.api.STHistogram;
import gr.demokritos.iit.irss.semagrow.base.Stat;
import gr.demokritos.iit.irss.semagrow.base.range.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.base.range.PrefixRange;
import gr.demokritos.iit.irss.semagrow.stholes.MergeInfo;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesBucket;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import org.openrdf.model.vocabulary.RDF;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by efi on 8/8/2014.
 */
public class TestApp {
    private static STHistogram h;

    public static void main( String[] args ) {
        STHolesHistogram<RDFRectangle> myH = new STHolesHistogram();

        //Test 1: Estimation with one enclosing bucket and two dimensions
        //with unit length
        //Create Root
        ArrayList<String> rootPrefixList = new ArrayList<String>();
        rootPrefixList.add("http://");
        PrefixRange subjectRangeR = new PrefixRange(rootPrefixList);
        HashSet<String> rootPredicates = new HashSet<String>();
        rootPredicates.add("a");
        ExplicitSetRange<String> predicateRangeR= new ExplicitSetRange<String>(rootPredicates);
        RDFLiteralRange objectRangeR = new RDFLiteralRange(0,10);
        RDFRectangle boxRoot = new RDFRectangle(subjectRangeR, predicateRangeR, objectRangeR);
        Long frequencyR = (long)110;
        List<Long> distinctR = new ArrayList<Long>();
        distinctR.add((long)10);
        distinctR.add((long)1);
        distinctR.add((long)5);
        Stat statR = new Stat(frequencyR, distinctR);
        STHolesBucket<RDFRectangle> root = new STHolesBucket<RDFRectangle>(boxRoot, statR, null, null);
        //Create a child
        ArrayList<String> b1PrefixList = new ArrayList<String>();
        b1PrefixList.add("http://a");
        PrefixRange subjectRangeB1 = new PrefixRange(b1PrefixList);
        HashSet<String> b1Predicates = new HashSet<String>();
        b1Predicates.add("a");
        ExplicitSetRange<String> predicateRangeB1= new ExplicitSetRange<String>(b1Predicates);
        RDFLiteralRange objectRangeB1 = new RDFLiteralRange(1,5);
        RDFRectangle boxB1 = new RDFRectangle(subjectRangeB1, predicateRangeB1, objectRangeB1);
        Long frequencyB1 = (long)50;
        List<Long> distinctB1 = new ArrayList<Long>();
        distinctB1.add((long)5);
        distinctB1.add((long)1);
        distinctB1.add((long)2);
        Stat statB1 = new Stat(frequencyB1, distinctB1);
        STHolesBucket<RDFRectangle> b1 = new STHolesBucket<RDFRectangle>(boxB1, statB1, null, null);
        root.addChild(b1);
        // Create b2, b1's child
        ArrayList<String> b2PrefixList = new ArrayList<String>();
        b2PrefixList.add("http://a/b");
        PrefixRange subjectRangeB2 = new PrefixRange(b2PrefixList);
        HashSet<String> b2Predicates = new HashSet<String>();
        b2Predicates.add("a");
        ExplicitSetRange<String> predicateRangeB2= new ExplicitSetRange<String>(b2Predicates);
        RDFLiteralRange objectRangeB2 = new RDFLiteralRange(2,3);
        RDFRectangle boxB2 = new RDFRectangle(subjectRangeB2, predicateRangeB2, objectRangeB2);
        Long frequencyB2 = (long)10;
        List<Long> distinctB2 = new ArrayList<Long>();
        distinctB2.add((long)2);
        distinctB2.add((long)1);
        distinctB2.add((long)2);
        Stat statB2 = new Stat(frequencyB2, distinctB2);
        STHolesBucket<RDFRectangle> b2 = new STHolesBucket<RDFRectangle>(boxB2, statB2, null, null);
        b1.addChild(b2);

        myH.setRoot(root);

        //Create query rectangle
        ArrayList<String> qPrefixList = new ArrayList<String>();
        qPrefixList.add("http://a/c");
        PrefixRange subjectRangeQ = new PrefixRange(qPrefixList);
        HashSet<String> qPredicates = new HashSet<String>();
        qPredicates.add("a");
        ExplicitSetRange<String> predicateRangeQ = new ExplicitSetRange<String>(qPredicates);
        RDFLiteralRange objectRangeQ = new RDFLiteralRange(2,3);
        RDFRectangle boxQ= new RDFRectangle(subjectRangeQ, predicateRangeQ, objectRangeQ);
        System.out.println("Expected estimation is: 10");
        System.out.println("Our estimation is: " + myH.estimate(boxQ));

        //Test 2: Estimation with one enclosing bucket and one dimension
        //with unit length (with one distinct predicate)

        //Create query rectangle
        qPrefixList.add("http://a/c/d");
        boxQ= new RDFRectangle(subjectRangeQ, predicateRangeQ, objectRangeQ);
        System.out.println("Expected estimation is: 50");
        System.out.println("Our estimation is: " + myH.estimate(boxQ));

        //Test 3: two enclosing buckets (one unconstrained variable) and two ranges
        //with unit length
        //Create root's second child
        ArrayList<String> b3PrefixList = new ArrayList<String>();
        b3PrefixList.add("http://a");
        PrefixRange subjectRangeB3 = new PrefixRange(b3PrefixList);
        HashSet<String> b3Predicates = new HashSet<String>();
        b3Predicates.add("a");
        ExplicitSetRange<String> predicateRangeB3= new ExplicitSetRange<String>(b3Predicates);
        RDFLiteralRange objectRangeB3 = new RDFLiteralRange(6,7);
        RDFRectangle boxB3 = new RDFRectangle(subjectRangeB3, predicateRangeB3, objectRangeB3);
        Long frequencyB3 = (long)10;
        List<Long> distinctB3 = new ArrayList<Long>();
        distinctB3.add((long)2);
        distinctB3.add((long)1);
        distinctB3.add((long)1);
        Stat statB3 = new Stat(frequencyB3, distinctB3);
        STHolesBucket<RDFRectangle> b3 = new STHolesBucket<RDFRectangle>(boxB3, statB3, null, null);
        root.addChild(b3);

        myH.setRoot(root);

        //Create query rectangle
        ArrayList<String> q2PrefixList = new ArrayList<String>();
        q2PrefixList.add("http://a/c");
        PrefixRange subjectRangeQ2 = new PrefixRange(q2PrefixList);
        HashSet<String> q2Predicates = new HashSet<String>();
        q2Predicates.add("a");
        ExplicitSetRange<String> predicateRangeQ2 = new ExplicitSetRange<String>(q2Predicates);
        RDFLiteralRange objectRangeQ2 = new RDFLiteralRange();
        RDFRectangle boxQ2= new RDFRectangle(subjectRangeQ2, predicateRangeQ2, objectRangeQ2);
        System.out.println("Expected estimation is: 15");
        System.out.println("Our estimation is: " + myH.estimate(boxQ2));

        /*
        //Test getPCMerge
        Map.Entry<STHolesBucket<RDFRectangle>, Long> pcMergePenalty =
        myH.getPCMergePenalty(root, b1);

        MergeInfo<RDFRectangle> curMerge =
                new MergeInfo<RDFRectangle>(root, b1, pcMergePenalty.getKey(),
                        pcMergePenalty.getValue());
        STHolesBucket<RDFRectangle> bp = curMerge.getB1();
        STHolesBucket<RDFRectangle> bc = curMerge.getB2();
        STHolesBucket<RDFRectangle> bn = curMerge.getBn();

        STHolesBucket.merge(bp, bc, bn);
        */

        //Create root's third child b4
        ArrayList<String> b4PrefixList = new ArrayList<String>();
        b4PrefixList.add("http://a/b");
        PrefixRange subjectRangeB4 = new PrefixRange(b4PrefixList);
        HashSet<String> b4Predicates = new HashSet<String>();
        b4Predicates.add("a");
        ExplicitSetRange<String> predicateRangeB4= new ExplicitSetRange<String>(b4Predicates);
        RDFLiteralRange objectRangeB4 = new RDFLiteralRange(8,9);
        RDFRectangle boxB4 = new RDFRectangle(subjectRangeB4, predicateRangeB4, objectRangeB4);
        Long frequencyB4 = (long)20;
        List<Long> distinctB4 = new ArrayList<Long>();
        distinctB4.add((long)6);
        distinctB4.add((long)1);
        distinctB4.add((long)3);
        Stat statB4 = new Stat(frequencyB4, distinctB4);
        STHolesBucket<RDFRectangle> b4 = new STHolesBucket<RDFRectangle>(boxB4, statB4, null, null);
        myH.getRoot().addChild(b4);

        MergeInfo<RDFRectangle> m = new MergeInfo<RDFRectangle>(b1,b2,b3,4);
        System.out.println(m.toString());

        /*
        //Test getSSMerge
        Map.Entry<STHolesBucket<RDFRectangle>, Long> ssMergePenalty =
                myH.getSSMergePenalty(b1, b4);

        MergeInfo<RDFRectangle> curMerge =
                new MergeInfo<RDFRectangle>(b1, b4, ssMergePenalty.getKey(),
                        ssMergePenalty.getValue());
        STHolesBucket<RDFRectangle> bs1 = curMerge.getB1();
        STHolesBucket<RDFRectangle> bs2 = curMerge.getB2();
        STHolesBucket<RDFRectangle> bn  = curMerge.getBn();

        STHolesBucket.merge(bs1, bs2, bn);
        */

        /*
        //Test findBestMerge
        root = myH.getRoot();
        MergeInfo<RDFRectangle> bestMerge =  myH.findBestMerge(root);
        */
    }
}
