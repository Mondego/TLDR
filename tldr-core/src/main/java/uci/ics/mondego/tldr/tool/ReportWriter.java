package uci.ics.mondego.tldr.tool;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.Map.Entry;

import uci.ics.mondego.tldr.TLDR;

public class ReportWriter {
	/**
     * 
     * @param pair
     * @param project
     * @param commit
     */
	private String pair;
	private String project;
	private String commit;
	
	public ReportWriter(String pair, String project, String commit) {
		this.pair = pair;
		this.project = project;
		this.commit = commit;
	}
	
    public void logExperiment(String elapsedTimeInSecond){ 	
    	//System.out.println("GENERATING REPORT FOR "+project+" Commit: "+commit);	           	
    	PrintWriter writer1;
		try {
			writer1 = new PrintWriter(project+"_"+pair+"_REPORT_"+commit+"_.txt", "UTF-8");
			writer1.println("NUMBER OF NEW OR CHANGED ENTITIES : "+TLDR.allNewAndChangedentities.size());	   
			writer1.println("NUMBER OF ENTITY TO TEST : "+TLDR.entityToTest.size());	   
			writer1.println("NUMBER OF TEST TO RUN : "+TLDR.completeTestCaseSet.size());	   
			writer1.println("TOTAL TIME REQUIRED : "+elapsedTimeInSecond+" second");	
			writer1.println("======================================================");
			writer1.println("======================================================");
			
	    	Set<Entry<String, Boolean>> allEntries = TLDR.allNewAndChangedentities.entrySet();
			writer1.println("NEW OR CHANGED ENTITIES : \n\n");	   
	    	for (Entry<String, Boolean> e: allEntries) {
	    		writer1.println(e.getKey());
	    	}
	    	
	    	writer1.println("======================================================");
			writer1.println("======================================================");
						
			allEntries = TLDR.entityToTest.entrySet();
			writer1.println("ALL ENTITY TO TEST : \n\n");	   
	    	for(Entry<String, Boolean> e: allEntries){
	    		writer1.println(e.getKey());
	    	}
	    	
	    	writer1.println("======================================================");
			writer1.println("======================================================");
			
			writer1.println("ALL TESTS TO RUN : \n\n");	   
	    	for(Entry<String, Integer> e: TLDR.completeTestCaseSet.entrySet()){
	    		writer1.println(e.getKey());
	    	}	    	
	    	writer1.close();
		} 
		catch (FileNotFoundException fileNotFoundException) {
	    	fileNotFoundException.printStackTrace();
		} 
		catch (UnsupportedEncodingException unsupportedEncodingException) {
			unsupportedEncodingException.printStackTrace();
		}    	
    }
}
