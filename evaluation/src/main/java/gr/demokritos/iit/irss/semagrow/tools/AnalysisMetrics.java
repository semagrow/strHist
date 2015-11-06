package gr.demokritos.iit.irss.semagrow.tools;


/**
 * Created by katerina on 29/9/2015.
 */
public class AnalysisMetrics {

    private long actual_results;
    private long estimate_results;
    private long actual_cardinality;
    private long estimate_cardinality;
    private double error;
    private boolean equal_plan;
    private double actual_cpu_cost;
    private double actual_network_cost;
    private double estimate_cpu_cost;
    private double estimate_network_cost;
    private double actual_execution_time;
    private double estimate_execution_time;

    public long getActual_results() {
        return actual_results;
    }

    public void setActual_results(long actual_results) {
        this.actual_results = actual_results;
    }

    public long getEstimate_results() {
        return estimate_results;
    }

    public void setEstimate_results(long estimate_results) {
        this.estimate_results = estimate_results;
    }

    public long getActual_cardinality() {
        return actual_cardinality;
    }

    public void setActual_cardinality(long actual_cardinality) {
        this.actual_cardinality = actual_cardinality;
    }

    public long getEstimate_cardinality() {
        return estimate_cardinality;
    }

    public void setEstimate_cardinality(long estimate_cardinality) {
        this.estimate_cardinality = estimate_cardinality;
    }

    public double getError() {
        return error;
    }

    public void setError(double error) {
        this.error = error;
    }

    public boolean isEqual_plan() {
        return equal_plan;
    }

    public void setEqual_plan(boolean equal_plan) {
        this.equal_plan = equal_plan;
    }

    public double getActual_cpu_cost() {
        return actual_cpu_cost;
    }

    public void setActual_cpu_cost(double cpu_cost) {
        this.actual_cpu_cost = cpu_cost;
    }

    public double getActual_network_cost() {
        return actual_network_cost;
    }

    public void setActual_network_cost(double network_cost) {
        this.actual_network_cost = network_cost;
    }

    public double getEstimate_cpu_cost() {
        return estimate_cpu_cost;
    }

    public void setEstimate_cpu_cost(double cpu_cost) {
        this.estimate_cpu_cost = cpu_cost;
    }

    public double getEstimate_network_cost() {
        return estimate_network_cost;
    }

    public void setEstimate_network_cost(double network_cost) {
        this.estimate_network_cost = network_cost;
    }

    public double getActual_execution_time() {
        return actual_execution_time;
    }

    public void setActual_execution_time(double execution_time) {
        this.actual_execution_time = execution_time;
    }

    public double getEstimate_execution_time() {
        return estimate_execution_time;
    }

    public void setEstimate_execution_time(double execution_time) {
        this.estimate_execution_time = execution_time;
    }

    @Override
    public String toString() {
        String str = "Actual Results = "+actual_results + "\nHist Results = "+estimate_results+"\nError = "+error+"%\n"+
                "Equality of plans = "+equal_plan+"\nActual plan cpu cost = "+actual_cpu_cost+"\nHist cpu cost = "+estimate_cpu_cost+
                "\nActual plan cardinality = "+actual_cardinality+"\nHist plan cardinality = "+estimate_cardinality+"\n"+
                "Actual execution time = "+actual_execution_time+"\nHist execution time = "+estimate_execution_time;

        return str;
    }

    public void initialize() {
        actual_results = 0;
        estimate_results = 0;
        actual_cardinality = 0;
        estimate_cardinality = 0;
        error = 0.0;
        equal_plan = false;
        actual_cpu_cost = 0.0;
        actual_network_cost = 0.0;
        estimate_cpu_cost = 0.0;
        estimate_network_cost = 0.0;
        actual_execution_time = 0.0;
        estimate_execution_time = 0.0;
    }
}
