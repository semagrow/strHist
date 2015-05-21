package histogram;

import config.LogConfigImpl;
import exception.LogException;
import gr.demokritos.iit.irss.semagrow.api.QueryLogException;
import qfr.QueryLogReader;
import gr.demokritos.iit.irss.semagrow.stholes.STHistogramBase;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import log.LogWriterImpl;

import java.io.File;
import java.io.IOException;

/**
 * Created by kzam on 5/21/15.
 */
public class LoadHistogram {
    //private static String histDir;
    private STHolesHistogram histogram;
    private static LogConfigImpl config;

    public LoadHistogram(LogConfigImpl config) {
        this.config = config;
    }

    /**
     * refine the histogram with the results that are included into the file
     * @param file
     */
    public void refine(File file) throws ArrayIndexOutOfBoundsException, java.lang.ClassCastException, QueryLogException {
        loadHistogram();

        QueryLogReader reader = new QueryLogReader(file);
        try {
            reader.readData();

            ((STHistogramBase) this.histogram).refine(reader.adaption(config.getLogDir()).iterator());

            HistogramUtils.serializeHistogram(histogram, config.getHistDir());

        } catch (IOException e) {
            new LogException(e);

        } finally {
            reader.shutdown(config.isDelete());
        }
    }

    private void loadHistogram() {
        this.histogram = HistogramUtils.loadPreviousHistogram(config.getHistDir());
        LogWriterImpl.getInstance().write("Previous histogram loaded");
    }

}
