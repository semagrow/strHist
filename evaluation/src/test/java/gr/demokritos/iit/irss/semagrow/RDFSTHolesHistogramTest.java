package gr.demokritos.iit.irss.semagrow;


import gr.demokritos.iit.irss.semagrow.rdf.io.json.HistogramIO;
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

    public void testNum() throws URISyntaxException
    {


    	//java.net.URL u1 = this.getClass().getResource("/3670788-3676597");
    	//java.net.URL u2 = this.getClass().getResource("/US197");

        STHolesOrigHistogram h = HistogramIO.readOrig(Paths.get(getClass().
                    getResource("/369914-374641").toURI()).toString());
        NumQueryRecord r = readFromPool(Paths.get(getClass().
                getResource("/NL").toURI()).toString());
        h.refine(r);


    	/*
    	// Java 7-specific code:
        STHolesOrigHistogram h = HistogramIO.readOrig(Paths.get(u1).toString());
        NumQueryRecord r = readFromPool(Paths.get(u2).toString());
        h.refine(r);
        */
    	
    	/*
    	See http://wiki.eclipse.org/Eclipse/UNC_Paths 
    	about a possible caveat when using URI methods instead of Paths
    	Either way, these resources do not exist and the test fails
    	*/


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