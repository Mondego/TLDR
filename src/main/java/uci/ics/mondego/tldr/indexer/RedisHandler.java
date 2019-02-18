package uci.ics.mondego.tldr.indexer;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

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
    static final JedisPoolConfig poolConfig = buildPoolConfig();
    private static final JedisPool jedisPool = new JedisPool(poolConfig, "localhost", 6379,10*1000);  
    private static final JedisPool jedisPool2 = new JedisPool(poolConfig, "localhost", 6379,10*1000);  

    private final Logger logger = LogManager.getLogger(RedisHandler.class);

    private static JedisPoolConfig buildPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(1000);
        poolConfig.setMaxIdle(200);
        poolConfig.setMinIdle(100);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(1000).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(600).toMillis());
        poolConfig.setNumTestsPerEvictionRun(100);
        poolConfig.setBlockWhenExhausted(true);
        //poolConfig.setMaxWaitMillis(Duration.ofSeconds(360).toMillis());
        return poolConfig;
    }
    
	private RedisHandler(){
	
	}

	@Nullable
	private RedisHandler(String addr){
		try{
			//System.out.println("Server is running: "+jedis.ping()); 
		}
		catch(JedisConnectionException e){
			logger.error("Connection Refused in "+addr+"\n");
		}
	}
	
	public Jedis getDB() {
		Jedis jedis = jedisPool.getResource();
	    return jedis;
	}
	
	public Set<String> getAllKeys(String pattern){
		Jedis jedis = this.getDB();
	    Set<String> ret = jedis.keys(pattern);
	    if(jedis.isConnected())jedis.close();			
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
	
	
	// config object can be created from json file too
	private Config setConfig(String ip, int port){
		Config config = new Config();
		config.useSingleServer().setAddress(ip+":"+port);
		return config;
	}
	
	//get all keys
	private Iterable<String> getKeys(){
		RKeys keys = client.getKeys();
		return keys.getKeys();
	}
	
	//get all keys by pattern
	private Iterable<String> getKeys(String pattern){
		RKeys keys = client.getKeys();
		return keys.getKeysByPattern(pattern);
	}
	
	public void insert(String tableId, String key, String value) throws JedisConnectionException{		
		try{
			Jedis jedis = this.getDB();
		    jedis.set(tableId+key, value);
		    if(jedis != null)
		    	jedis.close();		    
		    if(jedis.isConnected())
	           jedis.disconnect();
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
		Jedis jedis = this.getDB();
	    String ret = jedis.get(tableId+key);
	    if(jedis != null)
	    	jedis.close();
	    if(jedis.isConnected())
	           jedis.disconnect();
	    return ret;
	}
	
	public boolean exists(String tableId, String key) throws JedisConnectionException{		
		Jedis jedis = this.getDB();
	    boolean ret = jedis.exists(tableId+key);
	    if(jedis != null)
	    	jedis.close();
	    if(jedis.isConnected())
	           jedis.disconnect();
	    return ret;
	}
	
	public void insertInSet(String tableId, String key, String value){
		Jedis jedis = this.getDB();
		String k = tableId+key;
		jedis.sadd(k, value);
		if(jedis != null)
			jedis.close();
		if(jedis.isConnected())
	           jedis.disconnect();
	}
	
	public Set<String> getAllKeys(String tableId, String pattern){
		Jedis jedis = this.getDB();
		String key = tableId+pattern;
		Set<String> keys = jedis.keys(key);
		if(jedis != null)
			jedis.close();
		if(jedis.isConnected())
	           jedis.disconnect();
		return keys;
	}
	
	
	public Set<String> getSet(String tableId, String key){
		Set<String> ret = null;
		try{
			Jedis jedis = this.getDB();
			ret = jedis.smembers(tableId+key);
			if(jedis != null)
				jedis.close();
			if(jedis.isConnected())
		        jedis.disconnect();
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
		Jedis jedis = this.getDB();
		Map<String, String> table = new HashMap<String, String>();
		Set<String> keys = jedis.keys(tableId+"*");
		for(String k: keys){
			String val = this.getValueByKey(null, k);
			table.put(k, val);
		}
		if(jedis != null)
			jedis.close();
		if(jedis.isConnected())
	           jedis.disconnect();
		return table;
	}
	
	public boolean existsInSet(String tableId, String key, String value){
		Jedis jedis = this.getDB();
		boolean ret = jedis.sismember(tableId+key, value);
		if(jedis != null)
			jedis.close();	
		if(jedis.isConnected())
	           jedis.disconnect();
		return ret;
	}
	
	public void destroyPool() {
		this.jedisPool.close();
	    this.jedisPool.destroy();
	}
	
//	public void close(){
//		if(jedis != null && jedis.isConnected())
//	        jedis.close();
//	}
	
}
