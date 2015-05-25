package gr.demokritos.iit.irss.semagrow.config;

/**
 * Created by kzam on 5/21/15.
 */
public class LogConfigImpl implements LogConfig {
    private String logDir;
    private String histDir;
    private String qfrPrefix;
    private boolean delete = false;

    @Override
    public String getLogDir() {
        return this.logDir;
    }

    @Override
    public void setLogDir(String dir) {
        this.logDir = dir;
    }

    @Override
    public String getHistDir() {
        return this.histDir;
    }

    @Override
    public void setHistDir(String dir) {
        this.histDir = dir;
    }

    @Override
    public String getQfrPrefix() { return this.qfrPrefix; }

    @Override
    public void setQfrPrefix(String qfrPrefix) { this.qfrPrefix = qfrPrefix; }

    @Override
    public boolean isDelete() { return this.delete; }

    @Override
    public void setDelete(boolean delete) { this.delete = delete; }
}

