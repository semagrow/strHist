package gr.demokritos.iit.irss.semagrow.config;

/**
 * Created by kzam on 5/21/15.
 */
public class LogConfigImpl implements LogConfig {

    private String logDir;

    private String histDir;

    private String qfrPrefix;

    private boolean delete = false;


    public String getLogDir() {
        return this.logDir;
    }


    public void setLogDir(String dir) {
        this.logDir = dir;
    }


    public String getHistDir() {
        return this.histDir;
    }


    public void setHistDir(String dir) {
        this.histDir = dir;
    }


    public String getQfrPrefix() { return this.qfrPrefix; }


    public void setQfrPrefix(String qfrPrefix) { this.qfrPrefix = qfrPrefix; }


    public boolean isDelete() { return this.delete; }


    public void setDelete(boolean delete) { this.delete = delete; }

}

