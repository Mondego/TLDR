package uci.ics.mondego.tldr.extractor;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;

import uci.ics.mondego.tldr.tool.StringProcessor;


public class MethodParser {
	
	Method method;
	List<String> dependency;
	
	public MethodParser(Method m){
		this.method = m;
		dependency = new ArrayList<String>();
		parse();
	}
	
	private void parse(){
		
		String[] code = method.getCode().toString().split("\n");
		
		System.out.println("all dependency");
		
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
				    line.contains("new")  ||
				    line.contains("getstatic")  ||
					line.contains("putstatic")  ||
					line.contains("putfield")  ||
					line.contains("checkcast")){
				processed = parts[2];
			}
			
			if(!dependency.contains(processed)){
				dependency.add(processed);
			}
		}	
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
	
	
	public List<String> getDependency(){
		return dependency;
	}

}
