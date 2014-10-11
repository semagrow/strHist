package gr.demokritos.iit.irss.semagrow.rdf;

import gr.demokritos.iit.irss.semagrow.api.STHistogram;
import gr.demokritos.iit.irss.semagrow.rdf.io.json.JSONSerializer;
import gr.demokritos.iit.irss.semagrow.rdf.io.log.LogParser;
import gr.demokritos.iit.irss.semagrow.rdf.io.log.RDFQueryRecord;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;

import java.util.List;


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

        	LogParser lp = new LogParser("src\\main\\resources\\semagrow_logs_4.log");

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
            new JSONSerializer((STHolesHistogram) h, "/home/nickozoulis/git/sthist/rdf/src/main/resources/hist.txt");

    }

}
