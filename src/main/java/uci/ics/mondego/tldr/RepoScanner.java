package uci.ics.mondego.tldr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uci.ics.mondego.tldr.model.ClassFile;
import uci.ics.mondego.tldr.model.SourceFile;
import uci.ics.mondego.tldr.model.TestClassFile;
import uci.ics.mondego.tldr.model.TestJavaFile;


public class RepoScanner {


	private String PROJ_DIR;
	
	
	private List<SourceFile> java_files;
	private List<SourceFile> test_java_files;
	private List<SourceFile> test_class_files;
	private List<SourceFile> jar_files;
	private List<SourceFile> class_files;
	
	public RepoScanner(){
		this.java_files = new ArrayList<SourceFile>();
		this.jar_files = new ArrayList<SourceFile>();
		this.class_files = new ArrayList<SourceFile>();
		this.test_java_files = new ArrayList<SourceFile>();
		this.test_class_files = new ArrayList<SourceFile>();
	}
	
	public RepoScanner(String project_directory){
		this.PROJ_DIR = project_directory;
		
		this.java_files = new ArrayList<SourceFile>();
		this.jar_files = new ArrayList<SourceFile>();
		this.class_files = new ArrayList<SourceFile>();
		this.test_java_files = new ArrayList<SourceFile>();
		this.test_class_files = new ArrayList<SourceFile>();
		
		this.scan(PROJ_DIR);
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
	
	public List<SourceFile> get_all_test_java_files() {
		return test_java_files;
	}
	
	public List<SourceFile> get_all_test_class_files() {
		return test_class_files;
	}
	
	
	/* gets all file from the project directory*/
	public void scan(String directoryName) {		
		File directory = new File(directoryName);
	    File[] fList = directory.listFiles();	    	
	    if(fList != null)
	        for (File file : fList) {    
	        	
	            if (file.isFile()) {
	            	String fileAbsolutePath = file.getAbsolutePath();
	            	
	                if(file.getAbsolutePath().endsWith(".class") && !fileAbsolutePath.contains("Test")){
	                	//System.out.println(file.getAbsolutePath());
	                	ClassFile f = new ClassFile(fileAbsolutePath);
	                	class_files.add(f);
	                }
	                
	                else if(fileAbsolutePath.endsWith(".java") && fileAbsolutePath.contains("Test")){
	                	TestJavaFile f = new TestJavaFile(fileAbsolutePath);
	                	test_java_files.add(f);
	                }
	                
	                else if(fileAbsolutePath.endsWith(".class") && fileAbsolutePath.contains("Test")){
	                	TestClassFile f = new TestClassFile(fileAbsolutePath);
	                	test_class_files.add(f);
	                }	
	            } 
	            else if (file.isDirectory()) {
	                scan(file.getAbsolutePath());
	            }
	        }
	    }
	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuilder printLog = new StringBuilder();
		
		printLog.append("\nJava Files\n*********************\n");
		for(int i=0;i<java_files.size();i++)
			printLog.append(java_files.get(i).getName()+";");
		
		printLog.append("\nTest Java Files\n*********************\n");
		for(int i=0;i<test_java_files.size();i++)
			printLog.append(test_java_files.get(i).getName()+";");
		
		printLog.append("\nTest Class Files\n*********************\n");
		for(int i=0;i<test_class_files.size();i++)
			printLog.append(test_class_files.get(i).getName()+"\n");
				
		printLog.append("\nJar Files\n*********************\n");
		for(int i=0;i<jar_files.size();i++)
			printLog.append(jar_files.get(i).getName()+";");
		
		printLog.append("\nClass Files\n*********************\n");
		for(int i=0;i<class_files.size();i++)
			printLog.append(class_files.get(i).getName()+";");
		
		return printLog.toString();
	}

}


