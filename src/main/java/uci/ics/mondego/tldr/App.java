package uci.ics.mondego.tldr;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.Method;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import redis.clients.jedis.exceptions.JedisConnectionException;
import uci.ics.mondego.tldr.indexer.RedisHandler;
import uci.ics.mondego.tldr.model.ThreadedChannel;
import uci.ics.mondego.tldr.tool.ConfigLoader;
import uci.ics.mondego.tldr.tool.FindAllTestDirectory;
import uci.ics.mondego.tldr.worker.ClassChangeAnalyzerWorker;
import uci.ics.mondego.tldr.worker.DFSTraversalWorker;
import uci.ics.mondego.tldr.worker.DependencyExtractorWorker;
import uci.ics.mondego.tldr.worker.EntityToTestMapWorker;
import uci.ics.mondego.tldr.worker.FileChangeAnalyzerWorker;
import uci.ics.mondego.tldr.worker.IntraTestTraversalWorker;
import uci.ics.mondego.tldr.worker.RepoScannerWorker;
import uci.ics.mondego.tldr.worker.TestChangeAnalyzerAndIndexerWorker;
import uci.ics.mondego.tldr.worker.TestDependencyExtractorWorker;
import uci.ics.mondego.tldr.worker.TestFileChangeAnalyzerWorker;

public class App 
{
	private static String CLASS_DIR;
	private static String TEST_DIR;
	private static double elapsedTimeInSecond;
	
	private static final Logger logger = LogManager.getLogger(App.class);
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
    public static ConcurrentHashMap<String, Boolean> completeTestCaseSet;
    public static ConcurrentHashMap<String, Method> allExtractedMethods;
    public static ConcurrentHashMap<String, Method> allExtractedTestMethods;
    
    public App(){
    	Date date = new Date();      
        String LogDate= new SimpleDateFormat("yyyyMMdd").format(date);
        System.setProperty("logFilename", LogDate);
        
    	logger.debug("Beginning of the Pipeline");
    	
    	ConfigLoader config = new ConfigLoader();
    	this.FileChangeAnalysisPool = new ThreadedChannel<String>(config.getThread(), FileChangeAnalyzerWorker.class);
    	this.EntityChangeAnalysisPool = new ThreadedChannel<String>(config.getThread(), ClassChangeAnalyzerWorker.class);
    	this.DependencyExtractionPool = new ThreadedChannel<HashMap<String, Method>>(config.getThread(), DependencyExtractorWorker.class);
    	this.DependencyGraphTraversalPool = new ThreadedChannel<String>(config.getThread(),DFSTraversalWorker.class);
    	this.TestFileChangeAnalysisPool = new ThreadedChannel<String>(config.getThread(), TestFileChangeAnalyzerWorker.class);
    	this.TestParseAndIndexPool = new ThreadedChannel<String>(config.getThread(), TestChangeAnalyzerAndIndexerWorker.class);
    	this.TestDependencyExtractionPool = new ThreadedChannel<HashMap<String, Method>>(config.getThread(),TestDependencyExtractorWorker.class);
    	this.EntityToTestMapPool = new ThreadedChannel<String>(config.getThread(), EntityToTestMapWorker.class);
    	this.IntraTestTraversalPool = new ThreadedChannel<String>(config.getThread(), IntraTestTraversalWorker.class);
    	
    	this.entityToTest = new ConcurrentHashMap<String, Boolean>();   	
    	this.allTestDirectories = new ConcurrentHashMap<String, Boolean>();
    	this.allNewAndChangedentities = new ConcurrentHashMap<String, Boolean>();
    	this.completeTestCaseSet = new ConcurrentHashMap<String, Boolean>();
        this.allExtractedMethods = new ConcurrentHashMap<String, Method>();
        this.allExtractedTestMethods = new ConcurrentHashMap<String, Method>();
    }

    public static void main( String[] args )
    {    	
       //PropertyConfigurator.configure("log4j.properties");
       
       ConfigLoader config = new ConfigLoader();      
       long startTime = System.nanoTime();
       try{
	       App executorInstance = new App();
	       
	       //CLASS_DIR = config.getCLASS_DIR();
	       CLASS_DIR = args[1];
	      
	       //TEST_DIR = config.getTEST_DIR(); 
	       //allTestDirectory.put(TEST_DIR, true); /*** comment out later *****/
	       
	       FindAllTestDirectory find = new FindAllTestDirectory(CLASS_DIR);
	       Set<String> allTestDir = find.getAllTestDir();
	       for(String s: allTestDir){
	    	   	allTestDirectories.put(s, true);
	       }
	              	       
    	   RepoScannerWorker runnable = new RepoScannerWorker(CLASS_DIR);
    	   runnable.scanClassFiles(CLASS_DIR);
   	       
	       App.FileChangeAnalysisPool.shutdown();
		   App.EntityChangeAnalysisPool.shutdown();
		   
		   for(Map.Entry<String, Method> entry: App.allExtractedMethods.entrySet()){
			    HashMap<String, Method> map = new HashMap<String, Method>();
			    map.put(entry.getKey(), entry.getValue());
				App.DependencyExtractionPool.send(map);	
		   }
		   
	       App.DependencyExtractionPool.shutdown();
	       /*** dangerous comment...uncomment if it doesn't work**/
//	       App.DependencyGraphTraversalPool.shutdown(); 
    	   
	       logger.debug("REPO SCANNING FOR TEST SUIT STARTS");
	       RepoScannerWorker testMap = new RepoScannerWorker(TEST_DIR);
	       //testMap.scanTestFiles(TEST_DIR);
	       
	       for(Entry<String, Boolean> e: allTestDirectories.entrySet()){
	    	   testMap.scanTestFiles(e.getKey());
	       }
	       
	       App.TestFileChangeAnalysisPool.shutdown();
	       App.TestParseAndIndexPool.shutdown();
	        	       
	       for(Map.Entry<String, Method> entry: App.allExtractedTestMethods.entrySet()){
			    HashMap<String, Method> map = new HashMap<String, Method>();
			    map.put(entry.getKey(), entry.getValue());
				App.TestDependencyExtractionPool.send(map);	
		   }
	       	       
    	   App.DependencyGraphTraversalPool.shutdown(); // comment out if problem 
    	   App.TestDependencyExtractionPool.shutdown();
	           	    	   
    	   logger.debug("REPO SCANNING, TEST PARSING, TEST INDEXING IS COMPLETE, NOW MAPPING STARTING");
    	       	   
    	   Set<Map.Entry<String, Boolean>> allEntries = App.entityToTest.entrySet();
	       for(Map.Entry<String, Boolean> e: allEntries){
	    	   //logger.debug(e.getKey()+"is being sent to the mapPool from App");
	    	   App.EntityToTestMapPool.send(e.getKey());
	       }
	       	       
	       App.EntityToTestMapPool.shutdown();
	       App.IntraTestTraversalPool.shutdown();
	       	       
	       /**** this is needed for running the tests i.e. for the wrapper*****/
	       if(args[3].equals("maven"))
	    	   System.out.println(getTestFilterForMaven());
	       else if(args[3].equals("gradle"))
	    	   System.out.println(getTestFilterForGradle());
	       
	       /*****************************/
	       
	       long endTime = System.nanoTime();	 
	       long elapsedTime = endTime - startTime;
	       elapsedTimeInSecond = (double)elapsedTime / 1000000000.0;	       	       
	       logExperiment(args[0], args[1].substring(args[1].lastIndexOf('-')+1), args[2]);     
       }
       
       catch( JedisConnectionException e){
    	   System.err.println("NO CONNECTION DETECTED");
    	   e.printStackTrace();
        }
       
       catch(ArrayIndexOutOfBoundsException e){
    	   System.out.println("the file path is too long");
    	   e.printStackTrace();
       }
       
       catch(NullPointerException e){
    	   System.out.println("extractor can't find the designated class");
    	   e.printStackTrace();
       }
       
       catch( ClassFormatException e){
    	   e.printStackTrace();
    	   logger.error("Class Format malfunction : "+ e.getMessage());
       } 
       
       catch (SecurityException e) {
    	    logger.error("Security Exception at : "+ e.getMessage());
			e.printStackTrace();
		}
       
       catch (InstantiationException e) {
    	   logger.error("InstantiationException Exception at : "+ e.getMessage());
    	   e.printStackTrace();
       } 
       
       catch (IllegalAccessException e) {
    	   logger.error("IllegalAccessException Exception at : "+ e.getMessage());
    	   e.printStackTrace();
	   } 
       
       catch (IllegalArgumentException e) {
    	   logger.error("IllegalArgumentException Exception at : "+ e.getMessage());
    	   e.printStackTrace();
       } 
       
       catch (InvocationTargetException e) {
    	   logger.error("InvocationTargetException Exception at : "+ e.getMessage());
    	   e.printStackTrace();
       } 
       
       catch (NoSuchMethodException e) {
    	   logger.error("NoSuchMethodException Exception at : "+ e.getMessage());
    	   e.printStackTrace();
       } 
       
       finally{
    	   RedisHandler.destroyPool();
    	   logger.debug("Ending the Pipeline");
       }
    }
    
    private static void logExperiment(String pair, String project, String commit){ 	
    	//System.out.println("GENERATING REPORT FOR "+project+" Commit: "+commit);	           	
    	PrintWriter writer1;
		try {
			writer1 = new PrintWriter(project+"_"+pair+"_REPORT_"+commit+"_.txt", "UTF-8");
			writer1.println("NUMBER OF NEW OR CHANGED ENTITIES : "+allNewAndChangedentities.size());	   
			writer1.println("NUMBER OF ENTITY TO TEST : "+entityToTest.size());	   
			writer1.println("NUMBER OF TEST TO RUN : "+completeTestCaseSet.size());	   
			writer1.println("TOTAL TIME REQUIRED : "+elapsedTimeInSecond+" second");	
			writer1.println("======================================================");
			writer1.println("======================================================");
			
	    	Set<Entry<String, Boolean>> allEntries = App.allNewAndChangedentities.entrySet();
			writer1.println("NEW OR CHANGED ENTITIES : \n\n");	   
	    	for(Entry<String, Boolean> e: allEntries){
	    		writer1.println(e.getKey());
	    	}
	    	
	    	writer1.println("======================================================");
			writer1.println("======================================================");
						
			allEntries = App.entityToTest.entrySet();
			writer1.println("ALL ENTITY TO TEST : \n\n");	   
	    	for(Entry<String, Boolean> e: allEntries){
	    		writer1.println(e.getKey());
	    	}
	    	
	    	writer1.println("======================================================");
			writer1.println("======================================================");
			
			allEntries = App.completeTestCaseSet.entrySet();
			writer1.println("ALL TESTS TO RUN : \n\n");	   
	    	for(Entry<String, Boolean> e: allEntries){
	    		writer1.println(e.getKey());
	    	}	    	
	    	writer1.close();
		} 
		
		catch (FileNotFoundException e) {
	    	logger.error("File Not Found Exception while writing report : "+ e.getMessage());
			e.printStackTrace();
		} 
		catch (UnsupportedEncodingException e) {
			logger.error("UnsupportedEncodingException while writing report : "+ e.getMessage());
			e.printStackTrace();
		}    	
    }
     
    /*** this method prepares the command suitable for sure-fire plugin********/
    private static String getTestFilterForMaven(){
	   StringBuilder sb = new StringBuilder();
       Set<Map.Entry<String, Boolean>> all = completeTestCaseSet.entrySet();
       int i=0;
       for(Entry<String, Boolean> es: all){
    	   if(es.getKey().contains("<init>") || es.getKey().contains("clinit"))
    		   continue;
    	   String pkg = es.getKey().substring(0, es.getKey().lastIndexOf('('));
    	   sb.append(pkg.substring(0,pkg.lastIndexOf('.')));
    	   String func = pkg.substring(pkg.lastIndexOf('.')+1);
    	   sb.append("#");
    	   sb.append(func);
    	   i++;
    	   if(i % 1000 == 0){
    		   sb.append(" ");
    	   }
    	   else if(i != (completeTestCaseSet.size() - 1))
    		   sb.append(",");
       }
       return sb.toString();
    }
    /*test {
    filter {
        includeTestsMatching '*shouldRunInMultipleThreads'
    }
}*/
    
    private static String getTestFilterForGradle(){
 	    StringBuilder sb = new StringBuilder();
        Set<Map.Entry<String, Boolean>> all = completeTestCaseSet.entrySet();
        if(all.size() == 0){
        	return "";
        }
        sb.append("test {\n");
        sb.append("filter {\n");
        
        for(Entry<String, Boolean> es: all){
     	   if(es.getKey().contains("<init>") || es.getKey().contains("clinit"))
     		   continue;
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
