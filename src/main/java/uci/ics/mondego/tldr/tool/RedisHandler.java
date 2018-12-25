package uci.ics.mondego.tldr.tool;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import uci.ics.mondego.tldr.exception.*;
import uci.ics.mondego.tldr.model.Package;
import uci.ics.mondego.tldr.model.Selection;

import java.net.ConnectException;

import org.apache.commons.pool2.PoolUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;


public class RedisHandler{

    private Jedis jedis; 
    private Package pk = null;
    private PoolUtils tou = null;

	public RedisHandler(){
		try{
			RedissonClient client = Redisson.create();
			
			jedis = new Jedis("localhost");
			System.out.println("Server is running: "+jedis.ping()); 
		}
		catch(JedisConnectionException e){
			System.out.println("Connection Refused");
		}
	}
	
	private Config setConfig(String ip, int port){
		Config config = new Config();
		config.useSingleServer().setAddress(ip+":"+port);
		return config;
	}
	
	public void insert(String fileName, String checkSum) throws JedisConnectionException{
		
		jedis.set(fileName, checkSum); 
			//jedis.bgsave();
	}
	
	public void update(String fileName, String checkSum){
	
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
