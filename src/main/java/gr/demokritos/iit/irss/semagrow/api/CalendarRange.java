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

    public boolean contains(Date date) {

        return ((begin.compareTo(date)) < 0) &&
                ((date.compareTo(end)) < 0);
    }

    public boolean contains(CalendarRange range) {

        return ((begin.compareTo(range.getBegin())) < 0) &&
                ((range.getBegin().compareTo(end)) < 0);

    }


    public boolean intersects(CalendarRange range) {


        Date nBegin = begin;
        Date nEnd = end;

        // if begin before range.getBegin()
        if ((begin.compareTo(range.getBegin())) < 0 ){

            nBegin = range.getBegin();
        } else if ((end.compareTo(range.getEnd())) > 0 ){

            nEnd = range.getEnd();
        }


        return nBegin.compareTo(nEnd) < 0;

    }


    public CalendarRange tightRange(CalendarRange calendarRange) {
        Date beginN = begin;
        Date endN = end;

        // if begin after range.getBegin()
        if ((begin.compareTo(calendarRange.getBegin())) > 0 ){

            beginN = calendarRange.getBegin();
        } else if ((end.compareTo(calendarRange.getEnd())) < 0 ){

            endN = calendarRange.getEnd();
        }

        return new CalendarRange(beginN, endN);
    }


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


        if ((nBegin.compareTo(nEnd) < 0)) {

            res = new CalendarRange(nBegin, nEnd);
        } else {

            res = null;
        }

        return res;
    }


    public CalendarRange minus(CalendarRange calendarRange) {

        Date beginN = begin;
        Date endN = end;

        Date dummyDate = new Date();

        //Scenario 1: participant contains bucket
        // in this dimension
        if (calendarRange.contains(this)) {

            return new CalendarRange(dummyDate, dummyDate);
        }
        //Scenario 2: bucket encloses participant range
        // in this dimension
        if (this.contains(calendarRange)) {

            long candidate1 = calendarRange.begin.getTime() - begin.getTime();
            long candidate2 = end.getTime() - calendarRange.end.getTime();

            if (candidate1 > candidate2) {

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

    public long getLength() {
    	Calendar calendarBegin = new GregorianCalendar();
    	calendarBegin.setTime(begin);
    	
    	Calendar calendarEnd = new GregorianCalendar();
    	calendarBegin.setTime(end);   	

        return calendarEnd.getTimeInMillis() - calendarBegin.getTimeInMillis();
    }

    public Date getBegin() {
        return begin;
    }

    public Date getEnd() {
        return end;
    }

    public String toString() {

        return "calendarRange: " + begin + "-" + end + "\n";

    }

    public static void main(String args[] ) throws ParseException {

        DateFormat format = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );
        Date       dateBegin = format.parse( "2011-10-07T08:51:52.006Z" );
        Date       dateEnd = format.parse( "2011-10-09T08:51:52.006Z" );
        CalendarRange range1 = new CalendarRange(dateBegin, dateEnd);
        System.out.println(range1);
    }

}
