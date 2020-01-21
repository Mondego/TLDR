package uci.ics.mondego.tldr.changeanalyzer;

import java.io.IOException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import redis.clients.jedis.exceptions.JedisConnectionException;
import uci.ics.mondego.tldr.exception.DatabaseSyncException;
import uci.ics.mondego.tldr.exception.NullDbIdException;
import uci.ics.mondego.tldr.indexer.RedisHandler;

/**
 * Base class for file and member change analyzer.
 * @author demigorgan
 *
 */
public abstract class ChangeAnalyzer {
	
	/*** TABLE ID : FILE - CHECKSUM ---- 1
	 * 				ENTITY - HASHCODE ---- 2
	 * 				ENTITY - LIST OF DEPENDENTS ---- 3
	 */
	protected static final Logger logger = LogManager.getLogger(ChangeAnalyzer.class);
	private final String entityName;
	private boolean changed;
	private boolean isSynced;
	protected RedisHandler redisHandler;
	
	public ChangeAnalyzer(String className) {
		this.entityName = className;
		this.changed = false;
		this.isSynced = false;
		this.redisHandler = new RedisHandler();
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
		redisHandler.close();
	}
	
	protected boolean sync(String tableId, String name, String newCheckSum){
		try {
			redisHandler.update(tableId, name, newCheckSum);
			this.isSynced = true;
		} catch (JedisConnectionException e) {
			e.printStackTrace();
		} catch (NullDbIdException e) {
			e.printStackTrace();
		}
		return isSynced;
	}
	
	protected String getValue(String tableId, String key){
		return redisHandler.getValueByKey(tableId, key);
	}
	
	protected boolean exists(String tableId, String key){
		return redisHandler.exists(tableId, key);
	}
	
	protected abstract void parse() throws IOException, DatabaseSyncException;
}
