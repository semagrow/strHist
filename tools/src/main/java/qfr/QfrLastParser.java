package qfr;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by kzam on 5/21/15.
 */
public class QfrLastParser {
    private static final String logFile = "/var/tmp/log/lastQfr.txt";
    private static BufferedReader reader = null;

    public long getTimestamp() throws IOException {

        return existFile() ? Long.parseLong(reader.readLine()) : 0;

    }

    private boolean existFile() {
        try {
            reader = new BufferedReader(new BufferedReader(new FileReader(logFile)));

            return true;

        } catch (FileNotFoundException e) {
            return false;
        }

    }
}
