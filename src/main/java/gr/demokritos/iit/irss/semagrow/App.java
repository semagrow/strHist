package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.api.STHistogram;
import gr.demokritos.iit.irss.semagrow.parsing.HistogramIO;
import gr.demokritos.iit.irss.semagrow.parsing.LogParser;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;

/**
 * Hello world!
 *
 */
public class App 
{

    private static STHistogram h;

    public static void main( String[] args )
    {

        h = new STHolesHistogram();

//        while( true ) {
        	// open query log file
//        	LogParser lp = new LogParser("src\\main\\resources\\semagrow_logs_3.log"); 
        	LogParser lp = new LogParser("src\\main\\resources\\test_2.txt");

        	
        	// filter per  
        	// TODO: construct workload
        	 h.refine(lp.parse());
        	 
        	 // Write histogram to file.
        	 HistogramIO histIO = new HistogramIO("src\\main\\resources\\hist", ((STHolesHistogram) h).getRoot());
        	 histIO.write();
//        }
    }

}
