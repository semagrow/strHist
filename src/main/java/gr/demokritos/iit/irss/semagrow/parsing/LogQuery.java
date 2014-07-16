package gr.demokritos.iit.irss.semagrow.parsing;

import java.util.ArrayList;
import java.util.List;

public class LogQuery {

	private String sessionId;
	private long startTime;
	private String sparqlEndpoint;
	private int numOfBinds;
	// Contains only the Query's Statements.
	private List<Binding> queryStatements;
	// Contains only the Query's Bindings.
	private List<Binding> queryBindings;


	public LogQuery() {
		setQueryStatements(new ArrayList<Binding>());
		setQueryBindings(new ArrayList<Binding>());
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

}
