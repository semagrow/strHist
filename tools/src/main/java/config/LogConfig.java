package config;

/**
 * Created by kzam on 5/21/15.
 */
public interface LogConfig {
    String getLogDir();

    void setLogDir(String dir);

    String getHistDir();

    void setHistDir(String dir);

    String getQfrPrefix();

    void setQfrPrefix(String qfrPrefix);

    boolean isDelete();

    void setDelete(boolean delete);
}
