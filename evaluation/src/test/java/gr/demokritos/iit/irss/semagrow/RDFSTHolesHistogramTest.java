package gr.demokritos.iit.irss.semagrow;


import gr.demokritos.iit.irss.semagrow.base.NumRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.RDFSTHolesHistogram;
import gr.demokritos.iit.irss.semagrow.rdf.parsing.HistogramIO;
import gr.demokritos.iit.irss.semagrow.rdf.qfr.RDFQueryRecord;
import gr.demokritos.iit.irss.semagrow.stholesOrig.STHolesOrigHistogram;
import junit.framework.TestCase;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class RDFSTHolesHistogramTest extends TestCase {


    public void testRefine() throws URISyntaxException {

        //RDFSTHolesHistogram h = HistogramIO.read(Paths.get(getClass().getResource("/DE19810").toURI()).toString());

        //RDFQueryRecord r = readFromPool(Paths.get(getClass().getResource("/SE9").toURI()).toString());

         //h.refine(r);

       // String filename = "src/main/resources/test_output";
       // outputHistogram(h, filename);

    }

    public void testNum() throws URISyntaxException {

        STHolesOrigHistogram h = HistogramIO.readOrig(Paths.get(getClass().getResource("/" +
                "1373867-1473515").toURI()).toString());
        NumQueryRecord r = readFromPool(Paths.get(getClass().getResource("/NL200").toURI()).toString());
        h.refine(r);

        String filename = "src/main/resources/test_output";
        outputHistogram(h, filename);

    //    NumQueryRecord e = readFromPool(Paths.get(getClass().getResource("/BR19830838758").toURI()).toString());
    //    h.estimate(e.getRectangle());
    }

    private static void outputHistogram(STHolesOrigHistogram h, String filename) {
        HistogramIO histIO = new HistogramIO(filename, h);
        histIO.write();
    }

    private static <T>  T readFromPool(String filename) {
        T rdfQueryRecord = null;
        File file = new File(filename);
        ObjectInputStream ois;

        try {

            ois = new ObjectInputStream(new FileInputStream(file));

            rdfQueryRecord = (T) ois.readObject();

            ois.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (EOFException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return  rdfQueryRecord;
    }// readFromPool

}