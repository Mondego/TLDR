package uci.ics.mondego.tldr.metainformation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import uci.ics.mondego.tldr.tool.ConfigLoader;

public class ExtractMetaInformation {
	
	private static Set<String> allClasses = new HashSet<String>();
	private static Set<String> allJavaFiles = new HashSet<String>();

	
	public static void main( String[] args )
    {
		ConfigLoader config = new ConfigLoader(); 
		scanRepo(config.getCLASS_DIR());
		
		StringBuilder sb = new StringBuilder();
		sb.append("\nAll Class File : " + allClasses.size()+"\n");
		sb.append("All Java File : "+allJavaFiles.size()+"\n");
		sb.append("Database Size Currently: "+getDatabaseSize()+"\n");
		
		// args[0] is the commit hashcode
		String fileName = args[0];
		writeLog(fileName, sb.toString());
    }
	
	public static double getDatabaseSize(){
		File file =new File("/Users/demigorgan/dump.rdb");
		double kilobytes = 0;
		if(file.exists()){
			double bytes = file.length();
			kilobytes = (bytes / 1024);
		}
		return kilobytes;
	}
	
	public static void scanRepo(String directoryName)  {	

		File directory = new File(directoryName);
	    File[] fList = directory.listFiles();	    	
	    if(fList != null)
	        for (File file : fList) {    	        	
	            if (file.isFile()) {
	            	String fileAbsolutePath = file.getAbsolutePath();	
	                if(fileAbsolutePath.endsWith(".class")){
	                	allClasses.add(fileAbsolutePath);
	                }	                	         
	                if(fileAbsolutePath.endsWith(".java")){
	                	allJavaFiles.add(fileAbsolutePath);
	                }
	            } 
	            
	            else if (file.isDirectory()) {
	            	scanRepo(file.getAbsolutePath());
	            }
	        }
	}
	
	private static void writeLog(String filename, String log){
		try {
			File file = new File(filename);
			FileWriter fileWriter = new FileWriter(file, true);
			fileWriter.write(log);			
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
