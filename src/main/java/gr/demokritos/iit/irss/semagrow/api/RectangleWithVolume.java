package gr.demokritos.iit.irss.semagrow.api;

import org.json.simple.JSONObject;

/**
 * Created by efi on 6/8/2014.
 */
public interface RectangleWithVolume<R> {

    /**
     * Return number of total dimensions
     * @return
     */
    int getDimensionality();

    R intersection(R rec);

    boolean contains(R rec);

    RangeLength<?> getRange(int i);

    boolean intersects(R rec);

    void shrink(R rec);

    // compute bounding box that tightly encloses
    // this rectangle and rec
    R computeTightBox(R rec);

    boolean equals(Object rec);

    JSONObject toJSON();

    //true if rectangle has
    //at least one infinite range
    boolean hasInfinite();

    long getVolume();
}

