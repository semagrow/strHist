package gr.demokritos.iit.irss.semagrow.parsing;

import gr.demokritos.iit.irss.semagrow.rdf.RDFQueryRecord;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import org.openrdf.model.vocabulary.RDF;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParser {

	private String path;
	
	private String logQuerySeparator = "[]", inQuerySeparator = "%%";
	/**
	 *  Holds the unique RDF Query Records, identified by their signature.
	 */
	private List<RDFQueryRecord> collection;
	

	public LogParser(String path) {
		setPath(path);
		collection = new ArrayList<RDFQueryRecord>();		
	}


	public List<RDFQueryRecord> parse() {

		String line = "", text = "";

		LogQuery lq = new LogQuery();

		try {

			BufferedReader br = new BufferedReader(new FileReader(
					new File(path)));

			while ((line = br.readLine()) != null) {
				text += line + "\n";

				// End of LogQuery.
				if (line.equals(logQuerySeparator)) {
					// Vectorize log query
					String[] split = text.split(inQuerySeparator);

					processOne(split[0], lq);

					processTwo(split[1], lq);

					processThree(split[2], lq);				

					// Reset the variables
					text = "";
					line = "";					
					processQuery(lq);
					lq = new LogQuery();
				} 

			} // while

			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return collection;
	}// parse


	private void processThree(String string, LogQuery lq) {

		// Create a regex pattern.
		String pattern = "(\\[?)(.*?)=(.*?)(;|])";
		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);
		// Now create matcher object.
		Matcher m = r.matcher(string);

		while (m.find()) 		
			lq.getQueryBindings().add(new Binding(m.group(2), m.group(3)));		
		
	}// processThree


	/**
	 * Extracts query variables and filters.
	 */
	private void processTwo(String string, LogQuery lq) {
		// Split input into lines array.
		String split[] = string.split("\\r?\\n");
		
		for (int i = 0; i < split.length; i++) {			
			if (split[i].contains("Filter")) {
				QueryFilter qf = null;
				
				if (split[i + 1].contains("Compare")) {
					String operator = Utilities.getCompareOperator(split[i + 1]);
					String variable = Utilities.getVariableName(split[i + 2]);					
					String valueConstant = Utilities.getValueConstant(split[i + 3]);
					
//					System.err.println("\nFilter");
//					System.out.println(operator +"\n" + variable + "\n" + valueConstant);
					
					qf = new QueryFilter("Compare", variable);
					if (operator.contains("<"))
						qf.setHigh(valueConstant);
					else if (operator.contains(">"))
						qf.setLow(valueConstant);
					
					// Skip processed lines.
					i = i + 3;
			
				} else if (split[i + 1].contains("Regex")) {
					String variable = Utilities.getVariableName(split[i + 3]);
					String regex = Utilities.getValueConstant(split[i + 4]);
					
//					System.err.println("\nFilter");
//					System.out.println(variable +"\n" + regex);
					
					qf = new QueryFilter("Regex", variable);
					qf.setRegex(regex);
					
					// Skip processed lines.
					i = i + 4;					
				}	
				
				// Add filter to query Object.
				lq.getQueryFilters().add(qf);
				
			} else if (split[i].contains("StatementPattern")) {				
				Binding b = null;
				
				for (int j=i+1; j<=i+3; j++) {
					b = extractStatementPatternVariable(split[j]);					
					if (b != null)
						lq.getQueryStatements().add(b);
					
//					System.err.println("\nStatementPattern");
//					System.out.println(b.getName() +" = "+ b.getValue());
				}				
			}			
		}// for	
	}// processTwo


	private Binding extractStatementPatternVariable(String string) {

		Binding spv = null;

		if (string.contains("name") && !string.contains("value")) {// Variable

			String pattern = "name=(.*?)\\)";
			
			// Create a Pattern object
			Pattern r = Pattern.compile(pattern);

			// Now create matcher object.
			Matcher m = r.matcher(string);

			if (m.find())
				spv = new Binding(m.group(1));
			
		} else if (string.contains("name") && string.contains("value")) {// Const

			// Extract Name.
			String pattern = "name=(.*?),", name = "", value = "";
			// Create a Pattern object
			Pattern r = Pattern.compile(pattern);
			// Now create matcher object.
			Matcher m = r.matcher(string);

			if (m.find())
				name = m.group(1).replace("-const-", "");

			// Extract Value.
			pattern = "value=(.*?)(,|\\))";
			// Create a Pattern object
			r = Pattern.compile(pattern);
			// Now create matcher object.
			m = r.matcher(string);

			if (m.find())
				value = m.group(1);

			spv = new Binding(name, value);
//			System.out.println(spv.getName() + " = " + spv.getValue());
		}// if
		


		return spv;
	}// extractStatementPatternVariable


	/**
	 * Extracts sessionId, startTime and sparqlEndpoint.
	 */
	private void processOne(String string, LogQuery lq) {
		String split[] = string.split("\\r?\\n");

		lq.setSessionId(split[0]);
		lq.setStartTime(Long.parseLong(split[1]));
		lq.setSparqlEndpoint(split[2]);
	}// processOne
	
	
	private void processQuery(LogQuery lq) {
		// Just a dump object.
		RDFQueryRecord qr = new RDFQueryRecord(lq);
		// Construct query's BindingSet.
		BindingSet bs = new BindingSet();
		
		for (Binding binding : lq.getQueryBindings()) 
			bs.getBindings().add(binding);				
		
		// Check if Query already exists in Collection.
		int index = collection.indexOf(qr);
		
		if (index != -1) { // Exists in Collection.
//			System.out.println("Query Contained");
			
			// Get this Query from the Collection.
			RDFQueryRecord qrTemp = collection.get(index);
			// Add BindingSet into Query.
			qrTemp.getQueryResult().getBindingSets().add(bs);		

		} else { // Doesn't exist in Collection.
			System.out.println("Query Not Contained");

			// Add BindingSet into Query.
			qr.getQueryResult().getBindingSets().add(bs);
			// Add the Query into Collection.
			collection.add(qr);
			
		}// else	
		
	}// processQuery


	/**
	 * TEST MAIN	
	 */
	public static void main(String[] args) {		
		
		long start = System.currentTimeMillis();

//		LogParser lp = new LogParser("src\\main\\resources\\semagrow_logs.log");
//		LogParser lp = new LogParser("src\\main\\resources\\test_2.txt");
		LogParser lp = new LogParser("src/main/resources/semagrow_logs_3.log");
		lp.parse();		
		
		// Print the parsed queries signatures.
//		for (RDFQueryRecord qr : lp.getCollection()) {
//			System.out.println(qr.getQuery());
//		}
		
		/*
		 * Report
		 */
		long end = System.currentTimeMillis();
		System.out.println("\n\n********************************");
		System.out.println("Total Parse Time: " + (double) (end - start) / 1000
				+ " sec.");
		System.out.println("Total Distinct Log Queries Parsed: "
				+ lp.getCollection().size());
		System.out.println("********************************");
		
		RDFQueryRecord rdfqr = new RDFQueryRecord(lp.getCollection().get(0).getLogQuery());

		RDFRectangle rec = rdfqr.getRectangle();
		System.out.println(rec);
				
//		RDFQueryRecord rdfqr1 = new RDFQueryRecord(lp.getCollection().get(1).getLogQuery());
//		RDFRectangle rec1 = rdfqr1.getRectangle();
//		System.out.println(rec1);

        List<RDFRectangle> rects = lp.getCollection().get(0).getQueryResult().getRectangles(rec);
		System.out.println("Check rectangles: ");
        for (RDFRectangle r : rects) {
            System.out.println(r);
        }
		
		//System.out.println(lp.getCollection().get(0).getQueryResult().getCardinality(rec));
		
		
		
		
		
		
		

		
	}// main


	/*
	 * Getters & Setters.
	 */
	public String getPath() {
		return path;
	}


	public void setPath(String path) {
		this.path = path;
	}


	
	public List<RDFQueryRecord> getCollection() {
		return collection;
	}


	
	public void setCollection(List<RDFQueryRecord> collection) {
		this.collection = collection;
	}

}
