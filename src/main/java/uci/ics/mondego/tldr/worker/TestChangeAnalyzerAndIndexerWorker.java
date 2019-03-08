package uci.ics.mondego.tldr.worker;

import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import uci.ics.mondego.tldr.changeanalyzer.TestChangeAnalyzer;
import uci.ics.mondego.tldr.exception.DatabaseSyncException;

public class TestChangeAnalyzerAndIndexerWorker extends Worker{
	
	private final String testClassName;
	private static final Logger logger = LogManager.getLogger(TestChangeAnalyzerAndIndexerWorker.class);
	
	public TestChangeAnalyzerAndIndexerWorker(String name){
		this.testClassName = name;
	}
	
	public void run(){
		try {
			//logger.debug(testClassName.substring(testClassName.lastIndexOf("/")+1)
			//		+"indexed, end of the pool parseindextest");
			TestChangeAnalyzer ts = new TestChangeAnalyzer(testClassName);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
