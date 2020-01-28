package uci.ics.mondego.tldr.indexer;

import java.util.Set;

import redis.clients.jedis.exceptions.JedisConnectionException;
import uci.ics.mondego.tldr.exception.NullDbIdException;

/**
 * API interface for Database.
 * @author demigorgan
 *
 */
public interface DatabaseHandler {
	
	Set<String> getAllMethodsAndFields(String projectId);
	
	Set<String> getAllKeys(String regex);
	
	void insertProject(String projectName);
	
	boolean projectExists(String projectName);
	
	String getProjectId(String projectName);
	
	void insert(String tableId, String key, String value) 
			throws JedisConnectionException, NullDbIdException;
	
	void update(String tableId, String key, String value) 
			throws JedisConnectionException, NullDbIdException;
	
	String getValueByKey(String tableId, String key) throws JedisConnectionException;
	
	boolean exists(String tableId, String key) throws JedisConnectionException;
	
	long insertInSet (String tableId, String key, String value) throws NullDbIdException;
	
	long removeFromSet(String tableId, String key, String value);
	
	long removeKey(String tableId, String key);
	
	Set<String> getAllKeysByPattern(String tableId, String pattern);
	
	Set<String> getSet(String tableId, String key);
	
	boolean existsInSet(String tableId, String key, String value);
	
	void close();
}
