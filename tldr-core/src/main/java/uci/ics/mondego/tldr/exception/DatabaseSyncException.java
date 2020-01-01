package uci.ics.mondego.tldr.exception;

public class DatabaseSyncException extends Exception{

	   String message;
	   public DatabaseSyncException(String val) {
		   this.message = "Problem in syncing data in the Redis Server : "+ val;
	   }
	   
	   public String toString(){ 
		   return ("EmptyByteCodeException Occurred: "+message) ;
	   }
}
