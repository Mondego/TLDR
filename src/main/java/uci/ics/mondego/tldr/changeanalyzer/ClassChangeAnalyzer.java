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

import uci.ics.mondego.tldr.extractor.MethodParser;
import uci.ics.mondego.tldr.tool.AccessCodes;
import uci.ics.mondego.tldr.tool.Databases;


public class ClassChangeAnalyzer extends ChangeAnalyzer{
	private List<String> changedAttributes;
	private Map<String, String> hashCodes; // stores all the hashcodes of all fields and methods
	private Map<String, Method> extractedChangedMethods;
	private final ClassParser parser;
	private List<Method> allMethods;
	private List<Field> allFields;
	private List<Method> allChangedMethods;
	private List<Field> allChangedFields;
	private final JavaClass parsedClass;
	private List<String> allInterfaces;
	private String superClass;
	
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
		this.parsedClass = parser.parse();
		this.allInterfaces = new ArrayList<String>();
		this.extractedChangedMethods = new HashMap<String, Method>();
		this.superClass = "";
		
		this.parse();
		
		//this.parseInterface();
		//this.parseSuperClass();
	}
	
	public Map<String, Method> getextractedFunctions(){
		return extractedChangedMethods;
	}
	
	private void parseInterface(){
		
		try {
			JavaClass[] interfaces = this.parsedClass.getAllInterfaces();
			
			String str="";
			for(JavaClass cls: interfaces){
				if(!cls.getClassName().contains("java."))str+=(cls.getClassName()+" , ");
				allInterfaces.add(cls.getClassName());
				logger.info("Interface of "+parsedClass.getClassName()+" is "+cls.getClassName());
			}
			
			if(str.length() > 0)
				System.out.println("Int of "+parsedClass.getClassName()+" : "+str);

			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}
	}
	
	private void parseSuperClass(){
		try {
			this.superClass = parsedClass.getSuperClass().getClassName();
			
			if(!this.superClass.contains("java."))
				System.out.println("Superclass of "+parsedClass.getClassName()+" : "+this.superClass);

			logger.info("Superclass of "+parsedClass.getClassName()+" : "+this.superClass);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}
	}
	
	public String getChecksumByAttribute(String attr){
		return hashCodes.get(attr);
	}
	
	public List<String> getChangedAttributes(){
		return changedAttributes;
	}
	
	protected void parse() throws IOException{
		
		
		Field [] allFields = parsedClass.getFields();
		for(Field f: allFields){
			this.allFields.add(f);
			
			String fieldFqn = parsedClass.getClassName()+"."+f.getName();
			
			//String currentHashCode = f.toString().hashCode() +"";
			
			String currentHashCode = CreateMD5(f.toString());
			
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
				//currentHashCode = f.toString().hashCode() +"";
				currentHashCode = CreateMD5(f.toString());
				if(!currentHashCode.equals(prevHashCode)){
					logger.info(fieldFqn+" changed");
					this.setChanged(true);
					changedAttributes.add(fieldFqn);
					this.allChangedFields.add(f);
					this.sync(Databases.TABLE_ID_ENTITY,fieldFqn, currentHashCode);
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
			
				String currentHashCode = CreateMD5(code);
				
				hashCodes.put(methodFqn, currentHashCode);
				
				if(!this.exists(Databases.TABLE_ID_ENTITY, methodFqn)){
					logger.info(methodFqn+" didn't exist in db...added");
					this.setChanged(true);
					changedAttributes.add(methodFqn);
					this.sync(Databases.TABLE_ID_ENTITY, methodFqn, currentHashCode);
					this.allChangedMethods.add(m);
					extractedChangedMethods.put(methodFqn, m);
				}
				else{
					String prevHashCode = this.getValue(Databases.TABLE_ID_ENTITY, methodFqn);
					
					if(!currentHashCode.equals(prevHashCode)){
						
						//System.out.println(m.getCode().toString(true));
						
						logger.info(methodFqn+" changed "+"prev : "+prevHashCode+"  new: "+currentHashCode+" "
								+ "class name: "+this.getEntityName());
						this.setChanged(true);
						changedAttributes.add(methodFqn);
						this.sync(Databases.TABLE_ID_ENTITY, methodFqn, currentHashCode);
						this.allChangedMethods.add(m);
						extractedChangedMethods.put(methodFqn, m);
					}
				}
			}
		}
		
		//this.syncDependency();
	}
	
	private void addDependentsInDb(String entity, String dependents){
		
		Set<String> prevDependents = this.rh.getSet(Databases.TABLE_ID_DEPENDENCY, entity);
		if(prevDependents.size() == 0 || prevDependents == null || 
				!prevDependents.contains(dependents)){
			this.rh.insertInSet(Databases.TABLE_ID_DEPENDENCY, entity, dependents);
			logger.info(dependents+ " has been updated as "+entity+" 's dependent");
		}
	}

    // Use input string to calculate MD5 hash
	private String CreateMD5(String input)
    {
		try {
	        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
	        byte[] array = md.digest(input.getBytes());
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < array.length; ++i) {
	          sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
	       }
	        return sb.toString();
	    } catch (java.security.NoSuchAlgorithmException e) {
	    }
	    return null;
    }
	
	private void syncDependency(){
		try{
			for(int i=0;i<allChangedMethods.size();i++){
				MethodParser mp = new MethodParser(allChangedMethods.get(i));
				List<String> dependencies = mp.getAllInternalDependencies();
				for(int j=0;j<dependencies.size();j++){
					String methodFqn =parsedClass.getClassName()+"."+allChangedMethods.get(i).getName();
					methodFqn += ("(");
					for(int k=0;k<allChangedMethods.get(i).getArgumentTypes().length;k++)
						methodFqn += ("$"+allChangedMethods.get(i).getArgumentTypes()[k]);
					methodFqn += (")");
		
					addDependentsInDb(dependencies.get(j), methodFqn);
				}
			}
		}
		catch(NullPointerException e){
			logger.error("Problem is syncing dependencies of changed entities"+e.getMessage());
		}
	}
}
