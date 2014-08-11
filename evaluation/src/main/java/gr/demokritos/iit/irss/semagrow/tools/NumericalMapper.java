package gr.demokritos.iit.irss.semagrow.tools;

import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.base.NumRectangle;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.qfr.RDFQueryRecord;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Nick on 10-Aug-14.
 */
public class NumericalMapper {

    private HashMap<String, Integer> index;
    private ArrayList<String> array;


    /**
     * Instantiates the Collections for pool conversion.
     * @param sortedFilePath Path to the file containing the sorted subjects.
     */
    public NumericalMapper(String sortedFilePath) {

        loadIndex(sortedFilePath);
        loadArray(sortedFilePath);
    }// Constructor


    private void loadArray(String sortedFilePath) {
        array = new ArrayList<String>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(sortedFilePath));
            String line = "";
            int counter = 1;

            while ((line = br.readLine()) != null) {

                line = line.replace("<", "").replace(">", "").trim();
                array.add(line);


                // TODO: Remove after debug. Counter too.
                if (counter++ == 2970959 / 5)
                    break;
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }// loadArray


    private void loadIndex(String sortedFilePath) {
        index = new HashMap<String, Integer>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(sortedFilePath));
            String line = "";
            int counter = 1;

            while ((line = br.readLine()) != null) {

                line = line.replace("<", "").replace(">", "").trim();
                index.put(line, counter);

                counter++;

                // TODO: Remove after debug. Counter too.
                if (counter == 2970959 / 5)
                    break;
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }// loadIndex


    public IntervalRange<Integer> getMapping(String string, boolean isPrefix) {
        return isPrefix ? getPrefixRange(string) : getSubjectRow(string);
    }


    private IntervalRange<Integer> getPrefixRange(String prefix)  {

        int startRange = -1, endRange = -1;

        for (int i=1; i<array.size(); i++) {

            if (array.get(i).startsWith(prefix) && startRange == -1)
                startRange = i;

            if (!array.get(i).startsWith(prefix) && startRange != -1 && endRange == -1) {
                endRange = i - 1;
                break;
             }
        }

        return new IntervalRange<Integer>(startRange, endRange);
    }// getPrefixRange


    private IntervalRange<Integer> getSubjectRow(String subject) {

          int row = index.get(subject);
          return new IntervalRange(row, row);
    }// getSubjectRow


    public static void main(String[] args) {
//        System.out.println(new NumericalMapper("C:/Users/Nick/Downloads/sorted/sorted", false)
//                .getMapping("http://agris.fao.org/aos/records/AG197500003"));


//        System.out.println(new NumericalMapper("C:/Users/Nick/Downloads/sorted/sorted", true).
//                getSubjectRow("http://agris.fao.org/aos/records/IT19780298159"));
    }
}
