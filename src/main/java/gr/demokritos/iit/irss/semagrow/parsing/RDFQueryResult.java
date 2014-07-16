package gr.demokritos.iit.irss.semagrow.parsing;

import gr.demokritos.iit.irss.semagrow.api.QueryResult;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;

import java.util.ArrayList;

public class RDFQueryResult implements QueryResult<RDFRectangle> {

	private ArrayList<BindingSet> bindingSets;


	public RDFQueryResult() {
		setBindingSets(new ArrayList<BindingSet>());
	}

	/**
	 * NOT IMPLEMENTED YET
	 */
	public long getCardinality(RDFRectangle rect) {		
		return 0;
	}


	public long getCardinality() {
		return bindingSets.size();
	}


	public ArrayList<BindingSet> getBindingSets() {
		return bindingSets;
	}


	public void setBindingSets(ArrayList<BindingSet> bindingSets) {
		this.bindingSets = bindingSets;
	}

}
