package uci.ics.mondego.tldr.tool;

import redis.clients.jedis.Jedis;

public class RedisHandler implements Database{

    private Jedis jedis; 

	public RedisHandler(){
		jedis = new Jedis("localhost");
		System.out.println("Server is running: "+jedis.ping()); 
	}
	
	public void insert(String fileName, String checkSum){
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
