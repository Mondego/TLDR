package uci.ics.mondego.tldr.exception;

@SuppressWarnings("serial")
public class UnknownDBIdException extends Exception{
	   String message;
	   
	   public UnknownDBIdException(String val) {
		   this.message = "Invalid Database ID : "+ val;
	   }
	   
	   public String toString(){ 
		   return ("MyException Occurred: "+message) ;
	   }
}


