package uci.ics.mondego.tldr.experiments;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import uci.ics.mondego.tldr.tool.ConfigLoader;
import uci.ics.mondego.tldr.tool.FindAllTestDirectory;

public class IntraTestDependencyExperiment {

	private static String CLASS_DIR;
	public static int count = 0;
	private static Set<String> allTestClass;
	private static Set<String> allSureFireReport;
	private static Set<String> allAnnotatedTestMethod;
	private static Set<String> allHelperMethod;
	private static Set<String> allFields;

	public static void main(String[] args){
    	ConfigLoader config = new ConfigLoader();
	    CLASS_DIR = config.getCLASS_DIR();
	    //allTestEntities = database.getAllKeys("9*");
	    allTestClass = new HashSet<String>();
	    allAnnotatedTestMethod = new HashSet<String>();
	    allHelperMethod = new HashSet<String>();
	    allFields = new HashSet<String>();
	    allSureFireReport = new HashSet<String>();
	    
	   FindAllTestDirectory find = new FindAllTestDirectory(CLASS_DIR);
       Set<String> allTestDir = find.getAllTestDir();
       for(String s: allTestDir){
    	    scanFilesWithPattern(s, ".class", allTestClass);
       }    
	    
       for(String s: allTestClass){
    	   TestParser tp = new TestParser(s);
    	   allAnnotatedTestMethod.addAll(tp.testMethods);
    	   allHelperMethod.addAll(tp.helperMethods);
    	   allFields.addAll(tp.fields);
       }
       
       scanFilesWithPattern(CLASS_DIR, "surefire-report.html", allSureFireReport);
 	    String tests="";       
       for(String str: allSureFireReport){
    	   System.out.println(str);
     	    SureFireReportParserUtilities su = new SureFireReportParserUtilities(str);
      	    for(String s: su.testCases){
      	    	String ss = s.substring(0, s.lastIndexOf('.')) + "#" + s.substring(s.lastIndexOf('.')+1);
      	    	tests+=(ss+"\n");
      	    }
       }      
  	    writeLog("log.txt", tests);
	    print(allHelperMethod.size());
	    print(allAnnotatedTestMethod.size());
	    print(allAnnotatedTestMethod.size());
	    print(allFields.size());
	}
	
	private static void writeLog(String filename, String log){
		try {
			File file = new File(filename);
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(log);			
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void scanFilesWithPattern(String directoryName, String sufix, Set<String> setToAdd)  {	

		File directory = new File(directoryName);
	    File[] fList = directory.listFiles();	    	
	    if(fList != null)
	        for (File file : fList) {    	        	
	            if (file.isFile()) {
	            	String fileAbsolutePath = file.getAbsolutePath();	
	                if(fileAbsolutePath.endsWith(sufix)){
	                	setToAdd.add(fileAbsolutePath); 
	                }	                	         
	            } 
	            
	            else if (file.isDirectory()) {
	                scanFilesWithPattern(file.getAbsolutePath(), sufix, setToAdd);
	            }
	        }
	}
	
	
	
	private static int countTotalTestEntity(){
		return -1;
	}
	
	private static void print(Set<String> setToPrint){
		for(String str: setToPrint){
			System.out.println(str);
		}
	}
	
	private static void print(int val){
		
			System.out.println(val);
		
	}
	
}
