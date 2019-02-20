package uci.ics.mondego.tldr.worker;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;

import uci.ics.mondego.tldr.App;
import uci.ics.mondego.tldr.changeanalyzer.FileChangeAnalyzer;

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
		// TODO Auto-generated method stub
		try {
			
            this.changeAnalyzer();
            
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }		
	}
	
	public void changeAnalyzer(){
		FileChangeAnalyzer fc;		
		try {			
			fc = new FileChangeAnalyzer(fileToAnalyze);
			if(fc.hasChanged()){ 	   
		        App.testParseAndIndex.send(fileToAnalyze);
			}
		} 
		catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
}