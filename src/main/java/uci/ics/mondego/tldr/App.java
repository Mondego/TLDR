package uci.ics.mondego.tldr;


import uci.ics.mondego.tldr.indexer.RedisHandler;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import redis.clients.jedis.exceptions.JedisConnectionException;
import uci.ics.mondego.tldr.changeanalyzer.ChangeAnalyzer;
import uci.ics.mondego.tldr.changeanalyzer.ClassChangeAnalyzer;
import uci.ics.mondego.tldr.changeanalyzer.FileChangeAnalyzer;
import uci.ics.mondego.tldr.extractor.ByteCodeParser;
import uci.ics.mondego.tldr.model.SourceFile;



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
    	BasicConfigurator.configure();

       RedisHandler rh = null;
       try{
	       PROJ_DIR = "/Users/demigorgan/brigadier";
	       
	    	//Scan the repository - gets java, test, class, and jar files. 
	       RepoScanner rs = new RepoScanner(PROJ_DIR);
	       
	       // in memory database handler
	       rh =  RedisHandler.getInstane();
	       
	       List<SourceFile> allClass = rs.get_all_class_files();
	       List<SourceFile> allTestClass = rs.get_all_test_class_files();
	       
	       List<SourceFile> changedFiles = new ArrayList<SourceFile>();
	       List<String> changedEntities = new ArrayList<String>();
	       
	       //ByteCodeParser bp = new ByteCodeParser(allClass.get(11));
	       	       	       
	       for(int i=0;i<allClass.size();i++){
		       ChangeAnalyzer fc = new FileChangeAnalyzer(allClass.get(i).getPath());
		       if(fc.hasChanged())
		    	   changedFiles.add(allClass.get(i));
	       }
	       
	       for(int i=0;i<changedFiles.size();i++){
	    	   // for each changed class we check which field/method change
	    	   ClassChangeAnalyzer cc = new ClassChangeAnalyzer(changedFiles.get(i).getPath()); 
	    	   List<String> chEnt = cc.getChangedAttributes();
	    	   changedEntities.addAll(chEnt);
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
