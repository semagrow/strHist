package gr.demokritos.iit.irss.semagrow.logging;

import gr.demokritos.iit.irss.semagrow.sesame.Workflow;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.BlockingQueue;

/**
 * 
 * @author Giannis Mouchakis
 *
 */
public class LoggerWithQueue implements Runnable {

	BlockingQueue<Object> queue;
	private Boolean finished;
	
//	static final Path path = Paths.get("/home/nickozoulis/strhist_exp_logs/", "semagrow_logs.log");
	static final OpenOption[] options = {StandardOpenOption.CREATE, StandardOpenOption.APPEND};
	private BufferedWriter writer;

	public LoggerWithQueue(BlockingQueue<Object> queue) {
		this.queue = queue;
		finished = false;
		try {
			writer = Files.newBufferedWriter(Workflow.path, StandardCharsets.UTF_8, options);
			if (Files.size(Workflow.path) == 0) {
//				writer.write("start headers");
//				writer.newLine();
//				writer.write("session-id");
//				writer.newLine();
//				writer.write("start-time");
//				writer.newLine();
//				writer.write("endpoint");
//				writer.newLine();
//				writer.write("query");
//				writer.newLine();
//				writer.write("total bindings");
//				writer.newLine();
//				writer.write("query binding names");
//				writer.newLine();
//				writer.write("result binding names");
//				writer.newLine();
//				writer.write("all binding names");
//				writer.newLine();
//				writer.write("end headers");
//				writer.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while (  ! finished  ) {
			try {
				Object obj = queue.take();
				if (obj instanceof String) {
					writer.write((String) obj);
				} else if (obj instanceof Integer) {
					writer.write((int) obj);
				} else {
					writer.write(obj.toString());
				}
				writer.newLine();
                writer.flush();
			} catch (IOException | InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		
		// empty remaining items in queue if any
		while (!queue.isEmpty()) {
			try {
				Object obj = queue.remove();
				if (obj instanceof String) {
					writer.write((String) obj);
				} else if (obj instanceof Integer) {
					writer.write((int) obj);
				} else {
					writer.write(obj.toString());
				}
				writer.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// close writer (and flush remains in file)
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void finish() {
		finished = true;
	}

}
