package uci.ics.mondego.tldr.exception;

@SuppressWarnings("serial")
public class TLDRMojoExecutionException extends Exception {
	
	 String message;
	   
	   public TLDRMojoExecutionException(String val) {
		   this.message = "Mojo exception occued : "+ val;
	   }
	   
	   public String toString(){ 
		   return ("TLDRMojoExecutionException Occurred: "+message) ;
	   }

}
