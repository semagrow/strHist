package gr.demokritos.iit.irss.semagrow.tools.deprecated;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by Nick on 10-Aug-14.
 */
public class CustomCollection<E> implements Iterable<E> {

    private String poolPath;

    public CustomCollection(String poolPath) {
        this.poolPath = poolPath;
    }

    @Override
    public Iterator<E> iterator() {
        return new CustomIterator<E>(poolPath);
    }

    protected class CustomIterator<T> implements Iterator<T> {

        private ArrayList<File> files;
        private String poolPath;


        public CustomIterator(String poolPath) {
            this.poolPath = poolPath;

            // Filter ascii from binary files to accept only the second ones.
            files = new ArrayList<File>(Arrays.asList(new File(poolPath).listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return !name.toLowerCase().endsWith(".txt");
                }
            })));
        }


        @Override
        public boolean hasNext() {
            return !files.isEmpty();
        }


        @Override
        public T next() {
            T temp = readFromPool(poolPath, files.get(0).getName());
            files.remove(0);
            return temp;
        }


        @Override
        public void remove() { }


        private T readFromPool(String path, String filename) {
            T rdfQueryRecord = null;
            File file = new File(path + filename);
            ObjectInputStream ois;

            try {

                ois = new ObjectInputStream(new FileInputStream(file));

                rdfQueryRecord = (T)ois.readObject();

                ois.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (EOFException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassCastException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return  rdfQueryRecord;
        }// readFromPool

    }
}
