package gr.demokritos.iit.irss.semagrow.rdf.io.log;

import java.io.Serializable;

public class Binding implements Serializable {

	private String name, value;


	public Binding(String name, String value) {
		setName(name);
		setValue(value);		
	}


	public Binding(String name) {
		setName(name);
		setValue("");
	}


	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Binding) {
			Binding spv = (Binding) obj;

			if (spv.getName().equals(this.getName()))
				if (spv.getValue().equals(this.getValue()))
					return true;
		}

		return false;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}

}
