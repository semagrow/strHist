package gr.demokritos.iit.irss.semagrow.base.range;

import gr.demokritos.iit.irss.semagrow.api.range.Range;
import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.api.range.Rangeable;
import org.simmetrics.metrics.JaroWinkler;

/**
 * Created by katerina on 20/11/2015.
 */
public class CircleRange implements RangeLength<String>, Rangeable<CircleRange> {

    private JaroWinkler strMetric = new JaroWinkler();

    private double radius;

    private String center;

    private boolean infinite = false;

    private long count = 1; /* how many points are within this range*/

    /* only for root */
    public CircleRange() {
        center = "";
        radius = 1.0;
        infinite = true;
    }

    public CircleRange(String center) {
        this.center = center;
        radius = 0.0;

        infinite = false;
    }

    public CircleRange(String center, double radius) {
        this.center = center;
        this.radius = radius;
        infinite = false;
    }

    public double getRadius() { return this.radius; }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public String getCenter() { return this.center; }

    public void setCenter(String center) {
        this.center = center;
    }

    public void addLength(long count) {
        this.count = this.count + count;
    }

    public void setLength(long count) {
        this.count = count;
    }

    /* ?????????????????????? */
    @Override
    public long getLength() {
        return count;
    }

    @Override
    public boolean isUnit() {
        return (count >= 1);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean includes(String elem) {
        if ((1.0 - strMetric.distance(elem, center)) <= radius)
            return true;

        return false;
    }

    @Override
    public boolean contains(CircleRange circleRange) {
        if (isInfinite())
            return true;

        if (this.getCenter() == null || this.getCenter().equalsIgnoreCase(""))
            return true;

        double cDist = 1.0 - strMetric.distance(circleRange.getCenter(), this.center);

        if (cDist + circleRange.getRadius() <= this.radius)
            return true;

        if (this.radius == 0.0 || circleRange.getRadius() == 0.0)
            return false;

        return false;
    }

    @Override
    public boolean intersects(CircleRange circleRange) {
        if (isInfinite())
            return true;

        if (this.contains(circleRange))
            return true;


        double cDist = 1.0 - strMetric.distance(circleRange.getCenter(), this.center);
        //double cDist = strMetric.getSimilarity(circleRange.getCenter(), this.center);

        if (cDist == 0.0)
            return true;

        if (this.getRadius() == 0.0 && circleRange.getRadius() == 0.0)
            return false;

        if (circleRange.getRadius() + this.radius > cDist)
            return true;

        return false;
    }

    @Override
    public void expand(String v) {
        if (!this.includes(v)) {
            double cDist = 1.0 - strMetric.distance(v, this.center);
            this.radius = cDist;
        }
        count++;
    }

    @Override
    public CircleRange intersection(CircleRange circleRange) {
        if (this.contains(circleRange))
            return circleRange;
        else if (circleRange.contains(this))
            return this;

        else {
            double cDist = 1.0 - strMetric.distance(circleRange.getCenter(), this.center);
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
            double cDist = 1.0 - strMetric.distance(circleRange.getCenter(), this.center);
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
            double cDist = 1.0 - strMetric.distance(circleRange.getCenter(), this.center);
            if (this.radius > circleRange.getRadius()) {
                CircleRange c = new CircleRange(this.center, cDist);
                c.setLength(this.getLength() + circleRange.getLength());

                return c;
            }
            else {
                CircleRange c = new CircleRange(circleRange.getCenter(), cDist);
                c.setLength(this.getLength() + circleRange.getLength());

                return c;
            }
        }

        if (this.radius == 0.0 || circleRange.getRadius() == 0.0) {
            double cDist = 1.0 - strMetric.distance(circleRange.getCenter(), this.center);

            if (this.radius == 0.0) {
                CircleRange c = new CircleRange(circleRange.getCenter(), cDist);
                c.setLength(circleRange.getLength() + 1);

                return c;
            }
            else {
                CircleRange c = new CircleRange(this.getCenter(), cDist);
                c.setLength(this.count + 1);

                return c;
            }
        }
        else {
            double cDist = 1.0 - strMetric.distance(circleRange.getCenter(), this.center);
            CircleRange c = new CircleRange(this.getCenter(), cDist+this.radius);
            c.setLength(this.count + circleRange.getLength());

            return c;
        }

    }

    //Tested
    public String toString() {

        String res="";

        if (center.equalsIgnoreCase("") || center == null)
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
        return this.infinite;
    }




    public boolean intersects(Range<?> r) {
        if (r instanceof CircleRange) {
            return intersects((CircleRange)r);
        }
        return false;
    }
}
