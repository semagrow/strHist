package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.api.STHistogram;
import gr.demokritos.iit.irss.semagrow.rdf.RDFSTHolesHistogram;
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

        h = new RDFSTHolesHistogram();

        while( true ) {
        	// open query log file
        	
        	// filter per  
        	// TODO: construct workload
//        	 h.refine( workload );
        }
    }

}
