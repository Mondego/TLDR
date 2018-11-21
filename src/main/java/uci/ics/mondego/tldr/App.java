package uci.ics.mondego.tldr;

import java.util.List;

import uci.ics.mondego.tldr.extractor.ASTBuilder;
import uci.ics.mondego.tldr.extractor.Scanner;
import uci.ics.mondego.tldr.tool.RedisHandler;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
       Scanner sc = new Scanner("/Users/demigorgan/Sourcerer");
       
       RedisHandler rd = new RedisHandler();
       
       
       List<uci.ics.mondego.tldr.model.File> allJava = sc.get_all_java_files();
       
       for(int i=0;i<allJava.size();i++){
    	   
    	   if(!rd.exists(allJava.get(i).getName())){
    		   System.out.println("file inserted");
    		   rd.insert(allJava.get(i).getName(), allJava.get(i).getCurrentCheckSum());
    	   }
    	   
    	   else if(!allJava.get(i).getCurrentCheckSum().equals(rd.getValue(allJava.get(i).getName()))){
    		   System.out.println(allJava.get(i).getName()+" has changed");
    		   rd.insert(allJava.get(i).getName(), allJava.get(i).getCurrentCheckSum());
    	   }
       }
       
       
    }
}
