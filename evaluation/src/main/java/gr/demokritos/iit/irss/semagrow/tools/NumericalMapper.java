package gr.demokritos.iit.irss.semagrow.tools;

import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.base.NumRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.qfr.RDFQueryRecord;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Nick on 10-Aug-14.
 */
public class NumericalMapper {

    private String sortedFilePath;

    public NumericalMapper(String sortedFilePath) {
        this.sortedFilePath = sortedFilePath;
    }


    public ArrayList<Integer> getPrefixRange(String prefix)  {
        ArrayList<Integer> array = new ArrayList<Integer>(2);

        try {
            BufferedReader br = new BufferedReader(new FileReader(sortedFilePath));
            String line = "";
            int counter = 1;
            int startRange = -1, endRange = -1;

            while ((line = br.readLine()) != null) {

                line = line.replace("<", "").replace(">", "").trim();

                if (line.startsWith(prefix) && startRange == -1)
                    startRange = counter;

                if (!line.startsWith(prefix) && startRange != -1 && endRange == -1) {
                    endRange = counter - 1;
                    break;
                }

                counter++;
            }// while

            array.add(startRange);
            array.add(endRange);

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return array;
    }// getPrefixRange


    public int getSubjectRow(String subject) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(sortedFilePath));
            String line = "";
            int counter = 1;

            while ((line = br.readLine()) != null) {

                line = line.replace("<", "").replace(">", "").trim();

                if (line.equals(subject))
                    return counter;

                counter++;
            }// while

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }// getSubjectRow


    public static void main(String[] args) throws IOException {
//        ArrayList<Integer> array = new NumericalMapper("C:/Users/Nick/Downloads/sorted/sorted")
//                .getPrefixRange("http://agris.fao.org/aos/records/AG197500003");


        System.out.println(new NumericalMapper("C:/Users/Nick/Downloads/sorted/sorted").
                getSubjectRow("http://agris.fao.org/aos/records/IT19780298159"));
    }
}
