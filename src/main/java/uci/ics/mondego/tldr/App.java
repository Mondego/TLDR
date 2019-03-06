package uci.ics.mondego.tldr;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.Method;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import redis.clients.jedis.exceptions.JedisConnectionException;
import uci.ics.mondego.tldr.changeanalyzer.ClassChangeAnalyzer;
import uci.ics.mondego.tldr.indexer.RedisHandler;
import uci.ics.mondego.tldr.model.ThreadedChannel;
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
	public static String TEST_DIR;
	private static final Logger logger = LogManager.getLogger(ClassChangeAnalyzer.class);
	
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
    	
    	this.FileChangeAnalysisPool = new ThreadedChannel<String>(8, FileChangeAnalyzerWorker.class);
    	this.EntityChangeAnalysisPool = new ThreadedChannel<String>(8, ClassChangeAnalyzerWorker.class);
    	this.DependencyExtractionPool = new ThreadedChannel<HashMap<String, Method>>(8, DependencyExtractorWorker.class);
    	this.DependencyGraphTraversalPool = new ThreadedChannel<String>(8,DFSTraversalWorker.class);
    	this.TestFileChangeAnalysisPool = new ThreadedChannel<String>(8, TestFileChangeAnalyzerWorker.class);
    	this.TestParseAndIndexPool = new ThreadedChannel<String>(8, TestChangeAnalyzerAndIndexerWorker.class);
    	this.EntityToTestMapPool = new ThreadedChannel<String>(8, EntityToTestMapWorker.class);
    	    	
    	this.entityToTest = new ConcurrentHashMap<String, Boolean>();   	
    	this.testToRun = new ConcurrentHashMap<String, Boolean>();
    }

    public static void main( String[] args )
    {    	
       PropertyConfigurator.configure("log4j.properties");
       long startTime = System.nanoTime();
       try{
	       App executorInstance = new App();
    	   
    	   CLASS_DIR = "/Users/demigorgan/Desktop/commons-math";
    	   TEST_DIR = "/Users/demigorgan/Desktop/commons-math/target/test-classes";
	      
    	   //CLASS_DIR = "/Users/demigorgan/brigadier";
	       //TEST_DIR = "/Users/demigorgan/brigadier/build/classes/java/test";
	       	       
    	   RepoScannerWorker runnable =new RepoScannerWorker(CLASS_DIR);
    	   runnable.scanClassFiles(CLASS_DIR);

    	   App.FileChangeAnalysisPool.shutdown();
	       App.EntityChangeAnalysisPool.shutdown();
	       App.DependencyExtractionPool.shutdown();
	       App.DependencyGraphTraversalPool.shutdown();
	       
	       
	       RepoScannerWorker testMap =new RepoScannerWorker(TEST_DIR);
    	   testMap.scanTestFiles(TEST_DIR);
	           	   
	       Set<Map.Entry<String, Boolean>> allEntries = App.entityToTest.entrySet();
	       for(Map.Entry<String, Boolean> e: allEntries){
	    	   App.EntityToTestMapPool.send(e.getKey());
	       }
	       
	       App.TestFileChangeAnalysisPool.shutdown();
	       App.TestParseAndIndexPool.shutdown();
	       App.EntityToTestMapPool.shutdown();
	    	
	       /**** this is needed for running the tests i.e. for the wrapper*****/
	       String print = getCommand();
	       System.out.println(print);

	       long endTime = System.nanoTime();	 
	       long elapsedTime = endTime - startTime;
	       double elapsedTimeInSecond = (double)elapsedTime / 1000000000.0;
	       //System.out.println("TOTAL TIME: "+ elapsedTimeInSecond);     
	       //logExperiment(args[0], getCommand());     
	       //System.out.println(App.testToRun.size());
	       //System.out.println(App.entityToTest.size());
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
		// TODO Auto-generated catch block
    	   e.printStackTrace();
       } 
       
       catch (NoSuchMethodException e) {
		   
    	   e.printStackTrace();
       } 
       
       finally{
    	   RedisHandler.destroyPool();
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
	    	
	    	
		} catch (FileNotFoundException e) {
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
}
