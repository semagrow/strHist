package gr.demokritos.iit.irss.semagrow.api;

import gr.demokritos.iit.irss.semagrow.api.range.Range;

/**
 * Rectangle is essentially a multidimensional bounding box
 * Created by angel on 7/11/14.
 */
public interface Rectangle<R> {

    /**
     * Return number of total dimensions
     * @return
     */
    int getDimensionality();

    Range<?> getRange(int i);

    R intersection(R rec);

    boolean contains(R rec);

    boolean intersects(R rec);

    void shrink(R rec);

    // compute bounding box that tightly encloses
    // this rectangle and rec
    R computeTightBox(R rec);
    
    boolean equals(Object rec);

    //true if rectangle has
    //at least one infinite range
    boolean isInfinite();

    boolean isMergeable(R rec);

    boolean isEmpty();

}