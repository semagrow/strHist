package gr.demokritos.iit.irss.semagrow.api;

import java.util.Map;

/**
 * Created by efi on 6/8/2014.
 */
public interface RectangleWithVolume<R> extends Rectangle<R>{

    long getVolume();

    public void shrink(R rec, int dimension);

    public Map.Entry<Double, Integer> getShrinkInfo(R rec);
}

