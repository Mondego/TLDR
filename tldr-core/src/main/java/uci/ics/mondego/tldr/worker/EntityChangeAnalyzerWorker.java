package uci.ics.mondego.tldr.worker;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.NoSuchElementException;
import org.apache.bcel.classfile.Method;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import uci.ics.mondego.tldr.TLDR;
import uci.ics.mondego.tldr.changeanalyzer.EntityChangeAnalyzer;
import uci.ics.mondego.tldr.exception.DatabaseSyncException;

public class EntityChangeAnalyzerWorker extends Worker{

	private final String className;
	private static final Logger logger = LogManager.getLogger(EntityChangeAnalyzerWorker.class);
	
	public EntityChangeAnalyzerWorker( String className){
		this.className = className;
	}
	
	public EntityChangeAnalyzerWorker(String name, String className){
		super(name);
		this.className = className;
	}
	
	public void run() {
		try {
            this.changeAnalyzer();
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }	
	}
	
	private void changeAnalyzer(){
	    EntityChangeAnalyzer cc;
		try {
			cc = new EntityChangeAnalyzer(className);
			HashMap<String, Method> m = cc.getChangedMethods();	 
	 		if(m.size() > 0){
	 			//logger.debug(className.substring(className.lastIndexOf("/"))
	 			//		+" -- Some method changed and sent to DependencyExtractionPool");
	 			
	 			TLDR.allChangedOrNewMethods.putAll(m);
	 				 			
	 			//App.DependencyExtractionPool.send(m);	
	 		}
		} 
		catch (IOException e1) {
			e1.printStackTrace();
		} 
		catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} 
		catch (SecurityException e1) {
			e1.printStackTrace();
		} 
		catch (DatabaseSyncException e) {
			e.printStackTrace();
		} 	 	      
	}	
}
