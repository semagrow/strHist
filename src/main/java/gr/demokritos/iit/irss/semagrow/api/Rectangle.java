package gr.demokritos.iit.irss.semagrow.api;

/**
 * Rectangle is essentially a multidimensional bounding box
 * Created by angel on 7/11/14.
 */
public interface Rectangle {

    /**
     * Return number of total dimensions
     * @return
     */
    int getDimensionality();

    void union(Rectangle rec);

    void intersection(Rectangle rec);

    boolean contains(Rectangle rec);

    boolean contains(Point point);

    boolean equals(Rectangle rec);
}
