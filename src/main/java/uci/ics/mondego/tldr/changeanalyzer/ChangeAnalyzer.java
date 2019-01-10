package uci.ics.mondego.tldr.changeanalyzer;

import java.io.IOException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import uci.ics.mondego.tldr.indexer.RedisHandler;

public abstract class ChangeAnalyzer {
	
	/*** TABLE ID : FILE - CHECKSUM ---- 1
	 * 				ENTITY - HASHCODE ---- 2
	 * 				ENTITY - LIST OF DEPENDENTS ---- 3
	 */

	protected static final Logger logger = LogManager.getLogger(ClassChangeAnalyzer.class);
	private final String entityName;
	private boolean changed;
	private boolean isSynced;
	protected RedisHandler rh;
	
	public ChangeAnalyzer(String className){
		this.entityName = className;
		this.changed = false;
		this.isSynced = false;
		this.rh = RedisHandler.getInstane();
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
	
	protected void sync(String tableId, String name, String newCheckSum){
		rh.update(tableId, name, newCheckSum);
		this.isSynced = true;
	}
	
	protected String getValue(String tableId, String key){
		return rh.getValueByKey(tableId, key);
	}
	
	protected boolean exists(String tableId, String key){
		return rh.exists(tableId, key);
	}
	
	protected abstract void parse() throws IOException;
}
