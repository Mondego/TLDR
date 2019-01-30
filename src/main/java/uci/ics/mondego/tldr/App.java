package uci.ics.mondego.tldr;


import uci.ics.mondego.tldr.indexer.RedisHandler;
import uci.ics.mondego.tldr.map.EntityToTestMap;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.Method;
import org.apache.commons.collections4.ListUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import redis.clients.jedis.exceptions.JedisConnectionException;
import uci.ics.mondego.tldr.changeanalyzer.ClassChangeAnalyzer;
import uci.ics.mondego.tldr.changeanalyzer.DependencyExtractor;
import uci.ics.mondego.tldr.changeanalyzer.FileChangeAnalyzer;
import uci.ics.mondego.tldr.changeanalyzer.TestChangeAnalyzer;
import uci.ics.mondego.tldr.model.SourceFile;
import uci.ics.mondego.tldr.resolution.DFSTraversal;
import uci.ics.mondego.tldr.tool.Databases;


/**
 * Hello world!
 *
 */
public class App 
{
	private static String PROJ_DIR;
	private static final Logger logger = LogManager.getLogger(ClassChangeAnalyzer.class);
	
	

    public static void main( String[] args )
    {    	
      // BasicConfigurator.configure();
       PropertyConfigurator.configure("log4j.properties");

       RedisHandler rh = null;
       try{
	       PROJ_DIR = "/Users/demigorgan/brigadier";
	       //PROJ_DIR = "/Users/demigorgan/log4j";
	       
	       //STEP 1 : Scan the repository - gets java, test, class, and jar files. 
	       RepoScanner rs = new RepoScanner(PROJ_DIR);
	       
	       // in memory database handler
	       rh =  RedisHandler.getInstane();
	       
	       List<SourceFile> allClass = rs.get_all_class_files();
	       List<SourceFile> allTestClass = rs.get_all_test_class_files();
	       
	       List<SourceFile> changedFiles = new ArrayList<SourceFile>();
	       List<SourceFile> changedTests = new ArrayList<SourceFile>();
	       
	       List<String> changedEntities = new ArrayList<String>();
	       
	       Map<String, Method> fqnToCodeMap = new HashMap<String, Method>();
	       
	       //ByteCodeParser bp = new ByteCodeParser(allClass.get(11));
	       
	       // STEP 2.1: FIND CHANGED CLASS FILES
	       for(int i=0;i<allClass.size();i++){
	    	   
	    	   FileChangeAnalyzer fc = new FileChangeAnalyzer(allClass.get(i).getPath());
		       if(fc.hasChanged())
		    	   changedFiles.add(allClass.get(i));
	       }
	       
	       // STEP 2.2: FIND ALL CHANGED TEST FILES
	       
	       for(int i=0;i<allTestClass.size();i++){
	    	   //System.out.println("here : "+allTestClass.get(i).getName());
	    	   FileChangeAnalyzer fc = new FileChangeAnalyzer(allTestClass.get(i).getPath());
		       if(fc.hasChanged()){ 	   
		    	   changedTests.add(allTestClass.get(i));
		       } 	   
	       }
	        
	        //STEP 3.1: FIND CHANGED ENTITIES
	       for(int i=0;i<changedFiles.size();i++){
	    	   // for each changed class we check which field/method change   
	    	   ClassChangeAnalyzer cc = new ClassChangeAnalyzer(changedFiles.get(i).getPath()); 
	    	   List<String> chEnt = cc.getChangedAttributes();
	    	   changedEntities.addAll(chEnt);
	    	   fqnToCodeMap.putAll(cc.getextractedFunctions());
	       }
	       
	       
	       // STEP 3.2: RESOLUTION OF DEPENDENCY
	       
	       System.out.println(fqnToCodeMap.size());
	       
	       DependencyExtractor depExt = new DependencyExtractor(fqnToCodeMap);
	       depExt.resolute();
	       
	       
	       // STEP 3.2: PARSE AND MAP TEST METHODS TO ENTITIES;
	       for(int i=0;i<changedTests.size();i++){
	    	   // for each changed class we check which field/method change  
	    	   
	    	   TestChangeAnalyzer cc = new TestChangeAnalyzer(changedTests.get(i).getPath()); 
	    	   List<String> chEnt = cc.getChangedAttributes();
	    	   //changedEntities.addAll(chEnt);
	       }
	       
	       	       
	       // STEP 4: FIND ALL DEPENDENT ENTITIES FOR EACH CHANGED ENTITY
	       List<String> allEntitiesToTest = new ArrayList<String>();
	       DFSTraversal dfs = new DFSTraversal();
	       
	       for(int i=0;i<changedEntities.size();i++){
	    	   List<String> dep = dfs.get_all_dependent(changedEntities.get(i));
	    	   allEntitiesToTest = ListUtils.union(dep, allEntitiesToTest);
	    	   //System.out.println(changedEntities.get(i));
	       }
	       
	       
	       List<String> allTestToRun = new ArrayList<String>();
	       EntityToTestMap map = new EntityToTestMap();

	       // STEP 5: FIND ALL TESTS FOR THE allEntityToTest List
	       System.out.println("\n\n	ALL ENTITY TO TEST : \n");

	       for(int i=0;i<allEntitiesToTest.size();i++){
	    	   System.out.println(allEntitiesToTest.get(i));
	    	   Set<String> tests = map.getTests(allEntitiesToTest.get(i));
	    	   for(String str: tests)
	    		   allTestToRun.add(str);
	       }
	       
	       System.out.println("\n\n	ALL TEST TO RUN : \n");

	       for(int i=0;i<allTestToRun.size();i++){
	    	   //System.out.println(allTestToRun.get(i));
	       }
	       
       }
       
       catch( JedisConnectionException e){
    	   System.out.println("No Connection to Jedis Server");
    	   e.printStackTrace();   
       }
       
       catch(IOException e){
    	   System.out.println("extractor can't read the designated class");
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
       
       catch(NoSuchAlgorithmException e){
    	   e.printStackTrace();
       }
       
       catch( ClassFormatException e){
    	   logger.error("Class Format malfunction : "+ e.getMessage());
       }
       finally{
    	   rh.close();
       }
    }
}
