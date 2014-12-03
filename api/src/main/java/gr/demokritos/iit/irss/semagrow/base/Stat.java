package gr.demokritos.iit.irss.semagrow.base;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by angel on 7/15/14.
 */
public class Stat {

    public static final Stat emptyStat = new Stat();

    private Long frequency;

    private List<Long> distinctCount;

    public Stat(Long frequency, List<Long> distinctCount) {
        this.distinctCount = distinctCount;
        this.frequency = frequency;
    }

    public Stat(Stat original) {
        this.frequency = original.getFrequency();
        this.distinctCount = original.getDistinctCount();
    }

    /**
     * Default Constructor
     */
	public Stat() {
		setDistinctCount(new ArrayList<Long>());
		getDistinctCount().add((long)0);
		getDistinctCount().add((long)0);
		getDistinctCount().add((long)0);
		setFrequency((long)0);
	}

    public Long getFrequency() {
        return frequency;
    }

    public void setFrequency(Long frequency) {
        this.frequency = frequency;
    }

    public List<Long> getDistinctCount() {
        return distinctCount;
    }

    public void setDistinctCount(List<Long> distinctCount) {
        this.distinctCount = distinctCount;
    }

    public Double getDensity() {
        Long d = (long)1;
        for (Long l : getDistinctCount())
            if (l != null)
                d *= l;

        if (d != 0)
            return ((double)getFrequency()) / d;
        else
            return (double)0;
    }

    public String toString() {

        String res = "";

        res += "(";
        res += frequency.toString() + ",";
        res += "{";
        boolean comma = false;
        for (Long d : distinctCount) {
            if (comma)
                res += ",";

            res += d.toString();
            comma = true;
        }
        res +="}";
        res += ")";

        return res;

    }


    public static void main(String [] args){

        long frequency = 42;
        List<Long> distinct = new ArrayList<Long>();
        distinct.add((long)10);
        distinct.add((long)20);
        distinct.add((long)30);
        Stat statistics = new Stat(frequency, distinct);
        System.out.println(statistics);
    }
}
