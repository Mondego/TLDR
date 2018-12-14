package uci.ics.mondego.tldr;


import uci.ics.mondego.tldr.tool.RedisHandler;

import java.util.List;

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
    public static void main( String[] args )
    {
       RepoScanner sc = new RepoScanner("/Users/demigorgan/Sourcerer");
       
       RedisHandler rd = new RedisHandler();
       
       
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
       }
       
       
  
       
    }
}
