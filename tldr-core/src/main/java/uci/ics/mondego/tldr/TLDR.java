package uci.ics.mondego.tldr;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.bcel.classfile.Method;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import redis.clients.jedis.exceptions.JedisConnectionException;
import uci.ics.mondego.tldr.indexer.RedisHandler;
import uci.ics.mondego.tldr.model.Report;
import uci.ics.mondego.tldr.model.ThreadedChannel;
import uci.ics.mondego.tldr.tool.FindAllTestDirectory;
import uci.ics.mondego.tldr.tool.Constants;
import uci.ics.mondego.tldr.tool.TLDRRunProperty;
import uci.ics.mondego.tldr.worker.EntityChangeAnalyzerWorker;
import uci.ics.mondego.tldr.worker.DFSTraversalWorker;
import uci.ics.mondego.tldr.worker.DependencyExtractorWorker;
import uci.ics.mondego.tldr.worker.EntityToTestMapWorker;
import uci.ics.mondego.tldr.worker.FileChangeAnalyzerWorker;
import uci.ics.mondego.tldr.worker.IntraTestTraversalWorker;
import uci.ics.mondego.tldr.worker.RepoScannerWorker;
import uci.ics.mondego.tldr.worker.TestChangeAnalyzerAndIndexerWorker;
import uci.ics.mondego.tldr.worker.TestDependencyExtractorWorker;
import uci.ics.mondego.tldr.worker.TestFileChangeAnalyzerWorker;

/**
 * This is the primary class of for TLDR. This class exposes an API
 * {@code getImpactedTest} that returns the list of tests that were
 * impacted during the latest iteration. 
 * 
 * @author demigorgan
 *
 */
public class TLDR {
	// Directory of the compiles classes
	private static String CLASS_DIR;
	
	private static String TEST_DIR;
	
	// Total time in second required to figure out which tests need to be run
	private double selectionElapsedTimeInSecond;
	private long selectionStartTime;
	private long selectionEndTime;
	
	private static final Logger logger = LogManager.getLogger(TLDR.class);
    
	// Pools of workers
	public static ThreadedChannel<String> FileChangeAnalysisPool;
    public static ThreadedChannel<String> EntityChangeAnalysisPool;
    public static ThreadedChannel<HashMap<String, Method>> DependencyExtractionPool;
    public static ThreadedChannel<String> DependencyGraphTraversalPool;
    public static ThreadedChannel<String> TestFileChangeAnalysisPool;
    public static ThreadedChannel<String> TestParseAndIndexPool;
    public static ThreadedChannel<HashMap<String, Method>> TestDependencyExtractionPool;
    public static ThreadedChannel<String> EntityToTestMapPool;
    public static ThreadedChannel<String> IntraTestTraversalPool;

    public static ConcurrentHashMap<String, Boolean> entityToTest;
    public static ConcurrentHashMap<String, Boolean> allTestDirectories;
    public static ConcurrentHashMap<String, Boolean> allNewAndChangedentities;
    public static ConcurrentHashMap<String, Integer> completeTestCaseSet;
    public static ConcurrentHashMap<String, Method> allChangedOrNewMethods;
    public static ConcurrentHashMap<String, Method> allExtractedTestMethods;
    public static ConcurrentHashMap<String, Boolean> allNewAndChangeTests;
    
    public TLDR(){
    	Date date = new Date();      
        String LogDate= new SimpleDateFormat("yyyyMMdd").format(date);
        System.setProperty("logFilename", LogDate);
            	    	
    	this.FileChangeAnalysisPool = 
    			new ThreadedChannel<String>(8, FileChangeAnalyzerWorker.class);
    	this.EntityChangeAnalysisPool = 
    			new ThreadedChannel<String>(8, EntityChangeAnalyzerWorker.class);
    	this.DependencyExtractionPool = 
    			new ThreadedChannel<HashMap<String, Method>>(8, DependencyExtractorWorker.class);
    	this.DependencyGraphTraversalPool = 
    			new ThreadedChannel<String>(8,DFSTraversalWorker.class);
    	this.TestFileChangeAnalysisPool = 
    			new ThreadedChannel<String>(8, TestFileChangeAnalyzerWorker.class);
    	this.TestParseAndIndexPool = 
    			new ThreadedChannel<String>(8, TestChangeAnalyzerAndIndexerWorker.class);
    	this.TestDependencyExtractionPool = 
    			new ThreadedChannel<HashMap<String, Method>>(8,TestDependencyExtractorWorker.class);
    	this.EntityToTestMapPool = 
    			new ThreadedChannel<String>(8, EntityToTestMapWorker.class);
    	this.IntraTestTraversalPool = 
    			new ThreadedChannel<String>(8, IntraTestTraversalWorker.class);
    	    	
    	this.entityToTest = new ConcurrentHashMap<String, Boolean>();   	
    	this.allTestDirectories = new ConcurrentHashMap<String, Boolean>();
    	this.allNewAndChangedentities = new ConcurrentHashMap<String, Boolean>();
    	this.allNewAndChangeTests = new ConcurrentHashMap<String, Boolean>();
    	this.completeTestCaseSet = new ConcurrentHashMap<String, Integer>();
        this.allChangedOrNewMethods = new ConcurrentHashMap<String, Method>();
        this.allExtractedTestMethods = new ConcurrentHashMap<String, Method>();
    }

    public String getImpactedTest( TLDRRunProperty tldrRunProperty) {    	       
       selectionStartTime = System.nanoTime();
       String testFilter =  null;      
       logger.info("TLDR is starting" + selectionStartTime);
       
       try {   	   
	       CLASS_DIR = tldrRunProperty.getClass_dir();	      

	       String project_id = getProjectId(tldrRunProperty.getProject_name());
	       System.setProperty(Constants.PROJECT_ID, project_id);
	       	       
	       logger.info("Test directory search begins");
	       FindAllTestDirectory find = new FindAllTestDirectory(CLASS_DIR);
	       Set<String> allTestDir = find.getAllTestDir();
	       for (String s: allTestDir){
	    	   	allTestDirectories.put(s, true);
	       }
	       logger.info(allTestDirectories.size() + " test directory found");
       	       
    	   RepoScannerWorker runnable = new RepoScannerWorker(CLASS_DIR);
    	   runnable.scanClassFiles(CLASS_DIR);
   	       
	       TLDR.FileChangeAnalysisPool.shutdown();
		   TLDR.EntityChangeAnalysisPool.shutdown();
		   
		   for (Map.Entry<String, Method> entry: TLDR.allChangedOrNewMethods.entrySet()) {
			    HashMap<String, Method> map = new HashMap<String, Method>();
			    map.put(entry.getKey(), entry.getValue());
				TLDR.DependencyExtractionPool.send(map);	
		   }
		   
	       TLDR.DependencyExtractionPool.shutdown();
    	   
	       logger.debug("REPO SCANNING FOR TEST SUIT STARTS");
	       RepoScannerWorker testMap = new RepoScannerWorker(TEST_DIR);
	       
	       for (Entry<String, Boolean> e: allTestDirectories.entrySet()){
	    	   testMap.scanTestFiles(e.getKey());
	       }
	       
	       TLDR.TestFileChangeAnalysisPool.shutdown();
	       TLDR.TestParseAndIndexPool.shutdown();
	        	       
	       for (Map.Entry<String, Method> entry: TLDR.allExtractedTestMethods.entrySet()) {
			    HashMap<String, Method> map = new HashMap<String, Method>();
			    map.put(entry.getKey(), entry.getValue());
				TLDR.TestDependencyExtractionPool.send(map);	
		   }
	       	       
    	   TLDR.DependencyGraphTraversalPool.shutdown(); // comment out if problem 
    	   TLDR.TestDependencyExtractionPool.shutdown();
	           	    	   
    	   logger.debug("REPO SCANNING, TEST PARSING, TEST INDEXING IS COMPLETE, NOW MAPPING STARTING");
    	       	   
    	   Set<Map.Entry<String, Boolean>> allEntries = TLDR.entityToTest.entrySet();
	       for(Map.Entry<String, Boolean> e: allEntries){
	    	   TLDR.EntityToTestMapPool.send(e.getKey());
	       }
	       	       
	       TLDR.EntityToTestMapPool.shutdown();
	       
	       for(Map.Entry<String, Boolean> entry: allNewAndChangeTests.entrySet()){
	    	   TLDR.IntraTestTraversalPool.send(entry.getKey());
	       }
	       
	       TLDR.IntraTestTraversalPool.shutdown();
	       
	       /**** this is needed for running the tests i.e. for the wrapper*****/	       
	       testFilter = getTestFilterForMaven();
	    	   
	       selectionEndTime = System.nanoTime();	 
	       long elapsedTime = selectionEndTime - selectionStartTime;
	       selectionElapsedTimeInSecond = (double)elapsedTime / 1000000000.0;	 
	       
       } 
       catch (JedisConnectionException jedisConnectionException){
    	   jedisConnectionException.printStackTrace();
        } 
       catch(ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException){
    	   arrayIndexOutOfBoundsException.printStackTrace();
       } 
       catch(NullPointerException nullPointerException){
    	   nullPointerException.printStackTrace();
       } 
       catch (SecurityException securityException) {
    	    securityException.printStackTrace();
	   } 
       catch (InstantiationException instantiationException) {
    	   instantiationException.printStackTrace();
       } 
       catch (IllegalAccessException illegalAccessException) {
    	   illegalAccessException.printStackTrace();
	   } 
       catch (IllegalArgumentException illegalArgumentException) {
    	   illegalArgumentException.printStackTrace();
       } 
       catch (InvocationTargetException invocationTargetException) {
    	   invocationTargetException.printStackTrace();
       } 
       catch (NoSuchMethodException noSuchMethodException) {
    	   noSuchMethodException.printStackTrace();
       } 
       finally{
    	   RedisHandler.destroyPool();
    	   logger.debug("Ending the Pipeline");
       }
       return testFilter;
    }
    
    /** 
     * Gets the ID of a project. If the project is not in db then
     * it inserts the project in the DB and retuns the address.
     * @param projectName
     */
    private String getProjectId(String projectName) {
    	RedisHandler redisHandler = new RedisHandler();
    	if (redisHandler.projectExists(projectName)) {
    		return redisHandler.getProjectId(projectName);
    	}    	
    	redisHandler.insertProject(projectName);
    	redisHandler.close();
    	return redisHandler.getProjectId(projectName);
    }
 
    /*** this method prepares the command suitable for sure-fire plugin********/
    private static String getTestFilterForMaven(){
	   StringBuilder sb = new StringBuilder();
       Set<Map.Entry<String, Integer>> all = completeTestCaseSet.entrySet();

       for (Entry<String, Integer> es: all) {
    	   if(es.getKey().contains("<init>") || es.getKey().contains("clinit")) {
    		   continue;  
    	   }
    	
    	   String pkg = es.getKey().substring(0, es.getKey().lastIndexOf('('));
    	   sb.append(pkg.substring(0,pkg.lastIndexOf(Constants.DOT)));
    	   String func = pkg.substring(pkg.lastIndexOf(Constants.DOT)+1);
    	   sb.append(Constants.POUND);
    	   sb.append(func);
    	   sb.append(Constants.COMMA);   	  	   
       }
       return sb.toString();
    }
    
    public static String getCLASS_DIR(){
    	return CLASS_DIR;
    }
    
    public static String getTEST_DIR(){
    	return TEST_DIR;
    }
    
    public double getSelectionElapsedTimeInSecond() {
    	return selectionElapsedTimeInSecond;
    }
    
    public long getSelectionStartTime() {
    	return selectionStartTime;
    }
    
    public long getSelectionEndTime() {
    	return selectionEndTime;
    }
    
    /**
     * Returns the report of the TLDR run.
     * @return
     */
    public Report getTestSelectionReport() {    	
    	return new Report(
    			allNewAndChangedentities, 
    			entityToTest, 
    			completeTestCaseSet, 
    			selectionElapsedTimeInSecond);
    }
}
