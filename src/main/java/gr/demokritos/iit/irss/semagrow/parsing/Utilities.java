package gr.demokritos.iit.irss.semagrow.parsing;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {
	
	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	public static String cleanURI(String value) {
		// Create a regex pattern.
		String pattern = "\"(.*?)\"\\^\\^<(.*?)(#)(.*?)>";
		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);
		// Now create matcher object.
		Matcher m = r.matcher(value);

		if (m.find()) {
			value = m.group(2) + m.group(3) + m.group(4);
		}

		return value;
	}// cleanURI


	/**
	 * Extracts the Value from the URI, input:
	 * "192"^^<http://www.w3.org/2001/XMLSchema#int> returns: 192
	 */
	public static String getValueFromURI(String value) {
		String val = "";
		// Create a regex pattern.
		String pattern = "\"(.*?)\"\\^\\^<(.*?)#(.*?)>";
		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);
		// Now create matcher object.
		Matcher m = r.matcher(value);

		if (m.find())
			val = m.group(1);

		return val;
	}


	/**
	 * Extracts the Type from the URI, input:
	 * "192"^^<http://www.w3.org/2001/XMLSchema#int> returns: int
	 */
	public static String getTypeFromURI(String value) {
		String type = "";
		// Create a regex pattern.
		String pattern = "\"(.*?)\"\\^\\^<(.*?)#(.*?)>";
		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);
		// Now create matcher object.
		Matcher m = r.matcher(value);

		if (m.find())
			type = m.group(3);

		return type;
	}
	
	
	public static String getCompareOperator(String string) {
		String operator = "";
		// Create a regex pattern.
		String pattern = "\\((.*?)\\)";
		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);
		// Now create matcher object.
		Matcher m = r.matcher(string);

		if (m.find())
			operator = m.group(1);

		return operator;
	}


	public static String getVariableName(String string) {
		String variable = "";
		// Create a regex pattern.
		String pattern = "\\(name=(.*?)\\)";
		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);
		// Now create matcher object.
		Matcher m = r.matcher(string);

		if (m.find())
			variable = m.group(1);

		return variable;
	}


	public static String getValueConstant(String string) {
		String value = "";
		// Create a regex pattern.
		String pattern = "\\(value=(.*?)\\)";
		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);
		// Now create matcher object.
		Matcher m = r.matcher(string);

		if (m.find())
			value = m.group(1);

		return value;
	}


}
