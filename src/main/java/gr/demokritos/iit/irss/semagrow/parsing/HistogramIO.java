package gr.demokritos.iit.irss.semagrow.parsing;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesBucket;

public class HistogramIO<R extends Rectangle<R>> {

	private String path;
	private STHolesBucket<R> rootBucket;


	public HistogramIO(String path, STHolesBucket<R> rootBucket) {
		setPath(path);
		setRootBucket(rootBucket);
	}


	public void write() {		
		writeNonBinary();
//		writeBinary();
	}


	/**
	 * Writes the histogram into a file in a non human-readable form.
	 */
	public void writeBinary() {
		ObjectOutputStream oos = null;		
		
		try {			
			oos = new ObjectOutputStream(new FileOutputStream(getPath() + ".ser"));
			
			oos.writeObject(getRootBucket());			
			
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}// writeBinary


	/**
	 * Writes the histogram into a file in a human-readable form.
	 */
	private void writeNonBinary() {
		BufferedWriter bw = null;
		
		try {
			bw = new BufferedWriter(new FileWriter(getPath() + ".txt"));

			// Write root bucket.			
			bw.write(rootBucket.toString());
			
			bw.write("\n{\n");
			// Get root's children.
			Collection<STHolesBucket<R>> children = rootBucket.getChildren();

			System.err.println("Children: " + children.size());

			// Foreach child, call write method recursively.
			for (STHolesBucket<R> child : children) {
				write(child, bw);
			}
			
			bw.write("\n}\n");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}// writeNonBinary	


	/**
	 * Recursively writes a bucket and its children if exist.
	 */
	private void write(STHolesBucket<R> bucket, BufferedWriter bw) {

		if (bucket.getChildren().isEmpty()) {
			try { 
				bw.write("\n{\n");
				bw.write(bucket.toString());
				bw.write("\n}\n");
			} catch (IOException e) {e.printStackTrace();}
			
		} else {
			try {
			bw.write("\n{\n");
			for (STHolesBucket<R> child : bucket.getChildren()) {
							
				bw.write(child.toString());
				
				write(child, bw);
				
			}// for
			bw.write("\n}\n");
			} catch (IOException e) {e.printStackTrace();}
		}// else
	}// write
	
	
	/**
	 * Reads the binary file and instantiates the rootBucket of the Histogram.	
	 */
	public STHolesBucket<R> readBinary(String path) {
		STHolesBucket<R> rootBucket = null;
		
		ObjectInputStream ois = null;
		
		try {
			ois = new ObjectInputStream(new FileInputStream(path));
						
			rootBucket = (STHolesBucket<R>)ois.readObject();			
			
			ois.close();
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return rootBucket;
	}// readBinary
	

	/*
	 * Getters & Setters.
	 */
	public String getPath() {
		return path;
	}


	public void setPath(String path) {
		this.path = path;
	}


	public STHolesBucket<R> getRootBucket() {
		return rootBucket;
	}


	public void setRootBucket(STHolesBucket<R> rootBucket) {
		this.rootBucket = rootBucket;
	}

}
