package gr.demokritos.iit.irss.semagrow.rdf.qfr;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import gr.demokritos.iit.irss.semagrow.base.range.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.base.range.PrefixRange;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.base.Stat;
import gr.demokritos.iit.irss.semagrow.rdf.RDFLiteralRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.parsing.Binding;
import gr.demokritos.iit.irss.semagrow.rdf.parsing.LogQuery;
import gr.demokritos.iit.irss.semagrow.rdf.parsing.QueryFilter;
import gr.demokritos.iit.irss.semagrow.rdf.parsing.Utilities;

public class RDFQueryRecord implements QueryRecord<RDFRectangle,Stat> {

	// Contains the Query's signature elements.
	private LogQuery logQuery;
	// Contains the Query's result's bindings sets.
	private RDFQueryResult queryResults;


	public RDFQueryRecord(LogQuery logQuery) {
		this.logQuery = logQuery;
		setQueryResult(new RDFQueryResult(logQuery.getQueryStatements()));
	}


	/**
	 * Returns a Rectangle over the Query.
	 */
	public RDFRectangle getRectangle() {

		System.out.println("SIZE: "+logQuery.getQueryStatements().size());
		
		PrefixRange subjectRange = getSubjectRange(logQuery.getQueryStatements().get(0));

		ExplicitSetRange<String> predicateRange = getPredicateRange(logQuery.getQueryStatements().get(1));

		RDFLiteralRange objectRange = getObjectRange(logQuery.getQueryStatements().get(2));
				
		return new RDFRectangle(subjectRange, predicateRange, objectRange);
	}// getRectangle


	/**
	 * TODO: Unchecked
	 */
	private RDFLiteralRange getObjectRange(Binding binding) {
		RDFLiteralRange objectRange = null;
		
		// Check what's inside the object's value.
		if (binding.getValue().equals("")) {// If object is a variable
			// Check if any Filter for object exists.
			QueryFilter qf = null, qf2 = null;
			if ((qf = hasFilter(binding.getName())) != null) {
				// Check the type of the filter
				if (qf.getFilterType().equals("Compare")) {// Filter: Compare -> Integer,Long,Date
					// Compare filter is assumed that ALWAYS comes with an other Compare Filter. 
					// So find the second one.
					qf2 = findPairCompareFilter(qf);
					
					// Find low-high values and parse it.
					String high = "", low = "";
					
					if (!qf.getHigh().equals(""))
						high = qf.getHigh();
					else if (!qf2.getHigh().equals(""))
						high = qf2.getHigh();
					
					if (!qf.getLow().equals(""))
						low = qf.getLow();
					else if (!qf2.getLow().equals(""))
						low = qf2.getLow();
					
					// Find the type(Integer,Long,Date) of the URIS.
					String type = Utilities.getTypeFromURI(low);
					low = Utilities.getValueFromURI(low);
					high = Utilities.getValueFromURI(high);
					
					if (type.equals("int") || type.equals("integer")) {				
						objectRange = new RDFLiteralRange(Integer.parseInt(low), Integer.parseInt(high));
						
					} else if (type.equals("long")) {						
						objectRange = new RDFLiteralRange(Long.parseLong(low), Long.parseLong(high));
						
					} else if (type.equals("dateTime")) {
						
						DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
						Date dateLow = null, dateHigh = null;
						
						try {
							dateLow = format.parse(low);
							dateHigh = format.parse(high);}
						catch (ParseException e) {e.printStackTrace();}
						
						if (dateLow != null && dateHigh != null)
							objectRange = new RDFLiteralRange(dateLow, dateHigh);
						else 
							System.err.println("Date Format Error.");
					}				
					
				} else if (qf.getFilterType().equals("Regex")) {// Filter: Regex -> Plain Literal or URL
					objectRange = new RDFLiteralRange(qf.getRegex());
				}				
				
			} else {
				objectRange = new RDFLiteralRange();
				System.err.println("Empty Object");
				//TODO: Apeiro Object. 
				// Xtypaei nullpointerexception giati ston keno constructor tou RDFLiteralRange
				// den dinetai timi sti metavliti range i ipoia xrisimopoieitai stin toString
			}
			
		} else if (binding.getValue().contains("^^")) {// URI
			String value = Utilities.getValueFromURI(binding.getValue());
			String type = Utilities.getTypeFromURI(binding.getValue());
			
			if (type.equals("int")) {
				objectRange = new RDFLiteralRange(Integer.parseInt(value), Integer.parseInt(value));
			} else if (type.equals("long")) {
				objectRange = new RDFLiteralRange(Long.parseLong(value), Long.parseLong(value));
			} else if (type.equals("dateTime")) {
				
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				Date date = null;
				
				try {date = format.parse(value);}
				catch (ParseException e) {e.printStackTrace();}
				
				if (date != null)
					objectRange = new RDFLiteralRange(date, date);
				else 
					System.err.println("Date Format Error.");
			}
			
		} else {// Plain Literal or URL
			objectRange = new RDFLiteralRange(binding.getValue());
		}	
		
		return objectRange;
	}// getObjectRange
	
	
	/**
	 * Given a QueryFilter find its pair one. (Low-High)	 
	 */
	private QueryFilter findPairCompareFilter(QueryFilter qf) {
		
		for (QueryFilter temp : getLogQuery().getQueryFilters()) {
			if (temp.getVariable().equals(qf.getVariable()))
				if (!temp.equals(qf))
					return temp;
		}
		
		return null;
	}


	public static void main(String[] args) {
	
		
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ExplicitSetRange<String> getPredicateRange(Binding binding) {
		HashSet<String> set = new HashSet<String>();

		// Check if binding is a const variable or not.
		if (binding.getValue().equals("")) {// Is variable
			// Check if any Filter for predicate exists.
			QueryFilter qf = null;
			if ((qf = hasFilter(binding.getName())) != null) {
				Set<String> explicitSet = new HashSet<String>();
				explicitSet.add(qf.getRegex());
							
				return new ExplicitSetRange(explicitSet);
							
			} else // Call the empty constructor, it handles this case. 					
				return new ExplicitSetRange();
			
		} else {// Is const
				// Add the whole value as predicate
				// TODO: Think if this is really ok.
			set.add(binding.getValue());
		}

		return new ExplicitSetRange<String>(set);
	}// getPredicateRange


	private PrefixRange getSubjectRange(Binding binding) {
		ArrayList<String> prefix = new ArrayList<String>();

		// Check if binding is a const variable or not.
		if (binding.getValue().equals("")) {// Is variable			
			// Check if any Filter for subject exists.
			QueryFilter qf = null;
			if ((qf = hasFilter(binding.getName())) != null) {
				ArrayList<String> prefixList = new ArrayList<String>();
				prefixList.add(qf.getRegex());
				
				return new PrefixRange(prefixList);
				
			} else // Call the empty constructor, it handles this case. 					
				return new PrefixRange();
			
		} else {// Is const
				// Add the whole value as prefix
				// TODO: Think if this is really ok.
			prefix.add(binding.getValue());
		}

		return new PrefixRange(prefix);
	}// getSubjectRange
	
	
	/**
	 * Checks if the given variable has a filter on it and returns it.	
	 */
	private QueryFilter hasFilter(String name) {		
		
		for (QueryFilter qf : getLogQuery().getQueryFilters()) {
			if (qf.getVariable().equals(name))
				return qf;
		}
		
		return null;
	}


	/**
	 * Returns a Rectangle over the Query's Results.
	 * TODO: Not implemented yet.
	 */
	public RDFQueryResult getResultSet() {
		return getQueryResult();
	}

	/**
	 * Returns a String representation of the Query.
	 */
	public String getQuery() {
		return logQuery.getQuery();
	}// getQuery


	@Override
	public boolean equals(Object obj) {

		if (obj instanceof RDFQueryRecord) {

			RDFQueryRecord qr = (RDFQueryRecord) obj;

			if (qr.getLogQuery().equals(this.getLogQuery()))
				return true;
		}

		return false;
	}// equals


	/*
	 * Getters & Setters.
	 */

	public LogQuery getLogQuery() {
		return logQuery;
	}


	public void setLogQuery(LogQuery logQuery) {
		this.logQuery = logQuery;
	}


	public RDFQueryResult getQueryResult() {
		return queryResults;
	}


	public void setQueryResult(RDFQueryResult queryResult) {
		this.queryResults = queryResult;
	}

}
