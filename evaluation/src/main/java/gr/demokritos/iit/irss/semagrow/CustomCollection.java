package gr.demokritos.iit.irss.semagrow;

import java.util.Iterator;

/**
 * Created by Nick on 10-Aug-14.
 */
public class CustomCollection<E> implements Iterable<E> {

    private String poolPath;

    public CustomCollection(String poolPath) {
        this.poolPath = poolPath;
    }

    @Override
    public Iterator<E> iterator() {
        return new CustomIterator<E>(poolPath);
    }
}
