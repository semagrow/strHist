package gr.demokritos.iit.irss.semagrow.rdf;

import gr.demokritos.iit.irss.semagrow.api.QueryRecord;
import gr.demokritos.iit.irss.semagrow.api.QueryResult;
import gr.demokritos.iit.irss.semagrow.parsing.LogQuery;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;

public class RDFQueryRecord implements QueryRecord<RDFRectangle> {

	// Contains the Query's signature elements.
	private LogQuery logQuery;
	// Contains the Query's result's bindings sets.
	private RDFQueryResult queryResults;


	public RDFQueryRecord(LogQuery logQuery) {
		this.logQuery = logQuery;
		setQueryResult(new RDFQueryResult(logQuery.getQueryStatements()));
	}


	/**
	 * NOT IMPLEMENTED YET
	 */
	public RDFRectangle getRectangle() {
		return null;
	}

	
	/**
	 * NOT IMPLEMENTED YET
	 */
	public QueryResult<RDFRectangle> getResultSet() {
		return null;
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
