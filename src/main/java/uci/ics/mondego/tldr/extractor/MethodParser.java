package uci.ics.mondego.tldr.extractor;

import java.util.ArrayList;
import java.util.List;
import org.apache.bcel.classfile.Method;

import uci.ics.mondego.tldr.tool.StringProcessor;


public class MethodParser {

	private final Method method;
	private List<String> allInternalDependency;
	private List<String> allExternalDependency;
	
	private List<String> allStaticDependency;
	private List<String> allFinalDependency;
	private List<String> allVirtualDependency;
	private List<String> allInterfaceDependency;
	
	public MethodParser(Method m){
		this.method = m;
		this.allInternalDependency = new ArrayList<String>();
		this.allExternalDependency = new ArrayList<String>();
		
		this.allStaticDependency = new ArrayList<String>();
		this.allFinalDependency = new ArrayList<String>();
		this.allVirtualDependency = new ArrayList<String>();
		this.allInterfaceDependency = new ArrayList<String>();
		
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
			
			else if(line.contains("checkcast")){
				// because a checkcast instruction looks like --- 51:   checkcast		<com.mojang.brigadier.tree.CommandNode> (64)
				processed = parts[2].substring(1, parts[2].length() - 1);
				processed = processed+".<init>(*)";
			}
			
			
			if(processed != null && line.contains("invokestatic") && !allStaticDependency.contains(processed)){
				allStaticDependency.add(processed);
			}
			
			else if(processed != null && line.contains("invokespecial") && !allFinalDependency.contains(processed)){
				allFinalDependency.add(processed);
			}
			
			else if(processed != null && line.contains("invokevirtual") && !allVirtualDependency.contains(processed)){
				allVirtualDependency.add(processed);
			}
			
			else if(processed != null && line.contains("invokeinterface") && !allInterfaceDependency.contains(processed)){
				allInterfaceDependency.add(processed);
			}	
				
			if(processed != null && (processed.contains("java.") && !allExternalDependency.contains(processed))){
				allExternalDependency.add(processed);
			}
			
			else if(processed != null && !allInternalDependency.contains(processed)){
				allInternalDependency.add(processed);
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
	
	public List<String> getAllInternalDependencies(){
		return allInternalDependency;
	}
	
	public List<String> getAllExternalDependencies(){
		return allExternalDependency;
	}
	
	public List<String> getAllInternalDependency() {
		return allInternalDependency;
	}

	public List<String> getAllExternalDependency() {
		return allExternalDependency;
	}

	public List<String> getAllStaticDependency() {
		return allStaticDependency;
	}

	public List<String> getAllFinalDependency() {
		return allFinalDependency;
	}

	public List<String> getAllVirtualDependency() {
		return allVirtualDependency;
	}
	
	public List<String> getAllInterfaceDependency() {
		return allInterfaceDependency;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("\nALL VIRTUAL DEPENDENCY :  \n");
		for(int i=0;i<allVirtualDependency.size();i++){
			sb.append(allVirtualDependency.get(i)+" , ");
		}
		sb.append("\nALL Interface DEPENDENCY :  \n");
		for(int i=0;i<allInterfaceDependency.size();i++){
			sb.append(allInterfaceDependency.get(i)+" , ");
		}
		
		sb.append("\nALL Final DEPENDENCY :  \n");
		for(int i=0;i<allFinalDependency.size();i++){
			sb.append(allFinalDependency.get(i)+" , ");
		}
		
		sb.append("\nALL Static DEPENDENCY :  \n");
		for(int i=0;i<allStaticDependency.size();i++){
			sb.append(allStaticDependency.get(i)+" , ");
		}
		return sb.toString();
	}

}
