package uci.ics.mondego.tldr.tool;

import java.security.Security;

import com.rfksystems.blake2b.Blake2b;
import com.rfksystems.blake2b.security.Blake2bProvider;

public class StringProcessor {
	
	// convert 'type' of entity to bytecode format to proper format
	// example: "Ljava/util/List;"   --  "java/util/List"
	public static String typeProcessor(String type){
		
		// for one character string it is always primitive type in asm
		if(type.length() == 1) {
			return "primitives";
		}
		
	    return type.substring(1, type.length() - 1);
	}
	
	public static String pathToFqnConverter(String path){
		return path.replace('/', '.');
	}
	
	public static String[] signatureProcessor(String signature){
		if (signature != null) {
			String [] word = signature.split(";|<|>|\\*");
			for(int i=0;i<word.length;i++){
				if(word[i].length() != 0) {
					word[i] = StringProcessor.pathToFqnConverter(word[i]).substring(1);
				}		
			}
			
			return word;
		} 
		return null;
	}
	
	public static String convertBaseType(char type) {
	    switch (type) {
	      case 'B':
	        return "byte";
	      case 'C':
	        return "char";
	      case 'D':
	        return "double";
	      case 'F':
	        return "float";
	      case 'I':
	        return "int";
	      case 'J':
	        return "long";
	      case 'S':
	        return "short";
	      case 'Z':
	        return "boolean";
	      case 'V':
	        return "void";
	      default:
	        return "" + type;
	    }
	 }
	
	public static boolean isPrimitive(char type) {
		String allPrimitives = "BCDFIJSZV";
		if(allPrimitives.contains(type+"")) {
			return true;
		}
		return false;
	}
	
	public static String CreateBLAKE(String input) {
		try {
			Security.addProvider(new Blake2bProvider());
	        java.security.MessageDigest md = java.security.MessageDigest.getInstance(Blake2b.BLAKE2_B_160);
	        byte[] array = md.digest(input.getBytes());
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < array.length; ++i) {
	          sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
	        }
	        return sb.toString();
	    } catch (java.security.NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	    return null;
    }	
}
