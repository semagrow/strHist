package gr.demokritos.iit.irss.semagrow.numericalqfr;

import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;

import java.io.BufferedReader;
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
//            int counter = 1;

            while ((line = br.readLine()) != null) {

                line = line.replace("<", "").replace(">", "").trim();
                array.add(line);

//                // TODO: Comment it after debug. Counter too.
//                if (counter++ == 2970959 / 5)
//                    break;
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

//                // TODO: Comment it after debug.
//                if (counter == 2970959 / 5)
//                    break;
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }// loadIndex


    public IntervalRange getMapping(String string, boolean isPrefix) {
        return isPrefix ? getPrefixRange(string) : getSubjectRow(string);
    }


    private IntervalRange getPrefixRange(String prefix)  {

        int startRange = -1, endRange = -1;

        for (int i=0; i<array.size(); i++) {

            if (array.get(i).startsWith(prefix) && startRange == -1)
                startRange = i + 1;

            if (!array.get(i).startsWith(prefix) && startRange != -1 && endRange == -1) {
                endRange = i;
                break;
             }
        }

        return new IntervalRange(startRange, endRange);
    }// getPrefixRange


    private IntervalRange getSubjectRow(String subject) {

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
