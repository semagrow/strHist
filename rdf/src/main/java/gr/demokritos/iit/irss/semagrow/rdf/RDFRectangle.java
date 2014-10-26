package gr.demokritos.iit.irss.semagrow.rdf;

import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.api.range.Range;
import gr.demokritos.iit.irss.semagrow.base.range.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.base.range.PrefixRange;
import org.openrdf.model.URI;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by angel on 7/15/14.
 */
public class RDFRectangle implements Rectangle<RDFRectangle> {

    private RDFURIRange subjectRange;

	private ExplicitSetRange<URI> predicateRange;

	// private Range<Object> objectRange;
	private RDFValueRange objectRange;


	public RDFRectangle(RDFURIRange subjectRange,
			ExplicitSetRange<URI> predicateRange, RDFValueRange objectRange) {
		this.subjectRange = subjectRange;
		this.predicateRange = predicateRange;
		this.objectRange = objectRange;
	}


	public int getDimensionality() {
		return 3;
	}


	public RDFRectangle intersection(RDFRectangle rec) {
		return new RDFRectangle(subjectRange.intersection(rec.subjectRange),
				predicateRange.intersection(rec.predicateRange),
				objectRange.intersection(rec.objectRange));
	}


	public boolean contains(RDFRectangle rec) {
		return subjectRange.contains(rec.subjectRange)
				&& predicateRange.contains(rec.predicateRange)
				&& objectRange.contains(rec.objectRange);
	}

	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RDFRectangle) {
			RDFRectangle rec = (RDFRectangle)obj;
			return this.contains(rec) && rec.contains(this);
			
		}
		
		return false;
	}


    //Tested
	public Range<?> getRange(int i) {

		switch (i) {
		case 0:
			return subjectRange;
		case 1:
			return predicateRange;
		case 2:
			return objectRange;
		default:
			throw new IllegalArgumentException("Dimension " + i
					+ " is not valid");
		}

	}

    
    public boolean intersects(RDFRectangle rec) {

        boolean res = subjectRange.intersects(rec.subjectRange) &&
                predicateRange.intersects(rec.predicateRange) &&
                objectRange.intersects(rec.objectRange);

        return res;
    }


    //Tested
    // Shrink rectangle so that it does not intersect with rec
    public void shrink(RDFRectangle rec) {

        RDFURIRange subjectRangeN = subjectRange.minus(rec.subjectRange);
        ExplicitSetRange<URI> predicateRangeN = predicateRange.minus(rec.predicateRange);
        RDFValueRange objectRangeN = objectRange.minus(rec.objectRange);

        ArrayList<Double> lengths = new ArrayList<Double>();
        long subjectLength = subjectRange.getLength();
        long subjectNLength = subjectRangeN.getLength();
        double reduced = ((double) subjectNLength)/ subjectLength *100;
        lengths.add(reduced);
        long predicateLength = predicateRange.getLength();
        long predicateNLength = predicateRangeN.getLength();
        reduced = ((double) predicateNLength)/ predicateLength *100;
        lengths.add(reduced);
        long objectLength = objectRange.getLength();
        long objectNLength = objectRangeN.getLength();
        reduced = ((double) objectNLength)/ objectLength *100;
        lengths.add(reduced);

        double largest = lengths.get(0);
        int j = 0; //dimension
        for (int i = 0; i < lengths.size(); i++) {

            if ( lengths.get(i) > largest ) {
                
                largest = lengths.get(i);
                j = i;
            }
        }

        switch (j) {
            case 0:
                setSubjectRange(subjectRangeN);
                break;
            case 1:
                setPredicateRange(predicateRangeN);
                break;
            case 2:
                setObjectRange(objectRangeN);
                break;
            default:
                throw new IllegalArgumentException("Dimension " + j
                        + " is not valid");
        }

    }

    public RDFRectangle computeTightBox(RDFRectangle rec) {

        RDFURIRange subjectRangeN = subjectRange.tightRange(rec.subjectRange);
        ExplicitSetRange<URI> predicateRangeN = predicateRange.tightRange(rec.predicateRange);
        RDFValueRange objectRangeN = objectRange.tightRange(rec.objectRange);
        
        return new RDFRectangle(subjectRangeN, predicateRangeN, objectRangeN);
    }

    public void setObjectRange(RDFValueRange objectRange) {
        this.objectRange = objectRange;
    }

    public void setPredicateRange(ExplicitSetRange<URI> predicateRange) {
        this.predicateRange = predicateRange;
    }

    public void setSubjectRange(RDFURIRange subjectRange) {
        this.subjectRange = subjectRange;
    }

    public boolean isInfinite() {

        return subjectRange.isInfinite() ||
               predicateRange.isInfinite() ||
               objectRange.isInfinite();
    }

    public boolean isMergeable(RDFRectangle rec) {

        return objectRange.hasSameType(rec.objectRange);
    }

    public boolean isEmpty() {
        return subjectRange.isEmpty() || predicateRange.isEmpty() || objectRange.isEmpty();
    }

    public boolean isEnclosing(RDFRectangle r) {
        // ASSUMPTION: Infinite is considered to be enclosed in every rectangle.

        boolean b = true;

        if (r.subjectRange.isInfinite())
            b = b && true;
        else
            b = b && subjectRange.contains(r.subjectRange);


        if (r.predicateRange.isInfinite())
            b = b && true;
        else
            b = b && predicateRange.contains(r.predicateRange);

        if (r.objectRange.isInfinite())
            b = b && true;
        else
            b = b && objectRange.contains(r.objectRange);

        return b;
    }

    public String toString() {

        String res = "rectangle:\n" +
                "\tsubject:\n" + "\t\t" + subjectRange.toString() + "\n" +
                "\tpredicate:\n" + "\t\t" + predicateRange.toString() + "\n" +
                "\tobject:\n" + "\t\t" + objectRange.toString() + "\n";

        return res;
    }

    public static void main(String args[] ) {
        /*
        ArrayList<String> myRangePrefixList = new ArrayList<String>();
        myRangePrefixList.add("http://a/");
        PrefixRange subjectRange = new PrefixRange(myRangePrefixList);

        HashSet<String> s1 = new HashSet<String>();
        s1.add("a");
        s1.add("b");
        s1.add("c");
        ExplicitSetRange<String> predicateRange = new ExplicitSetRange<String>(s1);

        int low = 0;
        int high = 10;
        RDFLiteralRange objectRange = new RDFLiteralRange(low, high);
        //RDFLiteralRange objectRange2  = new RDFLiteralRange();

        RDFRectangle rect = new RDFRectangle(subjectRange, predicateRange, objectRange);

        RDFLiteralRange objectRange2 = new RDFLiteralRange(low + 1, high);
        RDFRectangle rect2 = new RDFRectangle(subjectRange, predicateRange, objectRange2);
        System.out.println(rect + " and " + rect2 + " are mergeable: " + rect.isMergeable(rect2));

        RDFLiteralRange objectRange3 = new RDFLiteralRange("http://a");
        RDFRectangle rect3 = new RDFRectangle(subjectRange, predicateRange, objectRange3);
        System.out.println(rect + " and " + rect3 + " are mergeable: " + rect.isMergeable(rect3));

        System.out.println(rect.isInfinite());
        */
    }

    public RDFValueRange getObjectRange() {
        return objectRange;
    }

    public ExplicitSetRange<URI> getPredicateRange() {
        return predicateRange;
    }

    public RDFURIRange getSubjectRange() {
        return subjectRange;
    }
}
