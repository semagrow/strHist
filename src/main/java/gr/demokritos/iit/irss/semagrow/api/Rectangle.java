package gr.demokritos.iit.irss.semagrow.api;

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

    R intersection(R rec);

    boolean contains(R rec);

    Range<?> getRange(int i);
}
