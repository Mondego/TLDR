package uci.ics.mondego.tldr.worker;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import uci.ics.mondego.tldr.App;
import uci.ics.mondego.tldr.model.ClassFile;

public class RepoScannerWorker extends Worker{

	private final String repoDir;

	public RepoScannerWorker(String repo){
		this.repoDir = repo;		
	}
	
	public void run() {
		// TODO Auto-generated method stub
		try {
			 
			this.scanClassFiles(repoDir);
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
	
	public void scanTestFiles(String directoryName) throws InstantiationException, IllegalAccessException,
    	IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {	
	
		File directory = new File(directoryName);
	    File[] fList = directory.listFiles();	    	
	    if(fList != null)
	        for (File file : fList) {    	        	
	            if (file.isFile()) {
	            	String fileAbsolutePath = file.getAbsolutePath();	
	                if(fileAbsolutePath.endsWith(".class")){
	                	ClassFile f = new ClassFile(fileAbsolutePath);
	                	App.changedTestFiles.send(fileAbsolutePath);        		                
	                }	                	         
	            } 
	            else if (file.isDirectory()) {
	                scanTestFiles(file.getAbsolutePath());
	            }
	        }
    }

	
	public void scanClassFiles(String directoryName) throws InstantiationException, IllegalAccessException,
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
	                }	                	         
	            } 
	            else if (file.isDirectory() && !file.getAbsolutePath().equals(App.TEST_DIR)) {
	                scanClassFiles(file.getAbsolutePath());
	            }
	        }
	    }	
}
