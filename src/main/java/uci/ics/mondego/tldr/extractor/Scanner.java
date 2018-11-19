package uci.ics.mondego.tldr.extractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Scanner {
	
	private String PROJ_DIR;
	
	List<String> all_java_files;
	List<String> all_jar_files;
	List<String> all_class_files;
	
	
	public Scanner(String project_directory){
		PROJ_DIR = project_directory;
		all_java_files = new ArrayList<String>();
		all_jar_files = new ArrayList<String>();
		all_class_files = new ArrayList<String>();
		
		this.listf(PROJ_DIR);
		
	}
	
	
	/* gets all file from the project directory*/
	
	public void listf(String directoryName) {
	    File directory = new File(directoryName);

	    File[] fList = directory.listFiles();
	    	
	    if(fList != null)
	        for (File file : fList) {    
	        	
	            if (file.isFile()) {
	            	
	                if(file.getAbsolutePath().contains(".java")){
	                	all_java_files.add(file.getAbsolutePath());
	                }
	                else if(file.getAbsolutePath().contains(".jar")){
	                	all_jar_files.add(file.getAbsolutePath());
	                }
	                if(file.getAbsolutePath().contains(".class")){
	                	all_class_files.add(file.getAbsolutePath());
	                }
	                	
	            } 
	            else if (file.isDirectory()) {
	                listf(file.getAbsolutePath());
	            }
	        }
	    }
	}


