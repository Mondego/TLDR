package uci.ics.mondego.tldr.extractor;

import java.util.ArrayList;
import java.util.List;
import org.apache.bcel.classfile.Method;

import uci.ics.mondego.tldr.tool.StringProcessor;


public class MethodParser {
	
	Method method;
	List<String> allInternalDependency;
	List<String> allExternalDependency;
	
	public MethodParser(Method m){
		this.method = m;
		allInternalDependency = new ArrayList<String>();
		allExternalDependency = new ArrayList<String>();
		parse();
	}
	
	private void parse(){
		
		String[] code = method.getCode().toString().split("\n");
		
		for(String line: code){
			//method call
			String processed = null;
			String[] parts = line.split("\\s+");
			if(line.contains("invokevirtual") ||	
			   line.contains("invokeinterface") || 
			   line.contains("invokestatic") ||
			   line.contains("invokespecial") ||
			   line.contains("anewarray")  ||
			   line.contains("invokedynamic")){
				//System.out.println(line);
				processed = parts[2]+parseMethodParameters(parts[3]);					
			}
			
			// field
			else if(line.contains("getfield") ||
				    line.contains("getstatic")  ||
					line.contains("putstatic")  ||
					line.contains("putfield") ){
				processed = parts[2];
			}
			
			if(processed != null && (processed.contains("java.") && !allExternalDependency.contains(processed))){
				allExternalDependency.add(processed);
			}
			
			else if(processed != null && !allInternalDependency.contains(processed)){
				allInternalDependency.add(processed);
			}
		}
		
		//for(int j=0;j<allInternalDependency.size();j++)
		//	System.out.println(allInternalDependency.get(j));
	}
	
	private String parseMethodParameters(String signature){
		signature = signature.substring(signature.indexOf("(")+1, signature.indexOf(")"));
		
		if(signature.length() == 0)
			return "()";
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		
		String [] params =  signature.split(";");
		for(int i=0;i<params.length;i++){
			if(!StringProcessor.isPrimitive(params[i]))
				params[i] = ("$"+params[i].substring(1));
			else{
				StringBuilder sb1 = new StringBuilder();
				for(int j=0;j<params[i].length();j++){
					sb1.append("$"+StringProcessor.convertBaseType(params[i].charAt(j)));
				}
				params[i] = sb1.toString();
			}
			params[i] = StringProcessor.pathToFqnConverter(params[i]);
			sb.append(params[i]);
		}
		
		sb.append(")");
		
		return sb.toString();
	}
	
	public List<String> getAllInternalDependencies(){
		return allInternalDependency;
	}
	
	public List<String> getAllExternalDependencies(){
		return allExternalDependency;
	}

}
