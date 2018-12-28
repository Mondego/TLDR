package uci.ics.mondego.tldr.indexer;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import uci.ics.mondego.tldr.exception.*;
import uci.ics.mondego.tldr.model.Package;
import uci.ics.mondego.tldr.model.Selection;

import java.net.ConnectException;

import org.apache.commons.pool2.PoolUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.redisson.Redisson;
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
			this.client = Redisson.create();
			
			jedis = new Jedis("localhost");
			System.out.println("Server is running: "+jedis.ping()); 
		}
		catch(JedisConnectionException e){
			logger.error("Connection Refused in LocalHost\n");
		}
	}
	
	private RedisHandler(String addr){
		try{
			this.client = Redisson.create();
			
			jedis = new Jedis(addr);
			System.out.println("Server is running: "+jedis.ping()); 
		}
		catch(JedisConnectionException e){
			logger.error("Connection Refused in "+addr+"\n");
		}
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
	
	public void insert(String fileName, String checkSum) throws JedisConnectionException{
		
		jedis.set(fileName, checkSum); 
			//jedis.bgsave();
	}
	
	public void update(String fileName, String checkSum){
		jedis.set(fileName, checkSum);
	}
	
	
	public String getValue(String fileName) throws JedisConnectionException{ 
	    return jedis.get(fileName);
	}
	
	public boolean exists(String fileName) throws JedisConnectionException{
		return jedis.exists(fileName);
	}
	
	public void close(){
		jedis.close();
	}
	
}
