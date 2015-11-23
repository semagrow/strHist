package gr.demokritos.iit.irss.semagrow.base.range;

import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.api.range.Rangeable;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;

/**
 * Created by katerina on 20/11/2015.
 */
public class CircleRange implements RangeLength<String>, Rangeable<CircleRange> {

    private JaroWinkler strMetric = new JaroWinkler();

    private double radius;

    private String center;

    private long count = 1; /* how many points are within this range*/

    public CircleRange(String center) {
        this.center = center;
        radius = 0.0;
    }

    public CircleRange(String center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    public double getRadius() { return this.radius; }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public String getCenter() { return this.center; }

    public void setCenter(String center) {
        this.center = center;
    }

    /* ?????????????????????? */
    @Override
    public long getLength() {
        return count;
    }

    @Override
    public boolean isUnit() {
        return (count == 1);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean includes(String elem) {
        if (strMetric.getSimilarity(elem, center) <= radius)
            return true;

        return false;
    }

    @Override
    public boolean contains(CircleRange circleRange) {
        double cDist = strMetric.getSimilarity(circleRange.getCenter(), this.center);

        if (cDist + circleRange.getRadius() <= this.radius)
            return true;

        return false;
    }

    @Override
    public boolean intersects(CircleRange circleRange) {
        if (this.contains(circleRange))
            return true;

        double cDist = strMetric.getSimilarity(circleRange.getCenter(), this.center);

        if (circleRange.getRadius() + this.radius > cDist)
            return true;

        return false;
    }

    @Override
    public void expand(String v) {
        if (this.includes(v))
            count++;
    }

    @Override
    public CircleRange intersection(CircleRange circleRange) {
        if (this.contains(circleRange))
            return circleRange;
        else if (circleRange.contains(this))
            return this;

        else {
            double cDist = strMetric.getSimilarity(circleRange.getCenter(), this.center);
            return new CircleRange("", Math.max((circleRange.getRadius() + this.radius - cDist) , 0));
        }
    }

    @Override
    public CircleRange minus(CircleRange circleRange) {
        if (this.contains(circleRange))
            return new CircleRange("", (this.radius - circleRange.getRadius()));
        else if (circleRange.contains(this))
            return new CircleRange("", (circleRange.getRadius() - this.radius));

        else {
            double cDist = strMetric.getSimilarity(circleRange.getCenter(), this.center);
            return new CircleRange("", ( this.getRadius() - Math.max((circleRange.getRadius() + this.radius - cDist) , 0)) );
        }
    }



    @Override
    public CircleRange tightRange(CircleRange circleRange) {
        if (this.contains(circleRange))
            return this;
        else if (circleRange.contains(this))
            return circleRange;

        else if (this.intersects(circleRange)) {
            double cDist = strMetric.getSimilarity(circleRange.getCenter(), this.center);
            if (this.radius > circleRange.getRadius())
                return new CircleRange(this.center, cDist);
            else
                return new CircleRange(circleRange.getCenter(), cDist);
        }

        return null;
    }

    //Tested
    public String toString() {

        String res="";

        if (center.equalsIgnoreCase(""))
            res += "{ center = null";
        else
            res += "{ center = " + center;

        res += res + " , radius = " + radius + " }";

        return res;
    }

    public boolean equals(Object o) {
        if (this == o) return true;

        if (o instanceof CircleRange) {
            CircleRange range = (CircleRange)o;

            return( (this.center == range.center) && (this.radius == range.radius) );

        }

        return false;
    }

    public boolean isInfinite() {
        return infinite;
    }
}
