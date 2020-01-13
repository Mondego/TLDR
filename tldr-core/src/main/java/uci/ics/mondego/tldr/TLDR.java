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
import uci.ics.mondego.tldr.model.TestReport;
import uci.ics.mondego.tldr.indexer.RedisHandler;
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
 * This is the primary of for TLDR. 
 * @author demigorgan
 *
 */
public class TLDR {
	// Directory of the compiles classes
	private static String CLASS_DIR;
	
	private static String TEST_DIR;
	
	// Total time in second required to figure out which tests need to be run
	private static double elapsedTimeInSecond;
	
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
    //public static ThreadedChannel<String> TestRunnerPool;

    public static ConcurrentHashMap<String, Boolean> entityToTest;
    public static ConcurrentHashMap<String, Boolean> allTestDirectories;
    public static ConcurrentHashMap<String, Boolean> allNewAndChangedentities;
    public static ConcurrentHashMap<String, Integer> completeTestCaseSet;
    public static ConcurrentHashMap<String, Method> allChangedOrNewMethods;
    public static ConcurrentHashMap<String, Method> allExtractedTestMethods;
    public static ConcurrentHashMap<String, Boolean> allNewAndChangeTests;
    public static ConcurrentHashMap<String, TestReport> allTestReport;
    
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
    	//this.TestRunnerPool = new ThreadedChannel<String>(config.getThread(), TestRunnerWorker.class);
    	    	
    	this.entityToTest = new ConcurrentHashMap<String, Boolean>();   	
    	this.allTestDirectories = new ConcurrentHashMap<String, Boolean>();
    	this.allNewAndChangedentities = new ConcurrentHashMap<String, Boolean>();
    	this.allNewAndChangeTests = new ConcurrentHashMap<String, Boolean>();
    	this.completeTestCaseSet = new ConcurrentHashMap<String, Integer>();
        this.allChangedOrNewMethods = new ConcurrentHashMap<String, Method>();
        this.allExtractedTestMethods = new ConcurrentHashMap<String, Method>();
        this.allTestReport = new ConcurrentHashMap<String, TestReport>();
    }

    public String getImpactedTest( TLDRRunProperty tldrRunProperty) {    	       
       long startTime = System.nanoTime();
       String testFilter =  null;      
       logger.info("TLDR is starting" + startTime);
       
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
	       
	       /*for(Map.Entry<String, Integer> entry: completeTestCaseSet.entrySet()){
	    	   App.TestRunnerPool.send(entry.getKey());
	       }
	       App.TestRunnerPool.shutdown();*/
	       
	       /**** this is needed for running the tests i.e. for the wrapper*****/
	       if(tldrRunProperty.getTool_type().equals("maven")) {
	    	   testFilter = getTestFilterForMaven();
	       } else if(tldrRunProperty.getTool_type().equals("gradle")) {
	    	   testFilter = getTestFilterForGradle();
	       }
	    	   
	       /*****************************/
	       long endTime = System.nanoTime();	 
	       long elapsedTime = endTime - startTime;
	       elapsedTimeInSecond = (double)elapsedTime / 1000000000.0;	 
	       
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
       int i=0;
       for (Entry<String, Integer> es: all) {
    	   if(es.getKey().contains("<init>") || es.getKey().contains("clinit")) {
    		   continue;  
    	   }
    	
    	   String pkg = es.getKey().substring(0, es.getKey().lastIndexOf('('));
    	   sb.append(pkg.substring(0,pkg.lastIndexOf('.')));
    	   String func = pkg.substring(pkg.lastIndexOf('.')+1);
    	   sb.append("#");
    	   sb.append(func);
    	   i++;
    	   if(i % 1000 == 0) {
    		   sb.append(" ");
    	   } else if(i != (completeTestCaseSet.size() - 1)) {
    		   sb.append(",");
    	   }  		   
       }
       return sb.toString();
    }
    
    private static String getTestFilterForGradle() {
 	    StringBuilder sb = new StringBuilder();
        Set<Map.Entry<String, Integer>> all = completeTestCaseSet.entrySet();
        if(all.size() == 0){
        	return "";
        }
        sb.append("test {\n");
        sb.append("filter {\n");
        
        for(Entry<String, Integer> es: all){
     	   if(es.getKey().contains("<init>") || es.getKey().contains("clinit")) {
     		  continue;
     	   }
     	   
     	   String pkg = es.getKey().substring(0, es.getKey().lastIndexOf('('));
     	   sb.append("includeTestsMatching ");
     	   sb.append("'");
     	   sb.append(pkg);
     	   sb.append("'");
     	   sb.append("\n");    	   
        }
        sb.append("}\n");
        sb.append("}\n");

        return sb.toString();
     }
    
    public static String getCLASS_DIR(){
    	return CLASS_DIR;
    }
    
    public static String getTEST_DIR(){
    	return TEST_DIR;
    }
}