package uci.ics.mondego.tldr.changeanalyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.commons.lang3.StringUtils;
import uci.ics.mondego.tldr.App;
import uci.ics.mondego.tldr.tool.AccessCodes;
import uci.ics.mondego.tldr.tool.Databases;
import uci.ics.mondego.tldr.tool.StringProcessor;


public class ClassChangeAnalyzer extends ChangeAnalyzer{
	private List<String> changedAttributes;
	private Map<String, String> hashCodes; // stores all the hashcodes of all fields and methods
	private HashMap<String, Method> extractedChangedMethods;
	private final ClassParser parser;
	private List<Method> allMethods;
	private List<Field> allFields;
	private List<Method> allChangedMethods;
	private List<Field> allChangedFields;
	private final JavaClass parsedClass;
	private List<String> allInterfaces;
	private String superClass;
	private Map<String, Integer> allPreviousEntities;
	
	public ClassChangeAnalyzer(String className) throws IOException{
		super(className);
		this.changedAttributes = new ArrayList<String>();
		this.hashCodes = new HashMap<String, String>();
		this.parser = new ClassParser(this.getEntityName());
		this.allChangedFields = new ArrayList<Field>();
		this.allChangedMethods = new ArrayList<Method>();
		this.allMethods = new ArrayList<Method>();
		this.allFields = new ArrayList<Field>();
		this.parsedClass = parser.parse();
		this.allInterfaces = new ArrayList<String>();
		this.extractedChangedMethods = new HashMap<String, Method>();
		this.superClass = "";
				
		Set<String> prevEnt = rh.getAllKeysByPattern
				(Databases.TABLE_ID_ENTITY, parsedClass.getClassName()+".*");  
		allPreviousEntities= new HashMap<String, Integer>();
		for(String e: prevEnt){
			allPreviousEntities.put(e, 0);
		}
		
		this.parse();
				
		this.syncClassHierarchy();
		
		this.deleteDepreciatedEntities();
		
		this.closeRedis();
	}
	
	private void syncClassHierarchy(){	
		this.parseInterface();
		this.parseSuperClass();
		
		Set<String> all_superclass_interface = this.rh.getSet(Databases.TABLE_ID_INTERFACE_SUPERCLASS, 
				parsedClass.getClassName());
				
		for(int i=0;i<allInterfaces.size();i++){
			if(!all_superclass_interface.contains(allInterfaces.get(i))){
				this.rh.insertInSet(Databases.TABLE_ID_INTERFACE_SUPERCLASS, parsedClass.getClassName(), 
						allInterfaces.get(i));
				this.rh.insertInSet(Databases.TABLE_ID_SUBCLASS, allInterfaces.get(i), parsedClass.getClassName());
			}				
		}
		
		if(!all_superclass_interface.contains(this.superClass) && this.superClass.length() > 0){
			this.rh.insertInSet(Databases.TABLE_ID_INTERFACE_SUPERCLASS, parsedClass.getClassName(), 
					this.superClass);
			this.rh.insertInSet(Databases.TABLE_ID_SUBCLASS, this.superClass, parsedClass.getClassName());
		}				
	}
	
	private void parseInterface(){
		try {
			String[] interfaces = this.parsedClass.getInterfaceNames();
			for(String cls: interfaces){
				allInterfaces.add(cls);
				logger.info("Interface of "+parsedClass.getClassName()+" is "+cls);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}
	}
	
	private void parseSuperClass(){
		try {
			this.superClass = !parsedClass.getSuperclassName().contains("java.") ? parsedClass.getSuperclassName(): "";			
			logger.info("Superclass of "+parsedClass.getClassName()+" : "+this.superClass);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}
	}
	
	protected void parse() throws IOException{
		
		Field [] allFields = parsedClass.getFields();
		for(Field f: allFields){
			this.allFields.add(f);
			
			String fieldFqn = parsedClass.getClassName()+"."+f.getName();
			if(allPreviousEntities.containsKey(fieldFqn)){
				allPreviousEntities.put(fieldFqn, allPreviousEntities.get(fieldFqn) + 1);
			}
			
			String currentHashCode = StringProcessor.CreateBLAKE(f.toString());
			hashCodes.put(fieldFqn, currentHashCode);
			
			if(!rh.exists(Databases.TABLE_ID_ENTITY,fieldFqn)){
				logger.info(fieldFqn+" didn't exist in db...added");
				this.setChanged(true);
				changedAttributes.add(fieldFqn);
				this.allChangedFields.add(f);
				this.sync(Databases.TABLE_ID_ENTITY,fieldFqn, currentHashCode+"");
				App.entityToTest.put(fieldFqn, true);
			}
			else{
				String prevHashCode = this.getValue(Databases.TABLE_ID_ENTITY, fieldFqn);
				currentHashCode = StringProcessor.CreateBLAKE(f.toString());
				if(!currentHashCode.equals(prevHashCode)){
					logger.info(fieldFqn+" changed");
					this.setChanged(true);
					changedAttributes.add(fieldFqn);
					this.allChangedFields.add(f);
					this.sync(Databases.TABLE_ID_ENTITY,fieldFqn, currentHashCode);
					App.entityToTest.put(fieldFqn, true);
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
				String code =  m.getModifiers()+"\n"+ m.getName()+ 
				"\n"+m.getSignature()+"\n"+ m.getCode();
							
				String lineInfo = code.substring(code.indexOf("Attribute(s)") == -1
						? 0 : code.indexOf("Attribute(s)"), 
						code.indexOf("LocalVariable(") == -1?
						code.length() : code.indexOf("LocalVariable(")) ;
				
				code = StringUtils.replace(code, lineInfo, ""); // changes in other function impacts line# of other functions...so Linecount info of the code must be removed
							
				code = code.substring(0, code.indexOf("StackMapTable") == -1? 
						code.length() : code.indexOf("StackMapTable"));  // for some reason StackMapTable also change unwanted. WHY??
				
				code = code.substring(0, code.indexOf("StackMap") == -1? 
						code.length() : code.indexOf("StackMap"));  // for some reason StackMapTable also change unwanted. WHY??
				
				String methodFqn = parsedClass.getClassName()+"."+m.getName();
	
				methodFqn += ("(");
				for(int i=0;i<m.getArgumentTypes().length;i++)
					methodFqn += ("$"+m.getArgumentTypes()[i]);
				methodFqn += (")");
				
				if(allPreviousEntities.containsKey(methodFqn)){
					allPreviousEntities.put(methodFqn, allPreviousEntities.get(methodFqn) + 1);
				}
				
				String currentHashCode = StringProcessor.CreateBLAKE(code);
				hashCodes.put(methodFqn, currentHashCode);
				if(!this.exists(Databases.TABLE_ID_ENTITY, methodFqn)){
								
					logger.info(methodFqn+" didn't exist in db...added");
					this.setChanged(true);
					changedAttributes.add(methodFqn);
					this.sync(Databases.TABLE_ID_ENTITY, methodFqn, currentHashCode);
					this.allChangedMethods.add(m);
					App.entityToTest.put(methodFqn, true);
					extractedChangedMethods.put(methodFqn, m);
				}
				else{
					String prevHashCode = this.getValue(Databases.TABLE_ID_ENTITY, methodFqn);
										
					if(!currentHashCode.equals(prevHashCode)){
						
						System.out.println(m.getCode().toString());
						
						logger.info(methodFqn+" changed "+"prev : "+prevHashCode+"  new: "+currentHashCode+" "
								+ "class name: "+this.getEntityName());
						this.setChanged(true);
						changedAttributes.add(methodFqn);
						this.sync(Databases.TABLE_ID_ENTITY, methodFqn, currentHashCode);
						this.allChangedMethods.add(m);
						extractedChangedMethods.put(methodFqn, m);
						App.entityToTest.put(methodFqn, true);
					}
				}
			}
		}	
	}
	
	
	private long deleteDepreciatedEntities(){
		long count = 0;
		for ( Map.Entry<String, Integer> entry : allPreviousEntities.entrySet()) {
		    Integer val = entry.getValue();
		    if(val == 0){
		    	count++;
		    	String key = entry.getKey();
		    	this.rh.removeKey(Databases.TABLE_ID_ENTITY, key);
		    	Set<String> allDependencies = this.rh.getSet
		    			(Databases.TABLE_ID_FORWARD_INDEX_DEPENDENCY, key);
		    	this.rh.removeKey(Databases.TABLE_ID_FORWARD_INDEX_DEPENDENCY, key);
		    	for(String dep: allDependencies){
		    		this.rh.removeFromSet(Databases.TABLE_ID_DEPENDENCY, dep, key);
		    	}
		    	logger.debug(key+ " is remomved from DB");
		    }  
		}
		return count;
	}
	
	
	private void printMethod(Method m){
		StringBuilder sb = new StringBuilder();		
	}
	
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
	
	public String getChecksumByAttribute(String attr){
		return hashCodes.get(attr);
	}
	
	public List<String> getChangedAttributes(){
		return changedAttributes;
	}
	
	public HashMap<String, Method> getextractedFunctions(){
		return extractedChangedMethods;
	}
}
