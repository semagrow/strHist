package gr.demokritos.iit.irss.semagrow.rdf.parsing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LogQuery implements Serializable {

	private String sessionId;
	private long startTime;
	private String sparqlEndpoint;
	private int numOfBinds;
	// Contains only the Query's Statements.
	private List<Binding> queryStatements;
	// Contains only the Query's Bindings.
	private List<Binding> queryBindings;
	private List<QueryFilter> queryFilters;


	public LogQuery() {
		setQueryStatements(new ArrayList<Binding>());
		setQueryBindings(new ArrayList<Binding>());
		setQueryFilters(new ArrayList<QueryFilter>());
	}


	/**
	 * Returns a String representation of the Query.
	 */
	public String getQuery() {

		String s = "";

		s += "\n" + getSessionId();
//		s += "\n" + getStartTime();
		s += "\n" + getSparqlEndpoint();

		for (Binding spv : getQueryStatements())
			s += "\n" + spv.getName() + " " + spv.getValue();
				
		for (QueryFilter qf : getQueryFilters())
			s += "\n" + qf.getFilterType() + " " + qf.getVariable() + " " +
					qf.getRegex() + " " + qf.getLow() + " " + qf.getHigh();

		return s;
	}// getQuery


	@Override
	public boolean equals(Object obj) {

		if (obj instanceof LogQuery) {

			LogQuery lq = (LogQuery) obj;
			
			if (lq.numOfBinds == this.numOfBinds)	
				if (lq.getQuery().equals(this.getQuery()))
					return true;
		}// if
		
		return false;
	}// equals


	/*
	 * Getters & Setters.
	 */
	public String getSessionId() {
		return sessionId;
	}


	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}


	public long getStartTime() {
		return startTime;
	}


	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}


	public int getNumOfBinds() {
		return numOfBinds;
	}


	public void setNumOfBinds(int numOfBinds) {
		this.numOfBinds = numOfBinds;
	}


	public String getSparqlEndpoint() {
		return sparqlEndpoint;
	}


	public void setSparqlEndpoint(String sparqlEndpoint) {
		this.sparqlEndpoint = sparqlEndpoint;
	}


	public List<Binding> getQueryStatements() {
		return queryStatements;
	}


	public void setQueryStatements(List<Binding> queryStatements) {
		this.queryStatements = queryStatements;
	}


	public List<Binding> getQueryBindings() {
		return queryBindings;
	}


	public void setQueryBindings(List<Binding> queryBindings) {
		this.queryBindings = queryBindings;
	}


	public List<QueryFilter> getQueryFilters() {
		return queryFilters;
	}


	public void setQueryFilters(List<QueryFilter> queryFilters) {
		this.queryFilters = queryFilters;
	}

}
