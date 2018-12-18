package uci.ics.mondego.tldr;


import uci.ics.mondego.tldr.tool.RedisHandler;
import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.exceptions.JedisConnectionException;
import uci.ics.mondego.tldr.extractor.ByteCodeParser;
import uci.ics.mondego.tldr.extractor.JavaFileParser;
import uci.ics.mondego.tldr.extractor.RepoScanner;
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
       try{
	       PROJ_DIR = "/Users/demigorgan/brigadier";
	       
	    	//Scan the repository - gets java, test, class, and jar files. 
	       
	       RepoScanner rs = new RepoScanner(PROJ_DIR);
	       
	       
	       // in memory database handler
	       RedisHandler rh = new RedisHandler();
	       
	       List<SourceFile> allClass = rs.get_all_class_files();
	       List<SourceFile> allTestClass = rs.get_all_test_class_files();
	       
	       List<SourceFile> changedFiles = new ArrayList<SourceFile>();
	       
	       for(int i=0;i<allClass.size();i++){
	    	   
	    	   if(!rh.exists(allClass.get(i).getPath())){
	    		   System.out.println("file inserted");
	    		   rh.insert(allClass.get(i).getPath(), allClass.get(i).getCurrentCheckSum());
	    	   }
	    	   else{
	    		   String currentCheckSum = allClass.get(i).getCurrentCheckSum();
	    		   String prevCheckSum = rh.getValue(allClass.get(i).getPath());
	    		   
	    		   if(!currentCheckSum.equals(prevCheckSum)){
	        		   System.out.println("\n"+allClass.get(i).getPath()+" has changed");
	        		   
	        		   System.out.println(currentCheckSum+"   "+prevCheckSum);
	        		   
	        		   changedFiles.add(allClass.get(i));
	        		   rh.insert(allClass.get(i).getPath(), currentCheckSum);
	        		   
	    		   }
	    	   }
	    	   
	       }
       }
       
       catch( JedisConnectionException e){
    	   System.out.println("No Connection to Jedis Server");
    	   e.printStackTrace();   
       }
       
       
      /* 
       
       
       ByteCodeParser b = new ByteCodeParser();

       
       List<SourceFile> allJava = sc.get_all_java_files();
       
       List<SourceFile> allClass =  sc.get_all_class_files();
       
       for(int i=0;i<allClass.size();i++){
    	   
    	  if(i== 0){ 
    		  //JavaFileParser ast = new JavaFileParser(allClass.get(i).getName());
    		  //ast.AST();
    	  }
    	   
    	   if(!rd.exists(allClass.get(i).getName())){
    		   //System.out.println("file inserted");
    		   rd.insert(allClass.get(i).getName(), allClass.get(i).getCurrentCheckSum());
    	   }
    	   
    	   else if(!allClass.get(i).getCurrentCheckSum().equals(rd.getValue(allClass.get(i).getName()))){
    		   //System.out.println(allClass.get(i).getName()+" has changed");
    		   rd.insert(allClass.get(i).getName(), allClass.get(i).getCurrentCheckSum());
    		   
    	   }
       }
       
       
      for(int i=0;i<allClass.size();i++){
    	   
    	   if(!rd.exists(allClass.get(i).getName())){
    		   System.out.println("file inserted");
    		   rd.insert(allClass.get(i).getName(), allClass.get(i).getCurrentCheckSum());
   	   }
    	   
    	   else if(!allClass.get(i).getCurrentCheckSum().equals(rd.getValue(allClass.get(i).getName()))){
    		   //System.out.println(allClass.get(i).getName()+" has changed");
    		   rd.insert(allClass.get(i).getName(), allClass.get(i).getCurrentCheckSum());
    		   //TreeBuilder ast = new TreeBuilder(allClass.get(i).getName());
    		   //ast.AST();
    	   }
       }*/
    	
    }
}
