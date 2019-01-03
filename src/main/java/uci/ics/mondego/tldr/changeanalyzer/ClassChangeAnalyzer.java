package uci.ics.mondego.tldr.changeanalyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.commons.lang3.StringUtils;

import uci.ics.mondego.tldr.extractor.MethodParser;
import uci.ics.mondego.tldr.tool.AccessCodes;
import uci.ics.mondego.tldr.tool.Databases;


public class ClassChangeAnalyzer extends ChangeAnalyzer{
	private List<String> changedAttributes;
	private Map<String, String> hashCodes; // stores all the hashcodes of all fields and methods
	private final ClassParser parser;
	private List<Method> allMethods;
	private List<Field> allFields;
	private List<Method> allChangedMethods;
	private List<Field> allChangedFields;
	
	public List<Method> getAllMethods(){
		return allMethods;
	}
	
	public List<Field> getAllFields(){
		return allFields;
	}
	
	public List<Method> getAllChangedMethods(){
		return allChangedMethods;
	}
	
	public List<Field> getAllChangedFields(){
		return allChangedFields;
	}
	
	public ClassChangeAnalyzer(String className) throws IOException{
		super(className);
		this.changedAttributes = new ArrayList<String>();
		this.hashCodes = new HashMap<String, String>();
		this.parser = new ClassParser(this.getEntityName());
		this.allChangedFields = new ArrayList<Field>();
		this.allChangedMethods = new ArrayList<Method>();
		this.allMethods = new ArrayList<Method>();
		this.allFields = new ArrayList<Field>();
		this.parse();
	}
	
	public String getChecksumByAttribute(String attr){
		return hashCodes.get(attr);
	}
	
	public List<String> getChangedAttributes(){
		return changedAttributes;
	}
	
	protected void parse() throws IOException{
		JavaClass parsedClass = parser.parse();
		
		Field [] allFields = parsedClass.getFields();
		for(Field f: allFields){
			this.allFields.add(f);
			
			String fieldFqn = parsedClass.getClassName()+"."+f.getName();
			
			String currentHashCode = f.toString().hashCode() +"";
			hashCodes.put(fieldFqn, currentHashCode);
			if(!rh.exists(Databases.TABLE_ID_ENTITY,fieldFqn)){
				logger.info(fieldFqn+" didn't exist in db...added");
				this.setChanged(true);
				changedAttributes.add(fieldFqn);
				this.allChangedFields.add(f);
				this.sync(Databases.TABLE_ID_ENTITY,fieldFqn, currentHashCode+"");
			}
			else{
				String prevHashCode = this.getValue(Databases.TABLE_ID_ENTITY, fieldFqn);
				currentHashCode = f.toString().hashCode() +"";
				if(!currentHashCode.equals(prevHashCode)){
					logger.info(fieldFqn+" changed");
					this.setChanged(true);
					changedAttributes.add(fieldFqn);
					this.allChangedFields.add(f);
					this.sync(Databases.TABLE_ID_ENTITY,fieldFqn, currentHashCode+"");
				}
			}
		}
		
		Method [] allMethods= parsedClass.getMethods();
		
		for(Method m: allMethods){
			this.allMethods.add(m);
			if(m.getModifiers() == AccessCodes.ABSTRACT || 
			m.getModifiers() == AccessCodes.FINAL ||
			m.getModifiers() == AccessCodes.INTERFACE || 
			m.getModifiers() == AccessCodes.NATIVE || 
			m.getModifiers() == AccessCodes.PRIVATE || 
			m.getModifiers() == AccessCodes.PROTECTED || 
			m.getModifiers() == AccessCodes.PUBLIC || 
			m.getModifiers() == AccessCodes.STATIC || 
			m.getModifiers() == AccessCodes.STRICT || 
			m.getModifiers() == AccessCodes.SYNCHRONIZED || 
			m.getModifiers() == AccessCodes.TRANSIENT || 
			m.getModifiers() == AccessCodes.VOLATILE){
				String code = m.getGenericSignature() +"\n"+ m.getModifiers()+"\n"+ m.getName()+ 
				"\n"+m.getSignature()+"\n"+ m.getCode();
							
				String lineInfo = code.substring(code.indexOf("Attribute(s)") == -1? 0 : code.indexOf("Attribute(s)"), 
						code.indexOf("LocalVariable(") == -1?
						code.length() : code.indexOf("LocalVariable(")) ;
				code = StringUtils.replace(code, lineInfo, ""); // changes in other function impacts line# of other functions...so Linecount info of the code must be removed
							
				code = code.substring(0, code.indexOf("StackMapTable") == -1? code.length() : code.indexOf("StackMapTable"));  // for some reason StackMapTable also change unwanted. WHY??
				
				code = code.substring(0, code.indexOf("StackMap") == -1? code.length() : code.indexOf("StackMap"));  // for some reason StackMapTable also change unwanted. WHY??
				
				String methodFqn = parsedClass.getClassName()+"."+m.getName();
	
				methodFqn += ("(");
				for(int i=0;i<m.getArgumentTypes().length;i++)
					methodFqn += ("$"+m.getArgumentTypes()[i]);
				methodFqn += (")");
					
				if(methodFqn.contains("com.mojang.brigadier.tree.CommandNode.equal"))
					System.out.println("HERE ********** "+ methodFqn);
				
				String currentHashCode = code.hashCode()+"";
				hashCodes.put(methodFqn, currentHashCode);
				if(!this.exists(Databases.TABLE_ID_ENTITY, methodFqn)){
					logger.info(methodFqn+" didn't exist in db...added");
					this.setChanged(true);
					changedAttributes.add(methodFqn);
					this.sync(Databases.TABLE_ID_ENTITY, methodFqn, currentHashCode+"");
					this.allChangedMethods.add(m);
					//if(m.getName().equals("getThis"))
				    //	System.out.println("FIRST TIME "+ methodFqn+"================\n"+code);
				}
				else{
					String prevHashCode = this.getValue(Databases.TABLE_ID_ENTITY, methodFqn);
					
					if(!currentHashCode.equals(prevHashCode)){
						logger.info(methodFqn+" changed "+"prev : "+prevHashCode+"  new: "+currentHashCode+" class name: "+this.getEntityName());
						//MethodParser mp = new MethodParser(m);
						this.setChanged(true);
						changedAttributes.add(methodFqn);
						this.sync(Databases.TABLE_ID_ENTITY, methodFqn, currentHashCode+"");
						this.allChangedMethods.add(m);
					    //if(m.getName().equals("getThis"))
					    //	System.out.println("DURING CHANGE"+ methodFqn+"================\n"+code);
					}
				}
			}
		}
	}
}
