package gr.demokritos.iit.irss.semagrow.qfr;

import info.aduna.iteration.Iteration;

import java.util.Iterator;

/**
 * Created by angel on 10/31/14.
 */
public class IterationIterator<T> implements Iterator<T> {

    private Iteration<T, ? extends Exception> iter;

    public IterationIterator(Iteration<T, ? extends Exception> iter) {
        this.iter = iter;
    }

    @Override
    public boolean hasNext() {
        try {
            return iter.hasNext();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public T next() {
        try {
            return iter.next();
        } catch(Exception e) {
            return null;
        }
    }

    @Override
    public void remove() {
        try {
            iter.remove();
        } catch(Exception e) {

        }
    }
}
