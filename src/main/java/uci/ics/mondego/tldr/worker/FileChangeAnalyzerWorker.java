package uci.ics.mondego.tldr.worker;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;

import uci.ics.mondego.tldr.App;
import uci.ics.mondego.tldr.changeanalyzer.FileChangeAnalyzer;
import uci.ics.mondego.tldr.exception.DatabaseSyncException;

public class FileChangeAnalyzerWorker extends Worker{

	private final String fileToAnalyze;
	
	public FileChangeAnalyzerWorker(String filePath){
		this.fileToAnalyze = filePath;
	}
	
	public FileChangeAnalyzerWorker(String workerName, String filePath){
		super(workerName);
		this.fileToAnalyze = filePath;
	}
	
	public void run() {
		try {			
            this.changeAnalyzer();
        } 		
		catch (NoSuchElementException e) {
            e.printStackTrace();
        } 
		
		catch (IllegalArgumentException e) {
            e.printStackTrace();
        } 
		
		catch (SecurityException e) {
            e.printStackTrace();
        }		
	}
	
	public void changeAnalyzer(){
		FileChangeAnalyzer fc;
		try {
			fc = new FileChangeAnalyzer(fileToAnalyze);
			if(fc.hasChanged()){ 	   
		        App.EntityChangeAnalysisPool.send(fileToAnalyze);
			}
		} 
		
		catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
