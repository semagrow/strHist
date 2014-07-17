package gr.demokritos.iit.irss.semagrow.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Defines a set of T explicitly by defining each member of the set. Created by
 * angel on 7/12/14.
 */
public class ExplicitSetRange<T>
        implements RangeLength<T>, Rangeable<ExplicitSetRange<T>>
{

	private Set<T> items;

	public ExplicitSetRange(Collection<T> items) {
		this.items = new HashSet<T>(items);
	}


	public boolean contains(ExplicitSetRange<T> range) {
        return items.containsAll(range.items);
	}

    
    public boolean intersects(ExplicitSetRange<T> range) {

        ExplicitSetRange<T> esr = new ExplicitSetRange<T>(range.items);

        esr.items.retainAll(this.items);

        if (esr.getLength() == 0) {
            return false;
        }

        return true;
    }


    public ExplicitSetRange<T> intersection(ExplicitSetRange<T> range) {

        ExplicitSetRange<T> esr = new ExplicitSetRange<T>(range.items);

        esr.items.retainAll(this.items);

        return esr;
	}

    
    public ExplicitSetRange<T> minus(ExplicitSetRange<T> tExplicitSetRange) {
        Set<T> set = new HashSet<T>(this.items);
        set.removeAll(tExplicitSetRange.items);
        ExplicitSetRange<T> r = new ExplicitSetRange<T>(set);
        return r;
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

		ExplicitSetRange<String> esr = (ExplicitSetRange) esr1.intersection(esr2);
		Set<String> sss = ((ExplicitSetRange) esr).items;
		System.out.println("Intersect: " + sss.toString());
	}
}
