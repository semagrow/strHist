package gr.demokritos.iit.irss.semagrow.numerical;

import gr.demokritos.iit.irss.semagrow.api.*;
import gr.demokritos.iit.irss.semagrow.rdf.RDFLiteralRange;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by efi on 6/8/2014.
 */
public class NumRectangle implements RectangleWithVolume<NumRectangle> {

    private List<IntervalRange> dims;


    public NumRectangle(List<IntervalRange> dims) {

        this.dims = new ArrayList<IntervalRange>(dims);
    }


    public int getDimensionality() {
        return dims.size();
    }


    public NumRectangle intersection(NumRectangle rec) {

        List<IntervalRange> dimsN = new ArrayList<IntervalRange>();

        for (int i = 0; i < dims.size(); i++) {

            dimsN.set(i, dims.get(i).intersection(rec.dims.get(i)));
        }

        return new NumRectangle(dimsN);
    }


    public boolean contains(NumRectangle rec) {

        boolean res = true;

        for (int i = 0; i < dims.size(); i++) {

            res = res && dims.get(i).contains(rec.dims.get(i));
        }

        return res;
    }


    @Override
    public boolean equals(Object obj) {

        if (obj instanceof NumRectangle) {

            NumRectangle rec = (NumRectangle)obj;
            return this.contains(rec) && rec.contains(this);

        }

        return false;
    }


    //Tested
    public RangeLength<?> getRange(int i) {

        if (i >= dims.size()) {

            throw new IllegalArgumentException("Dimension " + i
                    + " is not valid");
        }

        return dims.get(i);

    }


    public boolean intersects(NumRectangle rec) {

        boolean res = true;

        for (int i = 0; i < dims.size(); i++) {

            res = res && dims.get(i).intersects(rec.dims.get(i));
        }

        return res;
    }


    //Tested
    // Shrink rectangle so that it does not intersect with rec
    public void shrink(NumRectangle rec) {
    //todo
    }

    public NumRectangle computeTightBox(NumRectangle rec) {

        List<IntervalRange> dimsN = new ArrayList<IntervalRange>();

        for (int i = 0; i < dims.size(); i++) {

            dimsN.set(i, dims.get(i).tightRange(rec.dims.get(i)));
        }

        return new NumRectangle(dimsN);
    }

    public void setDims(List<IntervalRange> dims) {

       this.dims = new ArrayList<IntervalRange>(dims);
    }

    public boolean hasInfinite() {

        //todo: fix this, do we need infinite for intervalRange?
        boolean res = false;

        return res;
    }


    public long getVolume() {

        long v = (long)1;

        for (IntervalRange d : dims) {

            v *= d.getLength();
        }

        return v;
    }

    public String toString() {

        String res = "rectangle:\n";

        for (int i = 0; i < dims.size(); i++) {

            res += "\tdim " + i + ":\n" + "\t\t" + dims.get(i).toString() + "\n";
        }

        return res;
    }


    public JSONObject toJSON() {
        JSONObject rectangle = new JSONObject();


        return rectangle;
    }

    public static void main(String args[] ) {

    }
}
