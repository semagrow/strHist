package gr.demokritos.iit.irss.semagrow.rdf;

import java.util.List;

import gr.demokritos.iit.irss.semagrow.api.STHistogram;
import gr.demokritos.iit.irss.semagrow.rdf.parsing.json_format.HistogramIO;
import gr.demokritos.iit.irss.semagrow.rdf.parsing.LogParser;
import gr.demokritos.iit.irss.semagrow.rdf.qfr.RDFQueryRecord;
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
        long start = System.currentTimeMillis();
//        while( true ) {
        	// open query log file
        	LogParser lp = new LogParser("src\\main\\resources\\semagrow_logs_4.log");
//        	LogParser lp = new LogParser("src\\main\\resources\\test_2.txt");
//            LogParser lp = new LogParser("src\\main\\resources\\master_log.log");

        	List<RDFQueryRecord> list = lp.parse();

        	/*
    		 * Report
    		 */
        	for (RDFQueryRecord qr : list) {
        		System.out.println(qr.getQuery());
        	}        	
    		long end = System.currentTimeMillis();
    		System.out.println("\n\n********************************");
    		System.out.println("Total Parse Time: " + (double) (end - start) / 1000
    				+ " sec.");
    		System.out.println("Total Distinct Log Queries Parsed: "
    				+ lp.getCollection().size());
    		System.out.println("********************************");
        	
        	
        	// filter per  
        	// TODO: construct workload
        	 h.refine(list);
        	 
        	 // Write histogram to file.
        	 HistogramIO histIO = new HistogramIO("src\\main\\resources\\hist", ((STHolesHistogram) h));
        	 histIO.write();




//        }
    }

}
