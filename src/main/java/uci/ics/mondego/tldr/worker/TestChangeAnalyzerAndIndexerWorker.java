package uci.ics.mondego.tldr.worker;

import java.io.IOException;

import uci.ics.mondego.tldr.changeanalyzer.TestChangeAnalyzer;
import uci.ics.mondego.tldr.exception.DatabaseSyncException;

public class TestChangeAnalyzerAndIndexerWorker extends Worker{
	
	private final String testClassName;
	
	public TestChangeAnalyzerAndIndexerWorker(String name){
		this.testClassName = name;
	}
	
	public void run(){
		try {
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
