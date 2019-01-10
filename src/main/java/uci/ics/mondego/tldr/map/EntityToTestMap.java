package uci.ics.mondego.tldr.map;

import java.util.Set;

import uci.ics.mondego.tldr.indexer.RedisHandler;
import uci.ics.mondego.tldr.tool.Databases;

public class EntityToTestMap {
	
	private RedisHandler rh;
	
	public EntityToTestMap(){
		this.rh = RedisHandler.getInstane();
	}
	
	public Set<String> getTests(String entity){
		return rh.getSet(Databases.TABLE_ID_TEST_DEPENDENCY, entity);
	}

}
