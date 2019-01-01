package uci.ics.mondego.tldr.extractor;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;


public class MethodParser {
	
	Method method;
	List<String> dependency;
	
	public MethodParser(Method m){
		this.method = m;
		dependency = new ArrayList<String>();
		parse();
	}
	
	private void parse(){
//		LocalVariableTable localVariablePool = method.getLocalVariableTable();
//		LocalVariable[] allLocalVariales = localVariablePool.getLocalVariableTable();
//		System.out.println("all local variables");
//		for(LocalVariable var: allLocalVariales){
//			dependency.add(var.getSignature());
//		}
		
		String[] code = method.getCode().toString().split("\n");
		
		
		System.out.println("all dependency");
		
		for(String line: code){
			if(line.contains("invokevirtual") ||	
			   line.contains("invokeinterface") || 
			   line.contains("invokestatic") ||
			   line.contains("invokespecial") ||
			   line.contains("getfield") ||
			   line.contains("anewarray")  ||
			   line.contains("new")  ||
			   line.contains("invokedynamic")  ||
			   line.contains("getstatic")  ||
			   line.contains("putstatic")  ||
			   line.contains("putfield")  ||
			   line.contains("checkcast")){
				
				String[] parts = line.split("\\s+");
				if(!dependency.contains(parts[2])){
					dependency.add(parts[2]);
					System.out.println(parts[2]+"  "+parts[3]);
				}				
			}
		}
		
	}

}
