package gr.demokritos.iit.irss.semagrow.tools;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by efi on 18/10/2014.
 */
public class AgrovocTermsGenerator {


        private String agrovocTermsPath;
        private int uniqueSubjectFileRows;


        private static Random rand = new Random();


        /**
         * Constructor
         * @param agrovocTermsPath filename where terms are saved
         */
        public AgrovocTermsGenerator(String agrovocTermsPath) {

            this.agrovocTermsPath = agrovocTermsPath;


           // Count total lines of unique subject file.
            uniqueSubjectFileRows = countLineNumber(agrovocTermsPath);
            System.out.println("Total File Rows: " + uniqueSubjectFileRows);
        }// Constructor





        private void loadFile(File file) {
            System.out.println("Loading " + file.getName());

            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line = "";
                String splits[];

                while ((line = br.readLine()) != null) {
                    splits = line.split(" ");

                    /*filteredData.add(new RDFTriple(cleanString(splits[0]),
                            cleanString(splits[1]),
                            cleanString(splits[2])));
                    */
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }// loadFile


        /**
         * Return n randomly chosen agrovoc terms
         */
        private ArrayList<String> generateAgrovocTerms(int n) throws IOException {

            String subject = "";
            int randomRowNumber = 1;
            ArrayList<Integer> rows = new ArrayList<Integer>();
            ArrayList<String> agrovocTerms = new ArrayList<String>();

            for (int i = 0; i < n; i++) {

                // Just choose a random subject from all agrovoc terms
                randomRowNumber = randInt(1, uniqueSubjectFileRows);
                rows.add(randomRowNumber);
            }

            agrovocTerms = getSpecificSubjects(rows);

            return agrovocTerms;

        }// generateAgrovocTerms


        private String cleanString(String string) {

            string = string.replace("<", "");
            string = string.replace(">", "");

            return string.trim();
        }// cleanString


        private ArrayList<String> getSpecificSubjects(ArrayList<Integer> rows) throws IOException {

            BufferedReader br = new BufferedReader(new FileReader(agrovocTermsPath));
            String line;
            int counter;
            int subjectsFound = 0;
            ArrayList<String> subjects = new ArrayList<>();

            for (int i = 0 ; i< rows.size(); i++) {

                counter =0;

                while ((line = br.readLine()) != null) {
                    counter++;

                    if (rows.contains(counter)) {

                        line = cleanString(line);
                        //System.out.println("Get Subject " + line);
                        subjects.add(line);
                        subjectsFound ++;

                        if (subjectsFound == rows.size()){
                            break;
                        }

                     }
                }
            }

            br.close();

            return subjects;
        }// getSpecificSubjects(ArrayList<Integer>)


        /**
         * Returns a pseudo-random number between min and max, inclusive.
         * The difference between min and max can be at most
         * <code>Integer.MAX_VALUE - 1</code>.
         *
         * @param min Minimum value
         * @param max Maximum value.  Must be greater than min.
         * @return Integer between min and max, inclusive.
         * @see java.util.Random#nextInt(int)
         */
        public static int randInt(int min, int max) {


            // nextInt is normally exclusive of the top value,
            // so add 1 to make it inclusive
            int randomNum = AgrovocTermsGenerator.rand.nextInt((max - min) + 1) + min;

            return randomNum;
        }// randInt


        public static int countLineNumber(String path) {
            int lines = 0;
            try {

                File file = new File(path);
                LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
                lineNumberReader.skip(Long.MAX_VALUE);
                lines = lineNumberReader.getLineNumber();
                lineNumberReader.close();

            } catch (FileNotFoundException e) {
                System.out.println("FileNotFoundException Occurred"
                        + e.getMessage());
            } catch (IOException e) {
                System.out.println("IOException Occurred" + e.getMessage());
            }

            return lines;
        }// countLineNumber


        public static void main(String args[]) throws IOException {
            String dataFilename = "evaluation/src/test/resources/agrovoc_terms_full.txt";
            AgrovocTermsGenerator g = new AgrovocTermsGenerator(dataFilename);
            ArrayList<String> terms = new ArrayList<>();
            terms = g.generateAgrovocTerms(10);
            System.out.println("Finished");


        }

}
