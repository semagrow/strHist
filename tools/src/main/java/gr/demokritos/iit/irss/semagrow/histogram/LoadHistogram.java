package gr.demokritos.iit.irss.semagrow.histogram;

import gr.demokritos.iit.irss.semagrow.config.LogConfigImpl;
import eu.semagrow.querylog.api.QueryLogException;
import gr.demokritos.iit.irss.semagrow.qfr.QueryLogReader;
import gr.demokritos.iit.irss.semagrow.stholes.STHistogramBase;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesCircleHistogram;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by kzam on 5/21/15.
 */
public class LoadHistogram {
    //private static String histDir;
    //private STHolesHistogram histogram;
    private STHolesCircleHistogram histogram;
    private LogConfigImpl config;

    private static Logger logger = LoggerFactory.getLogger(LoadHistogram.class);

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

            HistogramCUtils.serializeHistogram(histogram, config.getHistDir());
        } catch (IOException e) {
            logger.warn("IO exception", e);
        } finally {
            reader.shutdown(config.isDelete());
        }
    }

    private void loadHistogram() {
        this.histogram = HistogramCUtils.loadPreviousHistogram(config.getHistDir());
  //      logger.info("Previous histogram loaded");
    }

}
