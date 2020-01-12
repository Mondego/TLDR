package uci.ics.mondego.tldr.indexer;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import uci.ics.mondego.tldr.exception.NullDbIdException;
import uci.ics.mondego.tldr.tool.Databases;
import uci.ics.mondego.tldr.tool.Constants;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import io.netty.util.internal.ConcurrentSet;

/**
 * This class is a wrapper class to manage access to Redis server. This class is Singleton
 * in nature. In order to use this class a Redis server must be opened beforehand. 
 * 
 * Table format : (Key, Value)
 * ===================================== 
 * <PROJECT_ID>-<TABLE_ID><KEY>, <VALUE>
 * @author demigorgan
 *
 */
public class RedisHandler{
    private final Logger logger = LogManager.getLogger(RedisHandler.class);
	private static RedisHandler instance = null; 
   
    private static final JedisPoolConfig poolConfig = buildPoolConfig();
    private final static JedisPool jedisPool = new JedisPool(poolConfig, "localhost", 6379,10*1000);  
    private Jedis jedis;
    private String project_id = null;
    
    private static JedisPoolConfig buildPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(2000);
        poolConfig.setMaxIdle(500);
        poolConfig.setMinIdle(100);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(120).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setNumTestsPerEvictionRun(100);
        poolConfig.setBlockWhenExhausted(true);
        //poolConfig.setMaxWaitMillis(Duration.ofSeconds(360).toMillis());
        return poolConfig;
    }
    
	public RedisHandler(){
		try{
			 jedis = jedisPool.getResource();
		}
		catch(JedisConnectionException e){
			logger.error("Connection Refused in localHost"+"\n");
		}
	}

	public RedisHandler(String addr){
		try{
			 jedis = jedisPool.getResource();
		}
		catch(JedisConnectionException e){
			logger.error("Connection Refused in "+addr+"\n");
		}
	}
	
	/**
	 * Get all keys with a Regex
	 * @param pattern
	 * @return
	 */
	public Set<String> getAllKeys(String regex){
	    Set<String> ret = jedis.keys(regex);
		return ret;
	}
		
	/**
	 * Inserts a project name in the database.
	 */
	public void insertProject(String projectName) {
		try {
			int projectId = 0;
			// If the project already exits then does not
			// insert again.
			if (projectExists(projectName)) {
				return;
			}
			// Update the last used ID number so that the next ID
			// can be used for next project.
			String id =  getValueByKey(
					/* tableId= */ Constants.EMPTY, 
					/* key= */ "LAST-PROJECT-ID");
			
			if (id !=  null) {
				projectId = Integer.parseInt(id); 
				projectId++;
				insert("project", "LAST-PROJECT-ID", projectId+"");
				project_id = projectId + Constants.EMPTY;
			}
			insert(Databases.TABLE_ID_PROJECT, projectName, projectId+"");
		} catch (JedisConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullDbIdException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns true if the project exits in the database.
	 */
	public boolean projectExists(String projectName) {
		return jedis.exists(Databases.TABLE_ID_PROJECT + projectName);
	}
	
	/**
	 * Returns the id of a project if exists, else returns a null.
	 */
	public String getProjectId(String projectName) {
		String id = jedis.get(Databases.TABLE_ID_PROJECT + projectName);
		if (id == null) {
			return null;
		}
		return id;
	}
	
	/** 
	 * Returns all methods and fields of a project. 
	 * */
	public Set<String> getAllMethodsAndFields(String projectId){
		StringBuilder sb = new StringBuilder();
		sb.append(projectId);
		sb.append(Constants.HYPHEN);
		sb.append(Databases.TABLE_ID_ENTITY);
		String pattern = sb.toString();
		Set<String> allEntities = new HashSet<String>();
	    for(String key: getAllKeys(pattern)) {
	    	// Remove projectId and tableId from the keys 
	    	int index = key.lastIndexOf('-'); 
	    	if (index <= (key.length() - 2)) {
	    		allEntities.add(key.substring(index + 2));
	    	}
	    }
	    return allEntities;
	}

	public static RedisHandler getInstane(String ... b) { 
        if (instance == null) {
        	if(b.length == 0) {
        		instance = new RedisHandler();       		
        	} else if(b.length == 1) {
        		instance = new RedisHandler(b[0]);
        	}
        } 
        return instance; 
    } 
	
	/**
	 * Inserts a key-value pair in a table fixed by {@link tableId}. 
	 */
	public void insert(String tableId, String key, String value) 
			throws JedisConnectionException, 
				   NullDbIdException {		
		String projectId = System.getProperty(Constants.PROJECT_ID);
		
		String lookupKey = null;
		
		// For inserting last used ID number, no tableId or projectId should be appended.
		if (tableId.equals("project")) {
			lookupKey = key;
		} 
		// For inserting project name we don't need to append project id.
		else if (tableId.equals(Databases.TABLE_ID_PROJECT)) {
			lookupKey = tableId + key;
		} 
		else {
			lookupKey = projectId + Constants.HYPHEN + tableId + key;
		}
		
		try {
			if (lookupKey.startsWith("null") 
				|| tableId == null 
				|| (!tableId.equals(Databases.TABLE_ID_PROJECT) && projectId == null)) {
				System.out.println("hereh ************" + lookupKey + "  "
						+ tableId+ "   " + projectId);
				throw new NullDbIdException(key+" "+value);
			}		    
			jedis.set(lookupKey, value);
		} catch(JedisDataException e) {
			e.printStackTrace();
		}
		catch(ClassCastException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Updates a key-value pair.
	 */
	public void update(String tableId, String key, String value) 
			throws JedisConnectionException, 
			       NullDbIdException {
		this.insert(tableId, key, value);
	}
	
	public String getValueByKey(String tableId, String key) throws JedisConnectionException { 
		String projectId = System.getProperty(Constants.PROJECT_ID);
		String lookupKey = projectId + Constants.HYPHEN + tableId + key;
		String ret = jedis.get(lookupKey);
	    return ret;
	}
	
	public boolean exists(String tableId, String key) 
			throws JedisConnectionException {				
		boolean ret = false;
		try{
			String projectId = System.getProperty(Constants.PROJECT_ID);
			String lookupKey = projectId + Constants.HYPHEN + tableId + key;
			
			ret = jedis.exists(lookupKey);
		} catch(NullPointerException e) {
			e.printStackTrace();
		}
	    return ret;
	}
	
	public long insertInSet (
			String tableId, String key, String value) throws NullDbIdException{
		String projectId = System.getProperty(Constants.PROJECT_ID);
		String lookupKey = projectId + Constants.HYPHEN + tableId + key;
		
		if ( lookupKey.startsWith("null") 
				|| tableId == null 
				|| key == null 
				|| value == null
				|| projectId == null) {
			throw new NullDbIdException(key+" "+value);
		}			
		
		long ret1 = jedis.sadd(lookupKey, value);
		long ret2 = 1;		
		
		//INSERT IN FORWARD INDEX		
		String forwardTableId = null;
		
		if(tableId.equals(Databases.TABLE_ID_DEPENDENCY)){
			forwardTableId = Databases.TABLE_ID_FORWARD_INDEX_DEPENDENCY;
		} else if (tableId.equals(Databases.TABLE_ID_TEST_DEPENDENCY)) {
			forwardTableId = Databases.TABLE_ID_FORWARD_INDEX_TEST_DEPENDENCY;
		}
				
		String lookupKeyForwardTable = projectId + Constants.HYPHEN + forwardTableId + value;
		ret2 = jedis.sadd(lookupKeyForwardTable, key);
		return ret1 & ret2;
	}
	
	public long removeFromSet(String tableId, String key, String value) {
		String projectId = System.getProperty(Constants.PROJECT_ID);
		String lookupKey = projectId + Constants.HYPHEN + tableId + key;
		long ret = jedis.srem(lookupKey, value);		
		return ret;
	}
	
	public long removeKey(String tableId, String key) {
		String projectId = System.getProperty(Constants.PROJECT_ID);
		String lookupKey = projectId + Constants.HYPHEN + tableId + key;
		long ret = jedis.del(lookupKey);
		return ret;
	}
	
	public Set<String> getAllKeysByPattern(String tableId, String pattern) {
		String projectId = System.getProperty(Constants.PROJECT_ID);
		String key = projectId + Constants.HYPHEN + tableId + pattern;
		Set<String> keys = new HashSet<String>();
		
		for (String string: jedis.keys(key)) {
			// Remove projectId and tableId from the keys 
			// Key format is - (project_id+"-"+one_digit_table_id+key)
	    	int index = string.indexOf(Constants.HYPHEN); 
	    	if (index < (string.length() - 2)) {
	    		String extractedKey = string.substring(index + 2);
	    		if (extractedKey == null || extractedKey.length() == 0) {
	    			continue;
	    		}
	    		keys.add(extractedKey);
	    	}
		}
		
		return keys;
	}
	
	public Set<String> getSet(String tableId, String key) {
		String projectId = System.getProperty(Constants.PROJECT_ID);
		Set<String> ret = new HashSet<String>();
		try{
			String lookupKey = projectId + Constants.HYPHEN + tableId + key;
			ret = jedis.smembers(lookupKey);
		} catch(JedisDataException e) {
			e.printStackTrace();
		} catch(ClassCastException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public boolean existsInSet(String tableId, String key, String value) {
		String projectId = System.getProperty(Constants.PROJECT_ID);
		String lookupKey = projectId + Constants.HYPHEN + tableId + key; 
		
		boolean ret = jedis.sismember(lookupKey, value);
		return ret;
	}
	
	public static void destroyPool() {
		jedisPool.close();
	    jedisPool.destroy();
	}
	
	public void close() {
		if(jedis != null && jedis.isConnected())
	        jedis.close();
	}	
}
