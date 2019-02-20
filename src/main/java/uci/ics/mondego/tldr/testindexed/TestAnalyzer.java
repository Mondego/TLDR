package uci.ics.mondego.tldr.testindexed;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.bcel.classfile.Method;

import uci.ics.mondego.tldr.model.ThreadedChannel;
import uci.ics.mondego.tldr.worker.ClassChangeAnalyzerWorker;
import uci.ics.mondego.tldr.worker.DFSTraversalWorker;
import uci.ics.mondego.tldr.worker.DependencyExtractorWorker;
import uci.ics.mondego.tldr.worker.EntityToTestMapWorker;
import uci.ics.mondego.tldr.worker.FileChangeAnalyzerWorker;

public class TestAnalyzer implements Runnable{

	private final String TEST_DIR;
	
	
	public static ThreadedChannel<String> changedTestFiles;
    public static ThreadedChannel<String> changedTestCases;
    public static ThreadedChannel<HashMap<String, Method>> dependencyExtractor;   
    
	public TestAnalyzer(String testDir){
		this.TEST_DIR = testDir;
		this.changedTestFiles = new ThreadedChannel<String>(8, FileChangeAnalyzerWorker.class);
    	this.changedTestCases = new ThreadedChannel<String>(8, ClassChangeAnalyzerWorker.class);
    	this.dependencyExtractor = new ThreadedChannel<HashMap<String, Method>>(8, DependencyExtractorWorker.class);    	
	}
	
	
    public void run() {
        //Code
    }
	
	
	public int analyze(String testDir)  {
		// TODO Auto-generated method stub
		return 1;
		
	}
}
