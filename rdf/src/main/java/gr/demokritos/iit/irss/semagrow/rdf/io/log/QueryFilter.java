package gr.demokritos.iit.irss.semagrow.rdf.io.log;

import java.io.Serializable;

public class QueryFilter implements Serializable {

	private String filterType;
	private String variable;
	private String regex;
	private String low, high;


	public QueryFilter(String filterType, String variable) {
		setFilterType(filterType);
		setVariable(variable);
		setRegex("");
		setHigh("");
		setLow("");
	}
	
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof QueryFilter) {
			QueryFilter qf = (QueryFilter)obj;
			if (this.getFilterType().equals(qf.getFilterType()))
				if (this.getVariable().equals(qf.getVariable()))
					if (this.getRegex().equals(qf.getRegex()))
						if (this.getLow() == qf.getLow())
							if (this.getHigh() == qf.getHigh())
								return true;
		}
		
		return false;
	}


	public String getFilterType() {
		return filterType;
	}


	public void setFilterType(String filterType) {
		this.filterType = filterType;
	}


	public String getRegex() {
		return regex;
	}


	public void setRegex(String regex) {
		this.regex = regex;
	}


	public String getLow() {
		return low;
	}


	public void setLow(String low) {
		this.low = low;
	}


	public String getHigh() {
		return high;
	}


	public void setHigh(String high) {
		this.high = high;
	}


	public String getVariable() {
		return variable;
	}


	public void setVariable(String variable) {
		this.variable = variable;
	}

}
