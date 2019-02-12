package uci.ics.mondego.tldr.worker;

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
		this.map();
		
	}
	
	private void map(){
	   EntityToTestMap map = new EntityToTestMap();
	   Set<String> tests = map.getTests(entity);
 	   for(String str: tests){
 		   if(!App.testToRun.contains(str)){
 			   App.testToRun.put(str, true);
 			   System.out.println(str);
 		   }
 	   }
	}
	

}
