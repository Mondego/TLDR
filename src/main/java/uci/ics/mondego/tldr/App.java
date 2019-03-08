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
import uci.ics.mondego.tldr.worker.ClassChangeAnalyzerWorker;
import uci.ics.mondego.tldr.worker.DFSTraversalWorker;
import uci.ics.mondego.tldr.worker.DependencyExtractorWorker;
import uci.ics.mondego.tldr.worker.EntityToTestMapWorker;
import uci.ics.mondego.tldr.worker.FileChangeAnalyzerWorker;
import uci.ics.mondego.tldr.worker.RepoScannerWorker;
import uci.ics.mondego.tldr.worker.TestChangeAnalyzerAndIndexerWorker;
import uci.ics.mondego.tldr.worker.TestFileChangeAnalyzerWorker;

public class App 
{
	private static String CLASS_DIR;
	private static String TEST_DIR;
	private static final Logger logger = LogManager.getLogger(App.class);
    public static ThreadedChannel<String> FileChangeAnalysisPool;
    public static ThreadedChannel<String> EntityChangeAnalysisPool;
    public static ThreadedChannel<HashMap<String, Method>> DependencyExtractionPool;
    public static ThreadedChannel<String> DependencyGraphTraversalPool;
    public static ThreadedChannel<String> TestFileChangeAnalysisPool;
    public static ThreadedChannel<String> TestParseAndIndexPool;
    public static ThreadedChannel<String> EntityToTestMapPool;
   
    public static ConcurrentHashMap<String, Boolean> entityToTest;
    public static ConcurrentHashMap<String, Boolean> testToRun;

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
    	this.EntityToTestMapPool = new ThreadedChannel<String>(config.getThread(), EntityToTestMapWorker.class);
    	    	
    	this.entityToTest = new ConcurrentHashMap<String, Boolean>();   	
    	this.testToRun = new ConcurrentHashMap<String, Boolean>();
    	
    }

    public static void main( String[] args )
    {    	
       //PropertyConfigurator.configure("log4j.properties");
       
       ConfigLoader config = new ConfigLoader();
       
       long startTime = System.nanoTime();
       
       try{
	       App executorInstance = new App();
	       CLASS_DIR = config.getCLASS_DIR();
	       TEST_DIR = config.getTEST_DIR();
	       	       
    	   RepoScannerWorker runnable =new RepoScannerWorker(CLASS_DIR);
    	   runnable.scanClassFiles(CLASS_DIR);
   	       
    	   App.FileChangeAnalysisPool.shutdown();
	       App.EntityChangeAnalysisPool.shutdown();
	       App.DependencyExtractionPool.shutdown();
	       App.DependencyGraphTraversalPool.shutdown();
    	   
	       logger.debug("REPO SCANNING FOR TEST SUIT STARTS");
	       RepoScannerWorker testMap =new RepoScannerWorker(TEST_DIR);
    	   testMap.scanTestFiles(TEST_DIR);
    	   App.TestFileChangeAnalysisPool.shutdown();
	       App.TestParseAndIndexPool.shutdown();
	       
    	   logger.debug("REPO SCANNING, TEST PARSING, TEST INDEXING IS COMPLETE, NOW MAPPING STARTING");
    	   
    	   Set<Map.Entry<String, Boolean>> allEntries = App.entityToTest.entrySet();
	       for(Map.Entry<String, Boolean> e: allEntries){
	    	   //logger.debug(e.getKey()+"is being sent to the mapPool from App");
	    	   App.EntityToTestMapPool.send(e.getKey());
	       }

	       App.EntityToTestMapPool.shutdown();
	       	       
	       /**** this is needed for running the tests i.e. for the wrapper*****/
	       String print = getCommand();
	       //System.out.println(print);

	       long endTime = System.nanoTime();	 
	       long elapsedTime = endTime - startTime;
	       double elapsedTimeInSecond = (double)elapsedTime / 1000000000.0;
	          
	       System.out.println(entityToTest.size());
	       System.out.println(testToRun.size());
	       System.out.println(elapsedTimeInSecond);
	       
	       //253.933992205
	       
	       //logExperiment(args[0], getCommand());     
       }
       
       catch( JedisConnectionException e){
    	   System.err.println("NO CONNECTION DETECTED");
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
    	   logger.error("Class Format malfunction : "+ e.getMessage());
       } 
       
       catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
       catch (InstantiationException e) {
		// TODO Auto-generated catch block
    	   e.printStackTrace();
       } 
       
       catch (IllegalAccessException e) {
		// TODO Auto-generated catch block
    	   e.printStackTrace();
	   } 
       
       catch (IllegalArgumentException e) {
		// TODO Auto-generated catch block
    	   e.printStackTrace();
       } 
       
       catch (InvocationTargetException e) {
    	   e.printStackTrace();
       } 
       
       catch (NoSuchMethodException e) {
		   
    	   e.printStackTrace();
       } 
       
       finally{
    	   RedisHandler.destroyPool();
    	   logger.debug("Ending the Pipeline");
       }
    }
    
    private static void logExperiment(String commit, String content){
    	
    	PrintWriter writer1;
    	PrintWriter writer2;
		try {
			writer1 = new PrintWriter("TEST_"+commit+"_.txt", "UTF-8");
			writer2 = new PrintWriter("ENTITY_"+commit+"_.txt", "UTF-8");
			writer1.println(content.replaceAll(",", "\n"));
	    	writer1.close();
	    	Set<Entry<String, Boolean>> allEntries = App.entityToTest.entrySet();
	    	for(Entry<String, Boolean> e: allEntries){
	    		writer2.println(e.getKey());
	    	}
	    	
	    	writer2.close();
		} 
		
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
    }
    
    private static String getCommand(){
    	  StringBuilder sb = new StringBuilder();
	       //sb.append("mvn test -Dtest=");
	       Set<Map.Entry<String, Boolean>> all = testToRun.entrySet();
	       int i=0;
	       for(Entry<String, Boolean> es: all){
	    	   if(es.getKey().contains("<init>") || es.getKey().contains("clinit"))
	    		   continue;
	    	   String pkg = es.getKey().substring(0, es.getKey().lastIndexOf('('));
	    	   sb.append(pkg.substring(0,pkg.lastIndexOf('.')));
	    	   String func = pkg.substring(pkg.lastIndexOf('.')+1);
	    	   sb.append("#");
	    	   sb.append(func);
	    	   if(i != (testToRun.size() - 1))
	    		   sb.append(",");
	    	   i++;
	       }
	       return sb.toString();
    }
    
    public static String getCLASS_DIR(){
    	return CLASS_DIR;
    }
    
    public static String getTEST_DIR(){
    	return TEST_DIR;
    }
}
