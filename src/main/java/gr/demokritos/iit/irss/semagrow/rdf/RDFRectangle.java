package gr.demokritos.iit.irss.semagrow.rdf;

import gr.demokritos.iit.irss.semagrow.api.*;

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


    public RDFRectangle shrink(RDFRectangle rec) {
        return this;
    }

}
