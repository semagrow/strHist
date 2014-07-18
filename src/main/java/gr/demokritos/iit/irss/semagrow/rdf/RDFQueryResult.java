package gr.demokritos.iit.irss.semagrow.rdf;

import gr.demokritos.iit.irss.semagrow.api.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.api.PrefixRange;
import gr.demokritos.iit.irss.semagrow.api.QueryResult;
import gr.demokritos.iit.irss.semagrow.parsing.Binding;
import gr.demokritos.iit.irss.semagrow.parsing.BindingSet;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

public class RDFQueryResult implements QueryResult<RDFRectangle> {

	private ArrayList<BindingSet> bindingSets;
	// Query's statements stored for Cardinality estimation
	private List<Binding> queryStatements;


	public RDFQueryResult(List<Binding> queryStatements) {
		setBindingSets(new ArrayList<BindingSet>());
		this.queryStatements = queryStatements;
	}


	/**
	 * NOT TESTED YET
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public long getCardinality(RDFRectangle rect) {

		long cardinality = 0;

		// First check if const variables of the query exist in rectangle.
		// If not, then cardinality is surely 0.
		if (areConstContained(rect)) {
			for (BindingSet bs : bindingSets) {
				for (Binding b : bs.getBindings()) {
					// Find if the Binding is Subject or Predicate or Object.
					int type = getBindingType(b);

					String value = clean(b.getValue());

					switch (type) {
					case 1:
						if (((PrefixRange) rect.getRange(1)).contains(value))
							cardinality++;
						break;
					case 2:
						if (((ExplicitSetRange) rect.getRange(2)).contains(value))
							cardinality++;
						break;
					case 3:
						// TODO: Change!
						URI l = ValueFactoryImpl.getInstance().createURI(value);
						if (((RDFLiteralRange) rect.getRange(3)).contains(l))
							cardinality++;
						break;
					default:
						System.err.println("Not a valid Binding.");
						break;
					}

				}// for
			}// for
		}// if

		return cardinality;
	}// getCardinality


	/**
	 * Finds if the Binding is Subject or Predicate or Object.
	 * 
	 * @param b
	 *            Binding to be tested.
	 * @return 1 for Object, 2 for Predicate, 3 for Object
	 */
	private int getBindingType(Binding b) {
		String name = b.getName();

		for (int i = 1; i <= queryStatements.size(); i++) {
			if (queryStatements.get(i).getName().equals(name))
				return i;
		}

		return 0;
	}// getBindingType


	/**
	 * Finds the const variables of the Query and checks if they exist in
	 * Rectangle.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean areConstContained(RDFRectangle rect) {

		boolean b = true;
		// Find the const variables of the query and check them.
		for (int i = 0; i < queryStatements.size(); i++) {
			if (!queryStatements.get(i).getValue().equals("")) { // Is a Const
																	// Variable

				String value = clean(queryStatements.get(i).getValue());

				switch (i) {
				case 1:
					b = b && ((PrefixRange) rect.getRange(i)).contains(value);
					break;
				case 2:
					//b = b && ((ExplicitSetRange) rect.getRange(i)).contains(value);
					break;
				case 3:
					// TODO: change!
					URI l = ValueFactoryImpl.getInstance().createURI(value);
					b = b && ((RDFLiteralRange) rect.getRange(i)).contains(l);
					break;
				}

			}// if
			if (!b)
				return false;
		}// for

		return b;
	}// areConstContained


	/**
	 * Clean the trash from the string.
	 */
	private static String clean(String string) {
		String pattern = "@(.*?)\"";

		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);

		// Now create matcher object.
		Matcher m = r.matcher(string);

		if (m.find()) {
			System.err.println("MATCH");
			string = string.replace(m.group(0), "");
		}

		string = string.replaceAll("\"", "");

		return string.trim();
	}


	public static void main(String[] args) {
		System.out.println(clean("Egyptian Division @en"));
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
