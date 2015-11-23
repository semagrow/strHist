package gr.demokritos.iit.irss.semagrow.rdf;

import gr.demokritos.iit.irss.semagrow.base.range.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesBucket;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by angel on 7/15/14.
 */
public class RDFSTHolesHistogram extends STHolesHistogram<RDFRectangle> {

    public RDFSTHolesHistogram() {
        super();
    }

    public RDFSTHolesHistogram(STHolesBucket root) {super(root);}


    @Override
    protected RDFRectangle getRectangle(RDFRectangle r) {
        //my new r

        String newStr = getMainSubject(r.getRange(0).toString());
        Collection<String> col = new LinkedList<String>();
        newStr = newStr.replaceAll("\"","");
        col.add(newStr);
        //System.out.println(newStr);

        RDFRectangle newR = new RDFRectangle(new RDFURIRange(col),
                (ExplicitSetRange<URI>) r.getRange(1),
                (RDFValueRange) r.getRange(2));

        //System.out.println("0 = "+newR.getRange(0) + " 1 ="+newR.getRange(1)+ " 2="+newR.getRange(2));
        return newR;
    }

    @Override
    protected String getSubject(RDFRectangle r) {

        return r.getSubjectRange().getPrefixList().get(0);
    }

    private String getMainSubject(String str) {
        //String str = sVal.stringValue();
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
