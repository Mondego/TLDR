package uci.ics.mondego.tldr.tool;

import redis.clients.jedis.Jedis;

import uci.ics.mondego.tldr.exception.*;
import uci.ics.mondego.tldr.model.Package;
import uci.ics.mondego.tldr.model.Selection;

import org.apache.commons.pool2.PoolUtils;


public class RedisHandler{

    private Jedis jedis; 
    private Package pk = null;
    private PoolUtils tou = null;

	public RedisHandler(){
		jedis = new Jedis("localhost");
		System.out.println("Server is running: "+jedis.ping()); 
	}
	
	public void insert(String fileName, String checkSum){
		Selection sel = new Selection();
		 jedis.set(fileName, checkSum); 
	     System.out.println("Stored string in redis:: "+ jedis.get(fileName));
	}
	
	public String getValue(String fileName){ 
	    return jedis.get(fileName);
	}
	
	public boolean exists(String fileName){
		return jedis.exists(fileName);
	}
	
	
	
	
}
