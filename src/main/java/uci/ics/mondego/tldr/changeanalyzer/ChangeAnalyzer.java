package uci.ics.mondego.tldr.changeanalyzer;

import java.io.IOException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import redis.clients.jedis.exceptions.JedisConnectionException;
import uci.ics.mondego.tldr.exception.DatabaseSyncException;
import uci.ics.mondego.tldr.exception.NullDbIdException;
import uci.ics.mondego.tldr.indexer.RedisHandler;

public abstract class ChangeAnalyzer {
	
	/*** TABLE ID : FILE - CHECKSUM ---- 1
	 * 				ENTITY - HASHCODE ---- 2
	 * 				ENTITY - LIST OF DEPENDENTS ---- 3
	 */
	protected static final Logger logger = LogManager.getLogger(ChangeAnalyzer.class);
	private final String entityName;
	private boolean changed;
	private boolean isSynced;
	protected RedisHandler database;
	
	public ChangeAnalyzer(String className) {
		this.entityName = className;
		this.changed = false;
		this.isSynced = false;
		this.database = new RedisHandler();
	}
	
	public boolean hasChanged(){
		return changed;
	}
	
	public String getEntityName(){
		return entityName;
	}
	
	protected void setChanged(boolean bool) {
		this.changed = bool;
	}
	
	public boolean hasSynced(){
		return isSynced;
	}
	
	protected void closeRedis(){
		database.close();
	}
	
	protected boolean sync(String tableId, String name, String newCheckSum){
		try {
			database.update(tableId, name, newCheckSum);
			this.isSynced = true;
			
		} catch (JedisConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullDbIdException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isSynced;
	}
	
	protected String getValue(String tableId, String key){
		return database.getValueByKey(tableId, key);
	}
	
	protected boolean exists(String tableId, String key){
		return database.exists(tableId, key);
	}
	
	protected abstract void parse() throws IOException, DatabaseSyncException;
}
