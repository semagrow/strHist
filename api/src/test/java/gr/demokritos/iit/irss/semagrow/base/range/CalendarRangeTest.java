package gr.demokritos.iit.irss.semagrow.base.range;

import gr.demokritos.iit.irss.semagrow.base.range.CalendarRange;
import junit.framework.*;

import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

import java.lang.Override;

public class CalendarRangeTest extends TestCase {

    protected DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    protected CalendarRange cr;

    @Override
    protected void setUp() throws ParseException {
        Date dateBegin = format.parse("2011-10-07T08:51:52.006Z");
        Date dateEnd = format.parse("2011-10-09T08:51:52.006Z");
        cr = new CalendarRange(dateBegin, dateEnd);
    }

    public void testIncludes1() throws Exception, ParseException {
        Date date = format.parse("2009-10-07T08:51:52.006Z");
        assertFalse(cr.includes(date));
    }

    public void testIncludes2() throws Exception {
        Date date = format.parse("2011-10-08T08:51:52.006Z");
        assertTrue(cr.includes(date));
    }

    public void testContains1() throws Exception {
        Date dateBegin = format.parse("2010-10-07T08:51:52.006Z");
        Date dateEnd = format.parse("2012-10-09T08:51:52.006Z");
        CalendarRange cr2 = new CalendarRange(dateBegin, dateEnd);

        assertFalse(cr.contains(cr2));
        assertTrue(cr2.contains(cr));
    }

    public void testContains2() throws Exception {
        Date dateBegin = format.parse("2011-10-08T08:51:52.006Z");
        Date dateEnd = format.parse("2012-10-09T08:51:52.006Z");
        CalendarRange cr2 = new CalendarRange(dateBegin, dateEnd);

        assertFalse(cr.contains(cr2));
        assertFalse(cr2.contains(cr));
    }

    public void testIntersects1() throws Exception {
        Date dateBegin = format.parse("2010-10-08T08:51:52.006Z");
        Date dateEnd = format.parse("2012-10-09T08:51:52.006Z");
        CalendarRange cr2 = new CalendarRange(dateBegin, dateEnd);

        assertTrue(cr.intersects(cr2));
    }

    public void testIntersects2() throws Exception {
        Date dateBegin = format.parse("2009-10-08T08:51:52.006Z");
        Date dateEnd = format.parse("2010-10-09T08:51:52.006Z");
        CalendarRange cr2 = new CalendarRange(dateBegin, dateEnd);

        assertFalse(cr.intersects(cr2));
    }

    public void testTightRange1() throws Exception {
        Date dateBegin = format.parse("2009-10-08T08:51:52.006Z");
        Date dateEnd = format.parse("2010-10-09T08:51:52.006Z");
        CalendarRange cr2 = new CalendarRange(dateBegin, dateEnd);

        CalendarRange tightRange = cr.tightRange(cr2);

        Date dateBegin2 = dateBegin;
        Date dateEnd2 = format.parse("2011-10-09T08:51:52.006Z");
        assertTrue(tightRange.equals(new CalendarRange(dateBegin2, dateEnd2)));
    }

    public void testExpand() throws Exception {
        Date date = format.parse("2012-10-08T08:51:52.006Z");
        cr.expand(date);

        Date dateBegin = cr.getBegin();
        Date dateEnd = date;
        CalendarRange cr2 = new CalendarRange(dateBegin, dateEnd);

        assertTrue(cr.equals(cr2));
    }

    /**
     * Full Intersection
     * @throws Exception
     */
    public void testIntersection1() throws Exception {
        Date dateBegin = format.parse("2009-10-08T08:51:52.006Z");
        Date dateEnd = format.parse("2012-10-09T08:51:52.006Z");
        CalendarRange cr2 = new CalendarRange(dateBegin, dateEnd);

        CalendarRange intersection = cr.intersection(cr2);

        assertTrue(intersection.equals(cr));
    }

    /**
     * Full Intersection
     * @throws Exception
     */
    public void testIntersection2() throws Exception {
        Date dateBegin = format.parse("2009-10-08T08:51:52.006Z");
        Date dateEnd = format.parse("2012-10-09T08:51:52.006Z");
        CalendarRange cr2 = new CalendarRange(dateBegin, dateEnd);

        CalendarRange intersection = cr2.intersection(cr);

        assertTrue(intersection.equals(cr));
    }

    /**
     * Partial Intersection
     * @throws Exception
     */
    public void testIntersection3() throws Exception {
        Date dateBegin = format.parse("2009-10-08T08:51:52.006Z");
        Date dateEnd = format.parse("2011-10-08T08:51:52.006Z");
        CalendarRange cr2 = new CalendarRange(dateBegin, dateEnd);

        CalendarRange intersection = cr.intersection(cr2);

        Date dateBegin2 = cr.getBegin();
        Date dateEnd2 = dateEnd;
        CalendarRange cr3 = new CalendarRange(dateBegin2, dateEnd2);

        assertTrue(intersection.equals(cr3));
    }

    public void testMinus() throws Exception {

    }

    public void testIsUnit() throws Exception {

    }

    public void testIsEmpty() throws Exception {

    }

    public void testGetLength() throws Exception {

    }
}