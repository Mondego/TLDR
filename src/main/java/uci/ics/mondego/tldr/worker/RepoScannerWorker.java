package uci.ics.mondego.tldr.worker;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import uci.ics.mondego.tldr.App;
import uci.ics.mondego.tldr.model.ClassFile;
import uci.ics.mondego.tldr.model.SourceFile;

public class RepoScannerWorker extends Worker{

	private final String repoDir;
		
	private List<SourceFile> java_files;
	private List<SourceFile> test_java_files;
	private List<SourceFile> test_class_files;
	private List<SourceFile> jar_files;
	private List<SourceFile> class_files;
	
	
	public RepoScannerWorker(String repo){
		this.repoDir = repo;
		this.java_files = new ArrayList<SourceFile>();
		this.jar_files = new ArrayList<SourceFile>();
		this.class_files = new ArrayList<SourceFile>();
		this.test_java_files = new ArrayList<SourceFile>();
		this.test_class_files = new ArrayList<SourceFile>();
	}
	
	public void run() {
		// TODO Auto-generated method stub
		try {
			 
			this.scan(repoDir);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void scan(String directoryName) throws InstantiationException, IllegalAccessException,
	    IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {	
		
		File directory = new File(directoryName);
	    File[] fList = directory.listFiles();	    	
	    if(fList != null)
	        for (File file : fList) {    	        	
	            if (file.isFile()) {
	            	String fileAbsolutePath = file.getAbsolutePath();	
	                if(fileAbsolutePath.endsWith(".class")){
	                	ClassFile f = new ClassFile(fileAbsolutePath);
	                	App.changedFiles.send(fileAbsolutePath);	                	
	                	class_files.add(f);
	                }	                	         
	            } 
	            else if (file.isDirectory()) {
	                scan(file.getAbsolutePath());
	            }
	        }
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
}
