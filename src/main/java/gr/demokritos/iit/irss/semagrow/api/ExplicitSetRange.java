package gr.demokritos.iit.irss.semagrow.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Defines a set of T explicitly by defining each member of the set. Created by
 * angel on 7/12/14.
 */
public class ExplicitSetRange<T> implements Range<T> {

	private Set<T> items;


	public ExplicitSetRange(Collection<T> items) {
		this.items = new HashSet<T>(items);
	}


	public boolean contains(T item) {
		return items.contains(item);
	}


	public boolean contains(Range<T> range) {
		if (range instanceof ExplicitSetRange)
			return items.containsAll(((ExplicitSetRange) range).items);
		
		return false;
	}


	public Range<T> intersect(Range<T> range) {
		if (range instanceof ExplicitSetRange) {

			ExplicitSetRange esr = new ExplicitSetRange(
					((ExplicitSetRange) range).items);

			((ExplicitSetRange) esr).items.retainAll(this.items);

			return esr;
		}
		return range;
	}


	public Range<T> union(Range<T> range) {
		if (range instanceof ExplicitSetRange) {
			
			ExplicitSetRange esr = new ExplicitSetRange(
					((ExplicitSetRange) range).items);
			
			((ExplicitSetRange) esr).items.addAll(this.items);
			
			return esr;
		}

		return range;
	}

    public boolean isUnit() { return getLength() == 1; }

    public long getLength() { return items.size(); }

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) {

		HashSet s1 = new HashSet<String>();
		s1.add('a');
		s1.add('b');
		s1.add('c');
		ExplicitSetRange esr1 = new ExplicitSetRange(s1);
		HashSet s2 = new HashSet<String>();
		s2.add('a');
		s2.add('b');
		s2.add('d');
		ExplicitSetRange esr2 = new ExplicitSetRange(s2);

		ExplicitSetRange<String> esr = (ExplicitSetRange) esr1.intersect(esr2);
		Set<String> sss = ((ExplicitSetRange) esr).items;
		System.out.println("Intesect: " + sss.toString());	
		
		esr = (ExplicitSetRange) esr1.union(esr2);
		sss = ((ExplicitSetRange) esr).items;
		System.out.println("Union: " + sss.toString());

	}
}
