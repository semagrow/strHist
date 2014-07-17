package gr.demokritos.iit.irss.semagrow.rdf;

import gr.demokritos.iit.irss.semagrow.api.*;

import javax.security.auth.Subject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by angel on 7/15/14.
 */
public class RDFRectangle implements Rectangle<RDFRectangle> {

	private PrefixRange subjectRange;

	private ExplicitSetRange<String> predicateRange;

	// private Range<Object> objectRange;
	private RDFLiteralRange objectRange;


	public RDFRectangle(PrefixRange subjectRange,
			ExplicitSetRange<String> predicateRange, RDFLiteralRange objectRange) {
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


	public boolean equals(RDFRectangle rec) {
		return this.contains(rec) && rec.contains(this);
	}


	public Range<?> getRange(int i) {

		switch (i) {
		case 1:
			return subjectRange;
		case 2:
			return predicateRange;
		case 3:
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


    // Shrink rectangle so that it does not intersect with rec
    public void shrink(RDFRectangle rec) {

        PrefixRange subjectRangeN = subjectRange.minus(rec.subjectRange);
        ExplicitSetRange<String> predicateRangeN = predicateRange.minus(rec.predicateRange);
        RDFLiteralRange objectRangeN = objectRange.minus(rec.objectRange);

        ArrayList<Double> lengths = new ArrayList<Double>();
        long subjectLength = subjectRange.getLength();
        long subjectNLength = subjectRangeN.getLength();
        double reduced = subjectNLength/ subjectLength *100;
        lengths.add(reduced);
        long predicateLength = predicateRange.getLength();
        long predicateNLength = predicateRangeN.getLength();
        reduced = predicateNLength/ predicateLength *100;
        lengths.add(reduced);
        long objectLength = objectRange.getLength();
        long objectNLength = objectRangeN.getLength();
        reduced = objectNLength/ objectLength *100;
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
            case 1:
                setSubjectRange(subjectRangeN);
            case 2:
                setPredicateRange(predicateRangeN);
            case 3:
                setObjectRange(objectRangeN);
            default:
                throw new IllegalArgumentException("Dimension " + j
                        + " is not valid");
        }

    }

    public void setObjectRange(RDFLiteralRange objectRange) {
        this.objectRange = objectRange;
    }

    public void setPredicateRange(ExplicitSetRange<String> predicateRange) {
        this.predicateRange = predicateRange;
    }

    public void setSubjectRange(PrefixRange subjectRange) {
        this.subjectRange = subjectRange;
    }
}
