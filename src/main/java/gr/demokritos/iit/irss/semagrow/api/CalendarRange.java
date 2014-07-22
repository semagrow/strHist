package gr.demokritos.iit.irss.semagrow.api;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Created by efi on 16/7/2014.
 */
public class CalendarRange implements RangeLength<Date>, Rangeable<CalendarRange> {

    private Date begin;
    private Date end;

    public CalendarRange(Date begin, Date end) {
        this.begin = begin;
        this.end = end;
    }

    //Tested
    public boolean contains(Date date) {

        return ((begin.compareTo(date)) <= 0) &&
                ((date.compareTo(end)) <= 0);
    }

    //Tested
    public boolean contains(CalendarRange range) {

        return ((begin.compareTo(range.getBegin())) <= 0) &&
                ((range.getEnd().compareTo(end)) <= 0);

    }

    //Tested
    public boolean intersects(CalendarRange range) {


        Date nBegin = begin;
        Date nEnd = end;

        // if begin before range.getBegin()
        if ((begin.compareTo(range.getBegin())) < 0 ){

            nBegin = range.getBegin();
        } else if ((end.compareTo(range.getEnd())) > 0 ){

            nEnd = range.getEnd();
        }


        return nBegin.compareTo(nEnd) <= 0;

    }


    //Tested
    public CalendarRange tightRange(CalendarRange calendarRange) {
        Date beginN = begin;
        Date endN = end;

        // if begin after range.getBegin()
        if ((begin.compareTo(calendarRange.getBegin())) > 0 ){

            beginN = calendarRange.getBegin();
        }

        if ((end.compareTo(calendarRange.getEnd())) < 0 ){

            endN = calendarRange.getEnd();
        }

        return new CalendarRange(beginN, endN);
    }


    public void expand(String v) {

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date date = null;

        try {date = format.parse(v);}
        catch (ParseException e) {e.printStackTrace();}

        if (date != null) {

            if (date.compareTo(begin) < 0) begin = date;
            else if (date.compareTo(end) > 0) end = date;
        } else
            System.err.println("Date Format Error.");
    }

    //Tested
    public CalendarRange intersection(CalendarRange range) {

        CalendarRange res;
        Date nBegin = begin;
        Date nEnd = end;

        // if begin before range.getBegin()
        if ((begin.compareTo(range.getBegin())) < 0 ){

                nBegin = range.getBegin();
        } else if ((end.compareTo(range.getEnd())) > 0 ){

                nEnd = range.getEnd();
        }


        if ((nBegin.compareTo(nEnd) <= 0)) {

            res = new CalendarRange(nBegin, nEnd);
        } else {

            res = null;
        }

        return res;
    }


    //Tested
    public CalendarRange minus(CalendarRange calendarRange) {

        Date beginN = begin;
        Date endN = end;

        Date dummyDate = new Date();

        //Scenario 1: participant contains bucket
        // in this dimension
        if (calendarRange.contains(this)) {
            System.out.println("boom");
            return new CalendarRange(dummyDate, dummyDate);
        }
        //Scenario 2: bucket encloses participant range
        // in this dimension
        if (this.contains(calendarRange)) {

            long candidate1 = calendarRange.begin.getTime() - begin.getTime();
            long candidate2 = end.getTime() - calendarRange.end.getTime();

            if (candidate1 >= candidate2) {

                return new CalendarRange(begin, calendarRange.begin);
            } else {

                return new CalendarRange(calendarRange.end, end);
            }

        }
        //Scenario 3: default case
        // if calendarRange.begin before begin
        if ((calendarRange.begin.compareTo(begin)) < 0 ) {

            beginN = calendarRange.end;
        } else if ((calendarRange.end.compareTo(end)) > 0 )  {

            endN = calendarRange.begin;
        }

        return new CalendarRange(beginN, endN);
    }


    public boolean isUnit() {

        return begin.equals(end);
    }

    //Tested
    public long getLength() {
    	Calendar calendarBegin = new GregorianCalendar();
    	calendarBegin.setTime(begin);
    	
    	Calendar calendarEnd = new GregorianCalendar();
    	calendarBegin.setTime(end);   	

        return calendarEnd.getTimeInMillis() - calendarBegin.getTimeInMillis();
    }

    //Tested
    public Date getBegin() {
        return begin;
    }
    //Tested
    public Date getEnd() {
        return end;
    }

    //Tested
    public String toString() {

        return "calendarRange: " + begin + "-" + end + "\n";

    }

    public static void main(String args[] ) throws ParseException {

        DateFormat format = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );
        Date       dateBegin = format.parse( "2011-10-07T08:51:52.006Z" );
        Date       dateEnd = format.parse( "2011-10-09T08:51:52.006Z" );
        CalendarRange range1 = new CalendarRange(dateBegin, dateEnd);
        System.out.println(range1);

        //Test intersection
        Date dateBegin2 =  format.parse( "2011-10-06T08:51:52.006Z" );
        Date dateEnd2 = format.parse( "2011-10-10T08:51:52.006Z" );
        CalendarRange range2 = new CalendarRange(dateBegin2, dateEnd2);

        if (range1.intersects(range2)) {
            System.out.println(range1 + " intersection with " + range2
                    + " = " + range1.intersection(range2));
        }

        //Test tight range
        System.out.println("Tight range of " + range1 + " and " + range2
                + " = " + range1.tightRange(range2));

        //Test minus
        Date dateBegin3 =  format.parse( "2011-10-05T08:51:52.006Z" );
        Date dateEnd3 = format.parse( "2011-10-09T07:51:52.006Z" );
        CalendarRange range3 = new CalendarRange(dateBegin3, dateEnd3);
        System.out.println(range2 + " minus " + range3 + " = " +
                range2.minus(range3));
        Date dateBegin4 =  format.parse( "2011-10-04T08:51:52.006Z" );
        Date dateEnd4 = format.parse( "2011-10-12T07:51:52.006Z" );
        CalendarRange range4 = new CalendarRange(dateBegin4, dateEnd4);
        System.out.println(range4 + " minus " + range3 + " = " +
                range4.minus(range3));

    }

}
