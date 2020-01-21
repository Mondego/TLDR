package uci.ics.mondego.tldr.extractor;

import java.util.HashSet;
import java.util.Set;
import org.apache.bcel.classfile.Method;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import uci.ics.mondego.tldr.exception.EmptyByteCodeException;
import uci.ics.mondego.tldr.tool.Constants;
import uci.ics.mondego.tldr.tool.StringProcessor;

/**
 * This class parses a particular method bytecode line by line and 
 * extract all the field and method dependency. Particularly, this 
 * class extracts the following bytecode instructions -- 
 * 
 * "invokevirtual", "invokeinterface", "invokestatic", "invokespecial", 
 * "invokefinal", "invokedynamic", "getfield", "getstatic", "putstatic", 
 * "putfield". 
 * 
 * @author demigorgan
 */
public class MethodParser {

	private final Method method;
	private final Set<String> allInternalDependency;
	private final Set<String> allExternalDependency;
	private final Set<String> allStaticDependency;
	private final Set<String> allFinalDependency;
	private final Set<String> allVirtualDependency;
	private final Set<String> allInterfaceDependency;
	private final Set<String> allStaticFieldUpdated;
	private final Set<String> allOwnFieldUpdated;
	private final Set<String> allSpecialDependency;
	private final Set<String> allFieldDependency;
	
	public static final Logger logger = LogManager.getLogger(MethodParser.class);
	
	@SuppressWarnings("")
	public MethodParser(Method m) {
		this.method = m;
		this.allInternalDependency = new HashSet<String>();
		this.allExternalDependency = new HashSet<String>();
		this.allStaticDependency = new HashSet<String>();
		this.allFinalDependency = new HashSet<String>();
		this.allVirtualDependency = new HashSet<String>();
		this.allInterfaceDependency = new HashSet<String>();
		this.allSpecialDependency = new HashSet<String>();
		this.allStaticFieldUpdated = new HashSet<String>();
		this.allOwnFieldUpdated = new HashSet<String>();
		this.allFieldDependency = new HashSet<String>();
		
		try {
			parse();
		} 
		catch (EmptyByteCodeException e) {
			logger.error(method.getName() + " is Abstract/Interface/Annotation... Skipping parsing");
		}
	}
	
	private void parse() throws EmptyByteCodeException {
		
		if (method.getCode() == null 
			|| method.getCode().toString() == null 
			|| method.getCode().toString().length() == 0 
			|| method.isAbstract() 
		    || method.isInterface() 
		    || method.isAnnotation()){
			throw new EmptyByteCodeException(method.getName());
		}
		
		String[] code = method.getCode().toString().split(Constants.NEW_LINE);
		
		for(String line: code) {
			//method call
			String processed = null;
			String[] parts = line.split("\\s+");
			
			/**** line format it 
			 * <LINE NUMBER>: <SPACE> <COMMAND> <SPACE> <FIELD/METHOD NAME> <SPACE> <PARAMETERS> <SPACE> <OTHER>
			 */
			if (line.contains("invokevirtual") ||	
			   line.contains("invokeinterface") || 
			   line.contains("invokestatic") ||
			   line.contains("invokespecial") ||
			   line.contains("invokefinal")  ||
			   line.contains("invokedynamic")) {
				
				// sometime we see anomaly in bytecode -- '<LINE NUMBER>:' and <COMMAND> are together
				if (parts[0].indexOf(Constants.COLON) < (parts[0].length() - 1)) {
					processed = parts[1] + parseMethodParameters(parts[2]);
				} else {
					// in regular case
					processed = parts[2]+parseMethodParameters(parts[3]);	
				}				
			}
			
			// field
			else if (line.contains("getfield") ||
				    line.contains("getstatic") || 
				    line.contains("putstatic") || 
				    line.contains("putfield")) {
				
				if(parts[0].indexOf(Constants.COLON) < (parts[0].length() - 1)) {
					processed = parts[1];
				} else {
					processed = parts[2];
				}
			}
			
			///// CHECK CAREFULLY
			else if(line.contains("checkcast")) {
				// because a checkcast instruction looks like --- 51:   checkcast		<com.mojang.brigadier.tree.CommandNode> (64)
				if (parts[0].indexOf(Constants.COLON) < (parts[0].length() - 1)) {
					processed = parts[1].substring(1, parts[1].length() - 1);
				} else {
					processed = parts[2].substring(1, parts[2].length() - 1);
				}
				processed = processed+".<init>(*)";
			}
			
			if(processed != null && line.contains("invokestatic")){
				allStaticDependency.add(processed);
			} 
			
			else if(processed != null && line.contains("invokefinal")){
				allFinalDependency.add(processed);
			} 
			
			else if(processed != null && line.contains("invokevirtual")){
				allVirtualDependency.add(processed);
			} 
			
			else if(processed != null && line.contains("invokeinterface")){
				allInterfaceDependency.add(processed);
			} 
			
			else if(processed != null && line.contains("invokespecial")){	
				allSpecialDependency.add(processed); // Add <init>
				
				// For each <init> we assume that there is an implicit dependency on 
				// static initializer i.e. <clinit>. 
				String staticInitializer = processStaticInitializer(processed);
				if (staticInitializer != null) {
					allSpecialDependency.add(staticInitializer); // Add <clinit>
				}				 
			} 
			
			else if(processed != null && (line.contains("getfield")  || line.contains("getstatic"))) {
				allFieldDependency.add(processed);
			} 
			
			else if(processed != null && line.contains("putfield")){
				allOwnFieldUpdated.add(processed);
			} 
			
			else if(processed != null && line.contains("putstatic")){
				allStaticFieldUpdated.add(processed);
			}
		}
	}
	
	private String parseMethodParameters(String signature){		
		try{
			signature = signature.substring(signature.indexOf("(") + 1, signature.indexOf(")"));
			
			if (signature.length() == 0) {
				return "()";
			}

			StringBuilder sb = new StringBuilder();
			sb.append("(");
			
			String [] params =  signature.split(Constants.SEMI_COLON);
			
			String array_append = Constants.EMPTY;
			for (int i = 0; i < params.length; i++){
				
				for (int j = 0;j < params[i].length(); j++){
					if (StringProcessor.isPrimitive(params[i].charAt(j))) {
						sb.append( 
								"$" + StringProcessor.convertBaseType(params[i].charAt(j)) + array_append);
						array_append = Constants.EMPTY;
					}
					
					else if(params[i].charAt(j) == 'L'){
						sb.append(
								"$" + StringProcessor.pathToFqnConverter(params[i].substring(j+1)) + array_append);
						array_append = Constants.EMPTY;
						break;
					}
					else if(params[i].charAt(j) == '['){
						array_append += "[]";
						continue;
					}
				}	
			}
			
			sb.append(")");			
			return sb.toString();
		}
		catch(StringIndexOutOfBoundsException e){
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String processStaticInitializer(String fqn) {
		// This method basically replaces <init> by <clinit>
		if (!fqn.contains("<init>")) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(fqn.substring(0, fqn.indexOf('<')));
		sb.append("<clinit>()");
		return sb.toString();
	}

	
	public Set<String> getAllInternalDependencies(){
		return allInternalDependency;
	}
	
	public Set<String> getAllExternalDependencies(){
		return allExternalDependency;
	}
	
	public Set<String> getAllInternalDependency() {
		return allInternalDependency;
	}

	public Set<String> getAllExternalDependency() {
		return allExternalDependency;
	}

	public Set<String> getAllStaticDependency() {
		return allStaticDependency;
	}

	public Set<String> getAllFinalDependency() {
		return allFinalDependency;
	}
	
	public Set<String> getAllSpecialDependency() {
		return allSpecialDependency;
	}

	public Set<String> getAllVirtualDependency() {
		return allVirtualDependency;
	}
	
	public Set<String> getAllInterfaceDependency() {
		return allInterfaceDependency;
	}
	
	public Set<String> getAllStaticFieldUpdated() {
		return allStaticFieldUpdated;
	}

	public Set<String> getAllOwnFieldUpdated() {
		return allOwnFieldUpdated;
	}
	
	public Set<String> getAllFieldDependency() {
		return allFieldDependency;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append("\nALL VIRTUAL DEPENDENCY :  \n");
		for(String str: allVirtualDependency){
			sb.append(str+" , ");
		}
		
		sb.append("\nALL INTERFACE DEPENDENCY :  \n");
		for(String str: allInterfaceDependency){
			sb.append(str+" , ");
		}
		
		sb.append("\nALL FINAL DEPENDENCY :  \n");
		for(String str: allFinalDependency){
			sb.append(str+" , ");
		}
		
		sb.append("\nALL STATIC DEPENDENCY :  \n");
		for(String str: allStaticDependency){
			sb.append(str+" , ");
		}
		
		sb.append("\nALL SPECIAL DEPENDENCY :  \n");
		for(String str: allSpecialDependency){
			sb.append(str+" , ");
		}
		
		sb.append("\nALL FIELD DEPENDENCY :  \n");
		for(String str: allFieldDependency){
			sb.append(str+" , ");
		}
		
		return sb.toString();
	}
}
