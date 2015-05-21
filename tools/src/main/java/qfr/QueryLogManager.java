package qfr;

import exception.IntegrationException;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by kzam on 5/21/15.
 */
public class QueryLogManager {
    private static String baseDir;
    private static String filePrefix;
    private File[] foundFiles;

    public QueryLogManager(String baseDir, String filePrefix) {
        this.baseDir = baseDir;
        this.filePrefix = filePrefix + ".";
    }

    /**
     * get all files with a specific prefix from a directory
     * the files that have already been used, are been removed
     * as well as the current file that semagrow uses
     * @return an array with the files
     */

    public File[] getQfrFiles() {
        File folder = new File(baseDir);

        foundFiles = folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(filePrefix);
            }
        });

        getNameSorted();

        try {
            getUncheckedFiles();

        } catch (IntegrationException e) {
            new IntegrationException(e);
        } finally {
            removeLastQfr();

            return foundFiles;
        }

    }

    private void removeLastQfr() {
        // do not take into consideration the last file
        // it is used from semagrow
        foundFiles = (File[]) ArrayUtils.removeElement(foundFiles, foundFiles[foundFiles.length - 1]);
    }

    private File getLastModified() {
        File file = null;
        long max = 0;

        for(int i=0; i<foundFiles.length; i++) {

            if(foundFiles[i].lastModified() > max) {
                max = foundFiles[i].lastModified();
                file = foundFiles[i];
            }
        }
        return file;
    }

    private void getNameSorted() {
        Arrays.sort(foundFiles, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f1.getName().split(filePrefix)[1]).compareTo(Long.valueOf(f2.getName().split(filePrefix)[1]));
                //return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });

    }

    private void getUncheckedFiles() throws IntegrationException {
        QfrLastParser parser = new QfrLastParser();
        File[] temp = foundFiles;

        try {
            long timestamp = parser.getTimestamp();

            for(int i=0; i<temp.length - 1; i++) {
                if(temp[i].lastModified() <= timestamp) {
                    foundFiles = (File[]) ArrayUtils.removeElement(foundFiles, temp[i]);
                }

            }

        } catch (IOException e) {
            throw new IntegrationException(e);
        }

    }


}
