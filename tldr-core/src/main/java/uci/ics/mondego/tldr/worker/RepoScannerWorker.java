package uci.ics.mondego.tldr.worker;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uci.ics.mondego.tldr.TLDR;

public class RepoScannerWorker extends Worker{
	private final String repoDir;
	private static final Logger logger = LogManager.getLogger(RepoScannerWorker.class);

	public RepoScannerWorker(String repo){
		this.repoDir = repo;		
	}
	
	public void run() {
		try {		 
			this.scanClassFiles(repoDir);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
	
	public void scanTestFiles(String directoryName) 
			throws InstantiationException, 
			IllegalAccessException,
			IllegalArgumentException, 
			InvocationTargetException, 
			NoSuchMethodException, 
			SecurityException {	
	
		File directory = new File(directoryName);
	    File[] fList = directory.listFiles();	    	
	    if(fList != null) {
	    	for (File file : fList) {    	        	
	            if (file.isFile()) {
	            	String fileAbsolutePath = file.getAbsolutePath();	
	                if(fileAbsolutePath.endsWith(".class")){
	                	TLDR.TestFileChangeAnalysisPool.send(fileAbsolutePath); 
	                }	                	         
	            } else if (file.isDirectory()) {
	                scanTestFiles(file.getAbsolutePath());
	            }
	        }
	    }	        
    }

	public void scanClassFiles(String directoryName) 
			throws InstantiationException, 
			IllegalAccessException,
			IllegalArgumentException, 
			InvocationTargetException, 
			NoSuchMethodException, 
			SecurityException {	
		
		File directory = new File(directoryName);
	    File[] fList = directory.listFiles();	    	
	    if(fList != null)
	        for (File file : fList) {    	        	
	            if (file.isFile()) {
	            	String fileAbsolutePath = file.getAbsolutePath();	
	                if(fileAbsolutePath.endsWith(".class")){
	                	//logger.debug(file.getName()+" found and sending to FileChangeAnalyzer");
	                	TLDR.FileChangeAnalysisPool.send(fileAbsolutePath);
	                }	                	         
	            } else if (file.isDirectory() 
	            		&& !TLDR.allTestDirectories.containsKey(file.getAbsolutePath().toString())) {
	            		//!file.getAbsolutePath().equals(App.getTEST_DIR())) {
	                scanClassFiles(file.getAbsolutePath());
	            }
	        }
	    }	
}
