package gr.demokritos.iit.irss.semagrow.tools;

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


    public ArrayList<Integer> getPrefixRange(String prefix) throws IOException {
        ArrayList<Integer> array = new ArrayList<Integer>(2);

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
        return array;
    }// getPrefixRange


    public int getSubjectRow(String subject) throws IOException {

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

        return -1;
    }// getSubjectRow


    public static void main(String[] args) throws IOException {
//        ArrayList<Integer> array = new NumericalMapper("C:\\Users\\Nick\\Downloads\\sorted\\sorted")
//                .getPrefixRange("http://agris.fao.org/aos/records/AG197500003");

        System.out.println("http://agris.fao.org/aos/records/AG19750000373");
    }
}
