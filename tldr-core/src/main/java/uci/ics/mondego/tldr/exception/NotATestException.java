package uci.ics.mondego.tldr.exception;

@SuppressWarnings("serial")
public class NotATestException extends Exception{

	  String message;
	   
	   public NotATestException(String val) {
		   this.message = "Given method is not a test method : "+ val;
	   }
	   
	   public String toString(){ 
		   return ("NotATestException Occurred: "+message) ;
	   }
}
