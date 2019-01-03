package uci.ics.mondego.tldr.tool;

public class StringProcessor {
	
	// convert 'type' of entity to bytecode format to proper format
	// example: "Ljava/util/List;"   --  "java/util/List"
	public static String typeProcessor(String type){
		
		// for one character string it is always primitive type in asm
		if(type.length() == 1)
			return "primitives";
		
	    return type.substring(1, type.length() - 1);
		
	}
	
	public static String pathToFqnConverter(String path){
		return path.replace('/', '.');
	}
	
	public static String[] signatureProcessor(String signature){
		if(signature != null){
			String [] word = signature.split(";|<|>|\\*");
			for(int i=0;i<word.length;i++){
				if(word[i].length() != 0){
					word[i] = StringProcessor.pathToFqnConverter(word[i]).substring(1);
				}		
			}
			
			return word;
		}
		else
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
	
	public static boolean isPrimitive(String type){
		if(type.length() == 0)
			return false;
		
		String allPrimitives = "BCDFIJSZV";
		
		if(allPrimitives.contains(type.charAt(0)+""))
				return true;
		return false;
	}
	
	
}
