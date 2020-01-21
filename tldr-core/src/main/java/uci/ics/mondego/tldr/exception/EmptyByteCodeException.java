package uci.ics.mondego.tldr.exception;

@SuppressWarnings("serial")
public class EmptyByteCodeException extends Exception{
	   String message;
	   
	   public EmptyByteCodeException(String val) {
		   this.message = "No ByteCode for Method : "+ val;
	   }
	   
	   public String toString(){ 
		   return ("EmptyByteCodeException Occurred: "+message) ;
	   }
}
