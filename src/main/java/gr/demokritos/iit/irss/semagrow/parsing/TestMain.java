package gr.demokritos.iit.irss.semagrow.parsing;

import gr.demokritos.iit.irss.semagrow.api.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.api.PrefixRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFLiteralRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.Stat;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesBucket;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;

import java.util.ArrayList;
import java.util.HashSet;


public class TestMain {

	public static void main(String[] args) {
//		//Create RDFRectangles
//        //Common subject prefix
//        ArrayList<String> subjectPrefixList = new ArrayList<String>();
//        subjectPrefixList.add("http://a");
//        PrefixRange subjectRange = new PrefixRange(subjectPrefixList);
//        //b
//        HashSet<String> s1 = new HashSet<String>();
//        s1.add("a");
//        s1.add("b");
//        s1.add("c");
//        s1.add("d");
//        ExplicitSetRange<String> bPredicateRange = new ExplicitSetRange<String>(s1);
//        RDFLiteralRange bObjectRange = new RDFLiteralRange(0,20);
//        RDFRectangle bBox = new RDFRectangle(subjectRange, bPredicateRange, bObjectRange);
//        //b1
//        HashSet<String> s2 = new HashSet<String>();
//        s2.add("c");
//        ExplicitSetRange<String> b1PredicateRange = new ExplicitSetRange<String>(s2);
//        RDFLiteralRange b1ObjectRange = new RDFLiteralRange(4,6);
//        RDFRectangle b1Box = new RDFRectangle(subjectRange, b1PredicateRange, b1ObjectRange);
//        //b2
//        HashSet<String> s3 = new HashSet<String>();
//        s3.add("d");
//        s3.add("e");
//        ExplicitSetRange<String> b2PredicateRange = new ExplicitSetRange<String>(s3);
//        RDFLiteralRange b2ObjectRange = new RDFLiteralRange(15,20);
//        RDFRectangle b2Box = new RDFRectangle(subjectRange, b2PredicateRange, b2ObjectRange);
//        //b3
//        HashSet<String> s4 = new HashSet<String>();
//        s4.add("b");
//        ExplicitSetRange<String> b3PredicateRange = new ExplicitSetRange<String>(s4);
//        RDFLiteralRange b3ObjectRange = new RDFLiteralRange(15,16);
//        RDFRectangle b3Box = new RDFRectangle(subjectRange, b3PredicateRange, b3ObjectRange);
//
//        //b4
//        HashSet<String> s5 = new HashSet<String>();
//        s5.add("b");
//        ExplicitSetRange<String> b4PredicateRange = new ExplicitSetRange<String>(s5);
//        RDFLiteralRange b4ObjectRange = new RDFLiteralRange(2,4);
//        RDFRectangle b4Box = new RDFRectangle(subjectRange, b4PredicateRange, b4ObjectRange);
//
//        //b5
//        HashSet<String> s6 = new HashSet<String>();
//        s6.add("d");
//        ExplicitSetRange<String> b5PredicateRange = new ExplicitSetRange<String>(s6);
//        RDFLiteralRange b5ObjectRange = new RDFLiteralRange(15, 16);
//        RDFRectangle b5Box = new RDFRectangle(subjectRange, b5PredicateRange, b5ObjectRange);
//
//        //Create Statistics
//        ArrayList<Long> bDistinct = new ArrayList<Long>();
//        bDistinct.add( (long) 5);
//        bDistinct.add( (long) 6);
//        bDistinct.add( (long) 7);
//        long bFreq = 100;
//        Stat bStat = new Stat(bFreq, bDistinct);
//
//        ArrayList<Long> b1Distinct = new ArrayList<Long>();
//        b1Distinct.add( (long) 4);
//        b1Distinct.add( (long) 5);
//        b1Distinct.add( (long) 7);
//        long b1Freq = 25;
//        Stat b1Stat = new Stat(b1Freq, b1Distinct);
//
//        ArrayList<Long> b2Distinct = new ArrayList<Long>();
//        b2Distinct.add( (long) 5);
//        b2Distinct.add( (long) 3);
//        b2Distinct.add( (long) 4);
//        long b2Freq = 30;
//        Stat b2Stat = new Stat(b2Freq, b2Distinct);
//
//        ArrayList<Long> b3Distinct = new ArrayList<Long>();
//        b3Distinct.add( (long) 2);
//        b3Distinct.add( (long) 2);
//        b3Distinct.add( (long) 3);
//        long b3Freq = 10;
//        Stat b3Stat = new Stat(b3Freq, b3Distinct);
//
//        ArrayList<Long> b4Distinct = new ArrayList<Long>();
//        b4Distinct.add( (long) 1);
//        b4Distinct.add( (long) 1);
//        b4Distinct.add( (long) 1);
//        long b4Freq = 5;
//        Stat b4Stat = new Stat(b4Freq, b4Distinct);
//
//        ArrayList<Long> b5Distinct = new ArrayList<Long>();
//        b5Distinct.add( (long) 1);
//        b5Distinct.add( (long) 1);
//        b5Distinct.add( (long) 1);
//        long b5Freq = 3;
//        Stat b5Stat = new Stat(b5Freq, b5Distinct);
//
//        //Create buckets
//        STHolesBucket<RDFRectangle> b = new STHolesBucket<RDFRectangle>(bBox, bStat);
//        STHolesBucket<RDFRectangle> b1 = new STHolesBucket<RDFRectangle>(b1Box, b1Stat);
//        STHolesBucket<RDFRectangle> b2 = new STHolesBucket<RDFRectangle>(b2Box, b2Stat);
//        STHolesBucket<RDFRectangle> b3 = new STHolesBucket<RDFRectangle>(b3Box, b3Stat);
//        //not a candidate
//        STHolesBucket<RDFRectangle> b4 = new STHolesBucket<RDFRectangle>(b4Box, b4Stat);
//        STHolesBucket<RDFRectangle> b5 = new STHolesBucket<RDFRectangle>(b5Box, b5Stat);
//        b.addChild(b1);
//        b.addChild(b2);
//        b.addChild(b3);
//        b.addChild(b4);
//        b2.addChild(b5);
//
//
//        //Create RDFRectangle of Query Record
//        HashSet<String> sq = new HashSet<String>();
//        sq.add("b");
//        sq.add("c");
//        sq.add("d");
//        ExplicitSetRange<String> qPredicateRange = new ExplicitSetRange<String>(sq);
//        RDFLiteralRange qObjectRange = new RDFLiteralRange(5,18);
//        RDFRectangle qRect = new RDFRectangle(subjectRange, qPredicateRange, qObjectRange);
//
        
        
        
        
        
//        new HistogramIO("src\\main\\resources\\hist", b).write();


        // Read histogram from file.
        STHolesBucket rootBucket = HistogramIO.read("src\\main\\resources\\hist.txt");
        System.out.println(rootBucket);

        STHolesHistogram histogram = new STHolesHistogram();
        histogram.setRoot(rootBucket);
        new HistogramIO("src\\main\\resources\\histTest", histogram).write();

               

	}

}
