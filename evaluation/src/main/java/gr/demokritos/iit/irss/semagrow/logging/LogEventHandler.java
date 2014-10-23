package gr.demokritos.iit.irss.semagrow.logging;

import com.lmax.disruptor.EventHandler;
import gr.demokritos.iit.irss.semagrow.sesame.Workflow;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class LogEventHandler implements EventHandler<LogEvent>
{

	static final OpenOption[] options = {StandardOpenOption.CREATE, StandardOpenOption.APPEND};
	private BufferedWriter writer;
	
	public LogEventHandler() {
		super();
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
	
    public void onEvent(LogEvent event, long sequence, boolean endOfBatch)
    {
    	try {
    		Object obj = event.get();
    		if (obj instanceof String) {
    			writer.write( (String) obj);
    		} else if (obj instanceof Integer) {
    			writer.write( (int) obj);
    		} else {
    			writer.write(event.get().toString());
    		}
			writer.newLine();
			if (endOfBatch) {
				writer.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
