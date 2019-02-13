package uci.ics.mondego.tldr.indexer;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RedisHandler{

	private static RedisHandler instance = null; 
    private Jedis jedis; 
    private RedissonClient client;
    final JedisPoolConfig poolConfig = buildPoolConfig();
    JedisPool jedisPool = new JedisPool(poolConfig, "localhost");
    
    private final Logger logger = LogManager.getLogger(RedisHandler.class);

    private JedisPoolConfig buildPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(120).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }
    
	private RedisHandler(){
	
	}
	
	private RedisHandler(String addr){
		try{
			jedis = new Jedis(addr);
			System.out.println("Server is running: "+jedis.ping()); 
		}
		catch(JedisConnectionException e){
			logger.error("Connection Refused in "+addr+"\n");
		}
	}
	
	public Jedis getDB() {
		jedis = jedisPool.getResource();
	    return jedis;
	}
	
	public Set<String> getAllKeys(String pattern){
		Jedis jedis = this.getDB();
	    Set<String> ret = jedis.keys(pattern);
	    jedis.close();			
		return ret;
	}

	synchronized public static RedisHandler getInstane(String ... b) 
    { 
        if (instance == null) 
        {
        	if(b.length == 0)
        		instance = new RedisHandler(); 
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
		Jedis jedis = this.getDB();
	    jedis.set(tableId+key, value);
	    jedis.close();		
	}
	
	public void update(String tableId, String key, String value) throws JedisConnectionException{
		this.insert(tableId, key, value);
	}
	
	public String getValueByKey(String tableId, String key) throws JedisConnectionException{ 
		Jedis jedis = this.getDB();
	    String ret = jedis.get(tableId+key);
	    jedis.close();
	    return ret;
	}
	
	public boolean exists(String tableId, String key) throws JedisConnectionException{		
		Jedis jedis = this.getDB();
	    boolean ret = jedis.exists(tableId+key);
	    jedis.close();
	    return ret;
	}
	
	public void insertInSet(String tableId, String key, String value){
		Jedis jedis = this.getDB();
		String k = tableId+key;
		jedis.sadd(k, value);
	    jedis.close();
	}
	
	public Set<String> getAllKeys(String tableId, String pattern){
		Jedis jedis = this.getDB();
		String key = tableId+pattern;
		Set<String> keys = jedis.keys(key);
		jedis.close();
		return keys;
	}
	
	
	public Set<String> getSet(String tableId, String key){
		Jedis jedis = this.getDB();
		Set<String> ret = jedis.smembers(tableId+key);
		jedis.close();
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
		jedis.close();
		return table;
	}
	
	public boolean existsInSet(String tableId, String key, String value){
		Jedis jedis = this.getDB();
		boolean ret = jedis.sismember(tableId+key, value);
		jedis.close();		
		return ret;
	}
	
	public void destroyPool() {
	    this.jedisPool.destroy();
	}
	
	public void close(){
		if(jedis != null && jedis.isConnected())
	        jedis.close();
	}
	
}
