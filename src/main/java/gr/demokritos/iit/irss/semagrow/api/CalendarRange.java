package gr.demokritos.iit.irss.semagrow.api;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Created by efi on 16/7/2014.
 */
public class CalendarRange implements RangeLength<XMLGregorianCalendar>, Rangeable<CalendarRange> {

    private XMLGregorianCalendar begin;
    private XMLGregorianCalendar end;

    public CalendarRange(XMLGregorianCalendar begin, XMLGregorianCalendar end) {
        this.begin = begin;
        this.end = end;
    }

    public boolean contains(XMLGregorianCalendar date) {

        if (((begin.toGregorianCalendar().compareTo(
                date.toGregorianCalendar())) < 0 ) &&
                ((date.toGregorianCalendar().compareTo(
                        end.toGregorianCalendar())) < 0) ){
            return true;
        }
        return false;
    }

    public boolean contains(CalendarRange range) {

        if (((begin.toGregorianCalendar().compareTo(
                range.getBegin().toGregorianCalendar())) < 0 ) &&
                ((range.getBegin().toGregorianCalendar().compareTo(
                        end.toGregorianCalendar())) < 0) ){

            return true;
        }

        return false;
    }


    public boolean intersects(CalendarRange range) {

        CalendarRange res;
        XMLGregorianCalendar nBegin = begin;
        XMLGregorianCalendar nEnd = end;

        // if begin before range.getBegin()
        if ((begin.toGregorianCalendar().compareTo(
                range.getBegin().toGregorianCalendar())) < 0 ){

            nBegin = range.getBegin();
        } else if ((end.toGregorianCalendar().compareTo(
                range.getEnd().toGregorianCalendar())) > 0 ){

            nEnd = range.getEnd();
        }


        if (!(nBegin.toGregorianCalendar().compareTo(
                nEnd.toGregorianCalendar()) < 0)) {

            return false;
        }

        return true;
    }


    public CalendarRange tightRange(CalendarRange calendarRange) {
        XMLGregorianCalendar beginN = begin;
        XMLGregorianCalendar endN = end;

        // if begin after range.getBegin()
        if ((begin.toGregorianCalendar().compareTo(
                calendarRange.getBegin().toGregorianCalendar())) > 0 ){

            beginN = calendarRange.getBegin();
        } else if ((end.toGregorianCalendar().compareTo(
                calendarRange.getEnd().toGregorianCalendar())) < 0 ){

            endN = calendarRange.getEnd();
        }

        return new CalendarRange(beginN, endN);
    }


    public CalendarRange intersection(CalendarRange range) {

        CalendarRange res;
        XMLGregorianCalendar nBegin = begin;
        XMLGregorianCalendar nEnd = end;

        // if begin before range.getBegin()
        if ((begin.toGregorianCalendar().compareTo(
                range.getBegin().toGregorianCalendar())) < 0 ){

                nBegin = range.getBegin();
        } else if ((end.toGregorianCalendar().compareTo(
                range.getEnd().toGregorianCalendar())) > 0 ){

                nEnd = range.getEnd();
        }


        if ((nBegin.toGregorianCalendar().compareTo(
                nEnd.toGregorianCalendar()) < 0)) {

            res = new CalendarRange(nBegin, nEnd);
        } else {

            res = null;
        }

        return res;
    }


    public CalendarRange minus(CalendarRange calendarRange) {

        XMLGregorianCalendar beginN = begin;
        XMLGregorianCalendar endN = end;

        // if calendarRange.begin before begin
        if ((calendarRange.begin.toGregorianCalendar().compareTo(
                begin.toGregorianCalendar())) < 0 ) {

            beginN = calendarRange.end;
        } else if ((calendarRange.end.toGregorianCalendar().compareTo(
                end.toGregorianCalendar())) > 0 )  {

            endN = calendarRange.begin;
        }

        return new CalendarRange(beginN, endN);
    }


    public boolean isUnit() {

        return begin.toGregorianCalendar().equals(
                end.toGregorianCalendar());
    }

    public long getLength() {

        return  end.toGregorianCalendar().getTimeInMillis() -
                begin.toGregorianCalendar().getTimeInMillis();
    }

    public XMLGregorianCalendar getBegin() {
        return begin;
    }

    public XMLGregorianCalendar getEnd() {
        return end;
    }

    public String toString() {
        return "(" + begin + "-" + end + ")";
    }

}
