package uci.ics.mondego.tldr.extractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uci.ics.mondego.tldr.model.ClassFile;
import uci.ics.mondego.tldr.model.JarFile;
import uci.ics.mondego.tldr.model.JavaFile;
import uci.ics.mondego.tldr.model.SourceFile;
import uci.ics.mondego.tldr.model.TestCases;
import uci.ics.mondego.tldr.model.TestFile;


public class RepoScanner {
	
	private String PROJ_DIR;
	
	
	List<SourceFile> java_files;
	List<SourceFile> test_files;
	List<SourceFile> jar_files;
	List<SourceFile> class_files;
	
	
	
	public RepoScanner(){
		this.java_files = new ArrayList<SourceFile>();
		this.jar_files = new ArrayList<SourceFile>();
		this.class_files = new ArrayList<SourceFile>();
		this.test_files = new ArrayList<SourceFile>();
	}
	
	public RepoScanner(String project_directory){
		this.PROJ_DIR = project_directory;
		
		this.java_files = new ArrayList<SourceFile>();
		this.jar_files = new ArrayList<SourceFile>();
		this.class_files = new ArrayList<SourceFile>();
		this.test_files = new ArrayList<SourceFile>();
		
		this.listf(PROJ_DIR);
	}
	
	
	public String get_PROJ_DIR() {
		return PROJ_DIR;
	}

	public void set_PROJ_DIR(String pROJ_DIR) {
		PROJ_DIR = pROJ_DIR;
	}

	public List<SourceFile> get_all_java_files() {
		return java_files;
	}

	public List<SourceFile> get_all_jar_files() {
		return jar_files;
	}

	public List<SourceFile> get_all_class_files() {
		return class_files;
	}
	
	public List<SourceFile> get_all_test_files() {
		return test_files;
	}
	
	
	/* gets all file from the project directory*/
	public void listf(String directoryName) {
	    
		File directory = new File(directoryName);

	    File[] fList = directory.listFiles();
	    	
	    if(fList != null)
	        for (File file : fList) {    
	        	
	            if (file.isFile()) {
	            	String fileAbsolutePath = file.getAbsolutePath();
	            	
	                if(fileAbsolutePath.contains(".java")){
	                	JavaFile f = new JavaFile(fileAbsolutePath);
	                	java_files.add(f);
	                }
	                else if(fileAbsolutePath.contains(".jar")){
	                	JarFile f = new JarFile(fileAbsolutePath);
	                	jar_files.add(f);
	                }

	                else if(file.getAbsolutePath().contains(".class")){
	                	ClassFile f = new ClassFile(fileAbsolutePath);
	                	class_files.add(f);
	                }
	                
	                else if(file.getAbsolutePath().contains(".Test")){
	                	TestFile f = new TestFile(fileAbsolutePath);
	                	test_files.add(f);
	                }
	                	
	            } 
	            else if (file.isDirectory()) {
	                listf(file.getAbsolutePath());
	            }
	        }
	    }
	}


