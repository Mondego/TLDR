package uci.ics.mondego.tldr;


import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
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


/**
 * Hello world!
 *
 */
public class App 
{
	private static String PROJ_DIR;
	private static final Logger logger = LogManager.getLogger(ClassChangeAnalyzer.class);
	
    public static ThreadedChannel<String> changedFiles;
    public static ThreadedChannel<String> changedEntities;
    public static ThreadedChannel<String> allEntitiesToTest;
    public static ThreadedChannel<HashMap<String, Method>> dependencyExtractor;
    public static ThreadedChannel<String> traverseDependencyGraph;
    public static ThreadedChannel<String> entityToTestMap;
   
    public static ConcurrentHashMap<String, Method> fqnToCodeMap;
    public static ConcurrentHashMap<String, Boolean> entityToTest;
    public static ConcurrentHashMap<String, Boolean> testToRun;

    public App(){
    	
    	this.changedFiles = new ThreadedChannel<String>(8, FileChangeAnalyzerWorker.class);
    	this.changedEntities = new ThreadedChannel<String>(8, ClassChangeAnalyzerWorker.class);
    	this.dependencyExtractor = new ThreadedChannel<HashMap<String, Method>>(8, DependencyExtractorWorker.class);
    	this.traverseDependencyGraph = new ThreadedChannel<String>(8,DFSTraversalWorker.class);
    	this.entityToTestMap = new ThreadedChannel<String>(8,EntityToTestMapWorker.class);    	
    	
    	this.entityToTest = new ConcurrentHashMap<String, Boolean>();
    	this.testToRun = new ConcurrentHashMap<String, Boolean>();
    }

    public static void main( String[] args )
    {    	
       PropertyConfigurator.configure("log4j.properties");
       
       try{
	       App executorInstance = new App();
    	   
    	   //PROJ_DIR = "/Users/demigorgan/brigadier";
	       PROJ_DIR = "/Users/demigorgan/Desktop/Ekstazi_dataset/camel-master";
	       	       
    	   RepoScannerWorker runnable =new RepoScannerWorker(PROJ_DIR);
    	   runnable.scan(PROJ_DIR);
//           System.out.println("Creating Thread...");
//           Thread thread = new Thread(runnable);
//           System.out.println("Starting Thread...");
//           thread.start();
//    	   thread.join();
	       
	       //repoScanner.scan(PROJ_DIR);	       
	       
	        App.changedFiles.shutdown();
	    	App.changedEntities.shutdown();
	    	App.dependencyExtractor.shutdown();
	    	App.traverseDependencyGraph.shutdown();
	    	App.entityToTestMap.shutdown();
	    	
	    	System.out.println(entityToTest.size());	    	
	      
       }
       
       catch( JedisConnectionException e){
    	   System.out.println("No Connection to Jedis Server");
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
    	   logger.error("Class Format malfunction : "+ e.getMessage());
       } catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IllegalAccessException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IllegalArgumentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (InvocationTargetException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (NoSuchMethodException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
       finally{
    	   RedisHandler.destroyPool();
       }
    }
}
