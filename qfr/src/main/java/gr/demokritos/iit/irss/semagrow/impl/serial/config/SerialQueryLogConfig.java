package gr.demokritos.iit.irss.semagrow.impl.serial.config;

import eu.semagrow.querylog.config.QueryLogConfig;

/**
 * Created by angel on 31/7/2015.
 */
public class SerialQueryLogConfig implements QueryLogConfig {


    public String getFilename() {
        return null;
    }


    public void setFilename(String s) {

    }


    public boolean rotate() {
        return false;
    }


    public void setCounter(int i) {

    }


    public int getCounter() {
        return 0;
    }
}
