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
    private boolean infinite = false;

	public ExplicitSetRange(Collection<T> items) {
		this.items = new HashSet<T>(items);
	}


    // Construct an infinite expilicitSet range
	public ExplicitSetRange() {
        this.items = new HashSet<T>();
		infinite = true;
	}


	// TODO: It should be T, but complains.
    public boolean contains(String value) {
        if (infinite) return true;

        return items.contains(value);
    }
    
	public boolean contains(ExplicitSetRange<T> range) {

        if (infinite) return true;

        return items.containsAll(range.items);
	}

   
    public boolean intersects(ExplicitSetRange<T> range) {

        if (infinite) return true;

        ExplicitSetRange<T> esr = new ExplicitSetRange<T>(range.items);

        esr.items.retainAll(this.items);

        if (esr.getLength() == 0) {
            return false;
        }

        return true;
    }


    public ExplicitSetRange<T> tightRange(ExplicitSetRange<T> tExplicitSetRange) {

        if (infinite) return new ExplicitSetRange<T>();

        Set<T> itemsN = new HashSet<T>(items);
        itemsN.addAll(tExplicitSetRange.items);

        return null;
    }


    public ExplicitSetRange<T> intersection(ExplicitSetRange<T> range) {

        if (infinite) return range;

        ExplicitSetRange<T> esr = new ExplicitSetRange<T>(range.items);

        esr.items.retainAll(this.items);

        return esr;
	}

    

    public ExplicitSetRange<T> minus(ExplicitSetRange<T> tExplicitSetRange) {

        //todo: handle infinite
        Set<T> set = new HashSet<T>(this.items);
        set.removeAll(tExplicitSetRange.items);
        ExplicitSetRange<T> r = new ExplicitSetRange<T>(set);
        return r;
    }


    public String toString() {
        String res;

        res = "uris: { ";

        for (T p : items) {

            res = res + p + " ";
        }

        res += "}";

        return res;
    }

    public boolean isUnit() {

        if (infinite) return false;

        return getLength() == 1;
    }

    public long getLength() {

        if (infinite) return Integer.MAX_VALUE;
        return items.size();
    }

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

        System.out.println(esr2);
	}
}
