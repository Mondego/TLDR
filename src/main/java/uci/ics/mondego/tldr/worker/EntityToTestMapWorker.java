package uci.ics.mondego.tldr.worker;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import uci.ics.mondego.tldr.App;
import uci.ics.mondego.tldr.map.EntityToTestMap;

public class EntityToTestMapWorker extends Worker{
	
	private final String entity;
	
	public EntityToTestMapWorker(String entity){
		this.entity = entity;
	}

	public void run() {
		// TODO Auto-generated method stub
		try {
			this.map();
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
	
	private void map() throws InstantiationException, 
	    IllegalAccessException, IllegalArgumentException, InvocationTargetException, 
	    NoSuchMethodException, SecurityException{
	   EntityToTestMap map = new EntityToTestMap();
	   Set<String> tests = map.getTests(entity);
 	   
	   for(String str: tests){
 		   if(!App.testToRun.containsKey(str)){
 			   App.testToRun.put(str, true);
 		   }
 	   }  
 	   
	   map.closeRedis();
	}
}
