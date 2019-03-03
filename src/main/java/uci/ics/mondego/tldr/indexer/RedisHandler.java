package uci.ics.mondego.tldr.indexer;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import uci.ics.mondego.tldr.tool.Databases;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import javax.annotation.*;



public class RedisHandler{

	private static RedisHandler instance = null; 
   
    private RedissonClient client;
    private static final JedisPoolConfig poolConfig = buildPoolConfig();
    private final static JedisPool jedisPool = new JedisPool(poolConfig, "localhost", 6379,10*1000);  
    private Jedis jedis;
     
    private final Logger logger = LogManager.getLogger(RedisHandler.class);

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
	
	public Set<String> getAllKeys(String pattern){
	    Set<String> ret = jedis.keys(pattern);
		return ret;
	}

	public static RedisHandler getInstane(String ... b) 
    { 
        if (instance == null) 
        {
        	if(b.length == 0){
        		instance = new RedisHandler();       		
        	}
        		
        	else if(b.length == 1)
        		instance = new RedisHandler(b[0]);
        } 
        return instance; 
    } 
	
	public void insert(String tableId, String key, String value) throws JedisConnectionException{		
		try{
		    jedis.set(tableId+key, value);
		}
		catch(JedisDataException e){
			System.out.println(tableId+"   "+key+"    "+value+"   ");
			System.out.println(e.getMessage());
		}
		catch(ClassCastException e){
			System.out.println(tableId+"   "+key+"    "+value+"   ");
			System.out.println(e.getMessage());
		}
	}
	
	public void update(String tableId, String key, String value) throws JedisConnectionException{
		this.insert(tableId, key, value);
	}
	
	public String getValueByKey(String tableId, String key) throws JedisConnectionException{ 
	    String ret = jedis.get(tableId+key);
	    return ret;
	}
	
	public boolean exists(String tableId, String key) throws JedisConnectionException{				
		boolean ret = false;
		try{
	    	 ret = jedis.exists(tableId+key);
		}
		catch(NullPointerException e){
			e.printStackTrace();
		}
		
	    return ret;
	}
	
	public long insertInSet(String tableId, String key, String value){
		String tableIdKey = tableId+key;
		long ret1 = jedis.sadd(tableIdKey, value);
		long ret2 = 1;		
		
		//INSERT IN FORWARD INDEX		
		String forwardTableId = null;
		if(tableId.equals(Databases.TABLE_ID_DEPENDENCY)){
			forwardTableId = Databases.TABLE_ID_FORWARD_INDEX_DEPENDENCY;
		}
		else if(tableId.equals(Databases.TABLE_ID_TEST_DEPENDENCY)){
			forwardTableId = Databases.TABLE_ID_FORWARD_INDEX_TEST_DEPENDENCY;
		}
		
		String tableIdValue = forwardTableId + value;
		ret2 = jedis.sadd(tableIdValue, key);
		return ret1 & ret2;
	}
	
	public long removeFromSet(String tableId, String key, String value){
		String tableIdKey = tableId + key;
		long ret = jedis.srem(tableIdKey, value);		
		return ret;
	}
	
	public long removeKey(String tableId, String key){
		String tableIdKey = tableId + key;
		long ret = jedis.del(tableIdKey);
		return ret;
	}
	
	public Set<String> getAllKeysByPattern(String tableId, String pattern){
		
		String key = tableId+pattern;
		Set<String> keys = jedis.keys(key);
		return keys;
	}
	
	
	public Set<String> getSet(String tableId, String key){
		Set<String> ret = null;
		try{
			ret = jedis.smembers(tableId+key);
			
		}
		catch(JedisDataException e){
			System.out.println(tableId+"   "+key);
			System.out.println(e.getMessage());
		}
		catch(ClassCastException e){
			System.out.println(tableId+"   "+key);
			System.out.println(e.getMessage());
		}
		return ret;
	}
	
	public Map<String, String> getTable(String tableId){
		Map<String, String> table = new HashMap<String, String>();
		Set<String> keys = jedis.keys(tableId+"*");
		for(String k: keys){
			String val = this.getValueByKey(null, k);
			table.put(k, val);
		}
		return table;
	}
	
	public boolean existsInSet(String tableId, String key, String value){
		boolean ret = jedis.sismember(tableId+key, value);
		return ret;
	}
	
	public static void destroyPool() {
		jedisPool.close();
	    jedisPool.destroy();
	}
	
	public void close(){
		if(jedis != null && jedis.isConnected())
	        jedis.close();
	}	
}
