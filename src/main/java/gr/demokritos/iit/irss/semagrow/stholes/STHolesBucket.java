package gr.demokritos.iit.irss.semagrow.stholes;


import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collection;

/**
 * Created by angel on 7/11/14.
 */
public class STHolesBucket {

    public Rectangle getRectangle() {
        throw new NotImplementedException();
    }

    public long getFrequency() {
        throw new NotImplementedException();
    }

    public Collection<STHolesBucket> children()  {
        return null;
    }

    public STHolesBucket parent() {
        return null;
    }

    public void addChild(STHolesBucket bucket) { }

    public static STHolesBucket merge(STHolesBucket bucket1, STHolesBucket bucket2) {
        return null;
    }

    public static STHolesBucket shrink() {
        return null;
    }

}
