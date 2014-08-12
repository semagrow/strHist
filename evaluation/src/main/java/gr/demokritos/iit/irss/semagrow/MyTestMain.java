package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.api.Histogram;
import gr.demokritos.iit.irss.semagrow.rdf.parsing.HistogramIO;
import gr.demokritos.iit.irss.semagrow.stholesOrig.STHolesOrigHistogram;
import gr.demokritos.iit.irss.semagrow.tools.NumericalMapper;

/**
 * Created by Nick on 12-Aug-14.
 */
public class MyTestMain {

    static String histogramPath = "C:\\Users\\Nick\\Desktop\\histogram.json.txt";

    public static void main(String[] args) {

//        STHolesOrigHistogram h = HistogramIO.readOrig(histogramPath);
//
//        System.out.println(h.getRoot());
//
//        new HistogramIO("C:\\Users\\Nick\\Desktop\\hist.txt", h).write();

        System.out.println(new NumericalMapper("C:/Users/Nick/Downloads/sorted/sorted")
                .getMapping("http://agris.fao.org/aos/records/CH2001000", true));

    }


}
