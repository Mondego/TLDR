package uci.ics.mondego.tldr.extractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Scanner {
	
	private String PROJ_DIR;
	
	
	List<uci.ics.mondego.tldr.model.File> all_java_files;
	List<uci.ics.mondego.tldr.model.File> all_jar_files;
	List<uci.ics.mondego.tldr.model.File> all_class_files;
	
	
	
	public Scanner(String project_directory){
		PROJ_DIR = project_directory;
		all_java_files = new ArrayList<uci.ics.mondego.tldr.model.File>();
		all_jar_files = new ArrayList<uci.ics.mondego.tldr.model.File>();
		all_class_files = new ArrayList<uci.ics.mondego.tldr.model.File>();
		
		this.listf(PROJ_DIR);
	}
	
	
	public String get_PROJ_DIR() {
		return PROJ_DIR;
	}

	public void set_PROJ_DIR(String pROJ_DIR) {
		PROJ_DIR = pROJ_DIR;
	}

	public List<uci.ics.mondego.tldr.model.File> get_all_java_files() {
		return all_java_files;
	}

	public List<uci.ics.mondego.tldr.model.File> get_all_jar_files() {
		return all_jar_files;
	}

	public List<uci.ics.mondego.tldr.model.File> get_all_class_files() {
		return all_class_files;
	}
	
	
	/* gets all file from the project directory*/
	public void listf(String directoryName) {
	    File directory = new File(directoryName);

	    File[] fList = directory.listFiles();
	    	
	    if(fList != null)
	        for (File file : fList) {    
	        	
	            if (file.isFile()) {
	            	uci.ics.mondego.tldr.model.File f = new uci.ics.mondego.tldr.model.File(file.getAbsolutePath());
	                if(file.getAbsolutePath().contains(".java")){
	                	all_java_files.add(f);
	                }
	                else if(file.getAbsolutePath().contains(".jar")){
	                	
	                	all_jar_files.add(f);
	                }
	                if(file.getAbsolutePath().contains(".class")){
	                	
	                	all_class_files.add(f);
	                }
	                	
	            } 
	            else if (file.isDirectory()) {
	                listf(file.getAbsolutePath());
	            }
	        }
	    }
	}


