package uci.ics.mondego.tldr.worker;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import uci.ics.mondego.tldr.App;
import uci.ics.mondego.tldr.map.EntityToTestMap;

public class EntityToTestMapWorker extends Worker{
	
	private final String entity;
	private static final Logger logger = LogManager.getLogger(EntityToTestMapWorker.class);
	
	public EntityToTestMapWorker(String entity){
		this.entity = entity;
	}

	public void run() {
		// TODO Auto-generated method stub
		try {
			this.map();
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
		}
		
	}
	
	private void map() throws InstantiationException, 
	    IllegalAccessException, IllegalArgumentException, InvocationTargetException, 
	    NoSuchMethodException, SecurityException{
	   EntityToTestMap map = new EntityToTestMap();
	   Set<String> tests = map.getTests(entity);
 	   
	   for(String str: tests){
 		   logger.debug(str+" maps to "+entity+" and written to testToRun");	
		   App.testToRun.put(str, true);
 	   }  
 	   
	   map.closeRedis();
	}
}
