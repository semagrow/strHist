package gr.demokritos.iit.irss.semagrow.rdf;


import gr.demokritos.iit.irss.semagrow.rdf.parsing.HistogramIO;
import gr.demokritos.iit.irss.semagrow.rdf.qfr.RDFQueryRecord;
import junit.framework.TestCase;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class RDFSTHolesHistogramTest extends TestCase {


    public void testRefine() throws URISyntaxException {

        RDFSTHolesHistogram h = HistogramIO.read(Paths.get(getClass().getResource("/IT19").toURI()).toString().toString());

        RDFQueryRecord r = readFromPool(Paths.get(getClass().getResource("/IT199400").toURI()).toString().toString());

         h.refine(r);

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