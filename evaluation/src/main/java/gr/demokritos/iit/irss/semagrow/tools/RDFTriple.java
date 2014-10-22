package gr.demokritos.iit.irss.semagrow.tools;

/**
 * Created by Nick on 13-Aug-14.
 */
public class RDFTriple {

    private String subject, predicate, object;

    public RDFTriple(String subject, String predicate, String object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }


    public String getSubject() {
        return subject;
    }

    public String getPredicate() {
        return predicate;
    }

    public String getObject() {
        return object;
    }

}
