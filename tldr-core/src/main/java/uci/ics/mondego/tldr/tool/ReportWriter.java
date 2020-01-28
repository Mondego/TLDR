package uci.ics.mondego.tldr.tool;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.Map.Entry;

import uci.ics.mondego.tldr.model.Report;

public class ReportWriter {
	
    public void logExperiment(String logFileName, Report report, double testRunTimeInSecond) { 	
    	PrintWriter writer1;
		try {
			writer1 = new PrintWriter(logFileName, "UTF-8");
			writer1.println("NUMBER OF NEW OR CHANGED ENTITIES : "+ report.getNewOrChangedEntities().size());	   
			writer1.println("NUMBER OF ENTITY TO TEST : "+report.getEntitiesToTest().size());	   
			writer1.println("NUMBER OF TEST TO RUN : "+report.getTestsToRun().size());	   
			writer1.println("TOTAL TIME REQUIRED TO SELECT TEST: "+report.getSelectionTimeInSecond()+" second");	
			writer1.println("TOTAL TIME REQUIRED TO RUN TEST: " + testRunTimeInSecond + "second");
			writer1.println("======================================================");
			writer1.println("======================================================");
			
	    	Set<Entry<String, Boolean>> allEntries = report.getNewOrChangedEntities().entrySet();
			writer1.println("NEW OR CHANGED ENTITIES : \n\n");	   
	    	for (Entry<String, Boolean> e: allEntries) {
	    		writer1.println(e.getKey());
	    	}
	    	
	    	writer1.println("======================================================");
			writer1.println("======================================================");
						
			allEntries = report.getEntitiesToTest().entrySet();
			writer1.println("ALL ENTITY TO TEST : \n\n");	   
	    	for(Entry<String, Boolean> e: allEntries){
	    		writer1.println(e.getKey());
	    	}
	    	
	    	writer1.println("======================================================");
			writer1.println("======================================================");

			writer1.println("ALL TESTS TO RUN : \n\n");	   
	    	for(Entry<String, Integer> e: report.getTestsToRun().entrySet()){
	    		writer1.println(e.getKey());
	    	}	    	
	    	writer1.close();
		} 
		catch (FileNotFoundException fileNotFoundException) {
	    	fileNotFoundException.printStackTrace();
		} catch (UnsupportedEncodingException unsupportedEncodingException) {
			unsupportedEncodingException.printStackTrace();
		}    	
    }
    
    public void logExperiment(String logFileName, double testRunTimeInSecond) { 	
    	PrintWriter writer1;
		try {
			writer1 = new PrintWriter(logFileName, "UTF-8");
			writer1.println("TOTAL TIME REQUIRED TO RUN TEST: " + testRunTimeInSecond + "second");
	    	writer1.close();
		} 
		catch (FileNotFoundException fileNotFoundException) {
	    	fileNotFoundException.printStackTrace();
		} catch (UnsupportedEncodingException unsupportedEncodingException) {
			unsupportedEncodingException.printStackTrace();
		}    	
    }
    
    public void logError(String errorFileName, String message, String claz) {
    	PrintWriter writer1;
		try {
			
			writer1 = new PrintWriter(errorFileName, "UTF-8");
			writer1.println(Constants.DISTINCTION_LINE_EQUAL);
			writer1.println( "ERROR : "+ message);
			writer1.println( "CLASS : "+ claz);
			writer1.println(Constants.DISTINCTION_LINE_EQUAL);
			
	    	writer1.close();
		} 
		catch (FileNotFoundException fileNotFoundException) {
	    	fileNotFoundException.printStackTrace();
		} catch (UnsupportedEncodingException unsupportedEncodingException) {
			unsupportedEncodingException.printStackTrace();
		}   
    }
}
