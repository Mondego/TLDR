package uci.ics.mondego.tldr;


import uci.ics.mondego.tldr.indexer.RedisHandler;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;

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
	       //ByteCodeParser bp = new ByteCodeParser(allClass.get(11));
	       
	       
	       //FileChangeAnalyzer fc = new FileChangeAnalyzer("/users/demigorgan/brigadier/build/classes/java/main/com/mojang/brigadier/context/CommandContext.class");
	       
	       //ClassChangeAnalyzer cha = new ClassChangeAnalyzer("/users/demigorgan/brigadier/build/classes/java/main/com/mojang/brigadier/context/CommandContext.class"); 
	       
	       for(int i=0;i<allClass.size();i++){
		       ChangeAnalyzer fc = new FileChangeAnalyzer(allClass.get(i).getPath());
		       if(fc.hasChanged())
		    	   changedFiles.add(allClass.get(i));
		       
		       
	    	   /*if(!rh.exists(allClass.get(i).getPath())){
	    		   
	    		   System.out.println("file inserted");
	    		   rh.insert(allClass.get(i).getPath(), allClass.get(i).getCurrentCheckSum());
	    	   }
	    	   else{
	    		   //System.out.println("file exists");
	    		   String currentCheckSum = allClass.get(i).getCurrentCheckSum();
	    		   String prevCheckSum = rh.getValue(allClass.get(i).getPath());
	    		   
	    		   if(!currentCheckSum.equals(prevCheckSum)){
	    		       ByteCodeParser bp = new ByteCodeParser(allClass.get(i));
	    		       
	    		       System.out.println("file changed "+allClass.get(i).getPath());
	        		   changedFiles.add(allClass.get(i));
	        		   rh.insert(allClass.get(i).getPath(), currentCheckSum);
	    		   }
	    	   }*/
	    	   
	       }
	       
	       for(int i=0;i<changedFiles.size();i++){
	    	   ChangeAnalyzer cc = new ClassChangeAnalyzer(changedFiles.get(i).getPath());
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
       
       
       finally{
    	   rh.close();
       }
    	
    }
}
