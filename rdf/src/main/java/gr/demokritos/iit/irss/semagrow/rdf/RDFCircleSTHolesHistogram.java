package gr.demokritos.iit.irss.semagrow.rdf;

import gr.demokritos.iit.irss.semagrow.base.range.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesBucket;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesCircleHistogram;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import org.openrdf.model.URI;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by katerina on 23/11/2015.
 */
public class RDFCircleSTHolesHistogram extends STHolesCircleHistogram<RDFCircle> {

    public RDFCircleSTHolesHistogram() {
        super();
    }

    public RDFCircleSTHolesHistogram(STHolesBucket root) {super(root);}


    @Override
    protected RDFCircle getRectangle(RDFCircle r) {
        //my new r

        String newStr = getMainSubject(r.getRange(0).toString());
        newStr = newStr.replaceAll("\"","");

        RDFCircle newR = new RDFCircle(new RDFStrRange(newStr),
                (ExplicitSetRange<URI>) r.getRange(1),
                (RDFValueRange) r.getRange(2));

        return newR;
    }

    @Override
    protected String getSubject(RDFCircle r) {

        return r.getSubjectRange().getCenter();
    }


    private String getMainSubject(String str) {
        int count = 0;

        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                count++;

                if (count == 5) {
                    return str.substring(0, i);
                }
            }
        }
        return str;
    }
}
