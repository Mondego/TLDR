package uci.ics.mondego.tldr.map;

import java.util.Set;
import uci.ics.mondego.tldr.indexer.RedisHandler;
import uci.ics.mondego.tldr.tool.DatabaseIDs;

/**
 * This class maps a member to a test method.
 * @author demigorgan
 *
 */
public class EntityToTestMap {
	private RedisHandler database;
	
	public EntityToTestMap() {
		this.database = new RedisHandler();
	}
	
	public Set<String> getTests(String entity){
		return database.getSet(DatabaseIDs.TABLE_ID_TEST_DEPENDENCY, entity);
	}
	
	public void closeRedis(){
		database.close();
	}
}
