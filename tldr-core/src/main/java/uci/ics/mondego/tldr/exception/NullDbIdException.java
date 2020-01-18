package uci.ics.mondego.tldr.exception;

@SuppressWarnings("serial")
public class NullDbIdException extends Exception{
	   String message;
	   
	   public NullDbIdException(String val) {
		   this.message = "Null Database ID : "+ val;
	   }
	   
	   public String toString(){ 
		   return ("MyException Occurred: "+message) ;
	   }
}


