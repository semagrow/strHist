package gr.demokritos.iit.irss.semagrow.rdf.io.log;

import java.io.Serializable;
import java.util.ArrayList;

public class BindingSet implements Serializable {

	private ArrayList<Binding> bindings;


	public BindingSet() {
		setBindings(new ArrayList<Binding>());
	}


	public ArrayList<Binding> getBindings() {
		return bindings;
	}


	public void setBindings(ArrayList<Binding> results) {
		this.bindings = results;
	}

}
