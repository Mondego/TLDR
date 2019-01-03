package uci.ics.mondego.tldr.indexer;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisConnectionException;
import java.util.Set;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RedisHandler{

	private static RedisHandler instance = null; 
    private static Jedis jedis; 
    private RedissonClient client;
    private final Logger logger = LogManager.getLogger(RedisHandler.class);

	private RedisHandler(){
		try{
			jedis = new Jedis("localhost");
			logger.info("Server running : "+jedis.ping());
		}
		catch(JedisConnectionException e){
			logger.error("Connection Refused in LocalHost\n");
		}
	}
	
	private RedisHandler(String addr){
		try{
			//this.client = Redisson.create();
			jedis = new Jedis(addr);
			System.out.println("Server is running: "+jedis.ping()); 
		}
		catch(JedisConnectionException e){
			logger.error("Connection Refused in "+addr+"\n");
		}
	}
	
	public Set<String> getAllKeys(String pattern){
		return jedis.keys(pattern);
	}

	public static RedisHandler getInstane(String ... b) 
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
		
		Transaction t = jedis.multi();
		t.hset(tableId,key, value);
		t.exec();
		//jedis.set(fileName, checkSum); 
	}
	
	public void update(String tableId, String key, String value) throws JedisConnectionException{
		//String prev = jedis.hget(tableId, key);
		Transaction t = jedis.multi();
		t.hset(tableId, key, value);
		t.exec();
		//System.out.println(key+ " changed to : "+jedis.hget(tableId, key)+" from : "+prev);
		//jedis.set(fileName, checkSum);
	}
	
	public String getValueByKey(String tableId, String key) throws JedisConnectionException{ 
		return jedis.hget(tableId, key);
	}
	
	public boolean exists(String tableId, String key) throws JedisConnectionException{
		return jedis.hexists(tableId, key);
	}
	
	public void insertInSet(String tableId, String key, String value){
		String k = tableId+key;
		jedis.sadd(k, value);
	}
	
	public Set<String> getSet(String tableId, String key){
		return jedis.smembers(tableId+key);
	}
	
	public boolean existsInSet(String tableId, String key, String value){
		return jedis.sismember(tableId+key, value);
	}
		
	public void close(){
		if(jedis != null && jedis.isConnected())
	        jedis.close();
	}
	
}
