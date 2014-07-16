package gr.demokritos.iit.irss.semagrow.parsing;

import java.util.ArrayList;

public class BindingSet {

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
