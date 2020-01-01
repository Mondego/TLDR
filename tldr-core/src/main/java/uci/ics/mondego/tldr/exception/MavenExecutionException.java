package uci.ics.mondego.tldr.exception;

public class MavenExecutionException extends Exception{

	 String message;
	   
	   public MavenExecutionException(String val) {
		   this.message = "No ByteCode for Method : "+ val;
	   }
	   
	   public String toString(){ 
		   return ("EmptyByteCodeException Occurred: "+message) ;
	   }
}
