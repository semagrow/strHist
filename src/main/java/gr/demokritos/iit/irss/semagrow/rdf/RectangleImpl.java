package gr.demokritos.iit.irss.semagrow.rdf;

import gr.demokritos.iit.irss.semagrow.api.Point;
import gr.demokritos.iit.irss.semagrow.api.Range;
import gr.demokritos.iit.irss.semagrow.api.Rectangle;

import java.util.List;

/**
 * Created by angel on 7/15/14.
 */
public class RectangleImpl<T> implements Rectangle<T> {

    private List<Range<? extends T>> list;


    public int getDimensionality() {
        return 0;
    }

    public Rectangle<T> intersection(Rectangle<T> rec) {

        return null;
    }

    public boolean contains(Rectangle<T> rec) {

        return false;
    }


    public boolean contains(Point<T> point) {
        return false;
    }


    public boolean equals(Rectangle<T> rec) {
        return false;
    }

    @Override
    public Range<T> getRange(int i) {
        return null;
    }
}
