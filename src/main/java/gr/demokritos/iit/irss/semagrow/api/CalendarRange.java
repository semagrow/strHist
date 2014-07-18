package gr.demokritos.iit.irss.semagrow.api;

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

        if (((begin.compareTo(date)) < 0 ) &&
                ((date.compareTo(end)) < 0) ){
            return true;
        }
        return false;
    }

    public boolean contains(CalendarRange range) {

        if (((begin.compareTo(range.getBegin())) < 0 ) &&
                ((range.getBegin().compareTo(end)) < 0) ){

            return true;
        }

        return false;
    }


    public boolean intersects(CalendarRange range) {

        CalendarRange res;
        Date nBegin = begin;
        Date nEnd = end;

        // if begin before range.getBegin()
        if ((begin.compareTo(
                range.getBegin())) < 0 ){

            nBegin = range.getBegin();
        } else if ((end.compareTo(range.getEnd())) > 0 ){

            nEnd = range.getEnd();
        }


        if (!(nBegin.compareTo(nEnd) < 0)) {

            return false;
        }

        return true;
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
        return "(" + begin + "-" + end + ")";
    }

}
