package uci.ics.mondego.tldr.worker;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;

import uci.ics.mondego.tldr.App;
import uci.ics.mondego.tldr.changeanalyzer.FileChangeAnalyzer;
import uci.ics.mondego.tldr.exception.DatabaseSyncException;

public class TestFileChangeAnalyzerWorker extends Worker{

	private final String fileToAnalyze;
	
	public TestFileChangeAnalyzerWorker(String filePath){
		this.fileToAnalyze = filePath;
	}
	
	public TestFileChangeAnalyzerWorker(String workerName, String filePath){
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
		        App.TestParseAndIndexPool.send(fileToAnalyze);
			}
		} 
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (InstantiationException e) {
			e.printStackTrace();
		} 
		catch (IllegalAccessException e) {
			e.printStackTrace();
		} 
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		catch (InvocationTargetException e) {
			e.printStackTrace();
		} 
		catch (NoSuchMethodException e) {
			e.printStackTrace();
		} 
		catch (SecurityException e) {
			e.printStackTrace();
		} 
		catch (DatabaseSyncException e) {
			e.printStackTrace();
		}
	}
}