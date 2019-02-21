package uci.ics.mondego.tldr.changeanalyzer;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.commons.lang3.StringUtils;
import uci.ics.mondego.tldr.App;
import uci.ics.mondego.tldr.tool.AccessCodes;
import uci.ics.mondego.tldr.tool.Databases;
import uci.ics.mondego.tldr.tool.StringProcessor;

public class TestChangeAnalyzer extends ChangeAnalyzer{

	private List<String> changedAttributes;
	private final ClassParser parser;
	private List<Method> allMethods;
	private List<Field> allFields;
	private List<Method> allChangedMethods;
	private List<Field> allChangedFields;
	private final JavaClass parsedClass;
	
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
	
	public TestChangeAnalyzer(String className) throws IOException{
		super(className);
		this.changedAttributes = new ArrayList<String>();
		this.parser = new ClassParser(this.getEntityName());
		this.allChangedFields = new ArrayList<Field>();
		this.allChangedMethods = new ArrayList<Method>();
		this.allMethods = new ArrayList<Method>();
		this.allFields = new ArrayList<Field>();
		this.parsedClass = parser.parse();
		this.parse();
		
		this.closeRedis();
	}
	
	
	public List<String> getChangedAttributes(){
		return changedAttributes;
	}
	
	protected void parse() throws IOException{
		
		/*Field [] allFields = parsedClass.getFields();
		
		for(Field f: allFields){
			this.allFields.add(f);
			
			String fieldFqn = parsedClass.getClassName()+"."+f.getName();
			
			String currentHashCode =  StringProcessor.CreateBLAKE(f.toString());
			
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
				currentHashCode = StringProcessor.CreateBLAKE(f.toString());
				if(!currentHashCode.equals(prevHashCode)){
					logger.info(fieldFqn+" changed");
					this.setChanged(true);
					changedAttributes.add(fieldFqn);
					this.allChangedFields.add(f);
					this.sync(Databases.TABLE_ID_ENTITY,fieldFqn, currentHashCode+"");
				}
			}
		}*/
		
		Method [] allMethods= parsedClass.getMethods();
		
		for(Method m: allMethods){
			
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
					
				
				String currentHashCode = StringProcessor.CreateBLAKE(code);
				
				if(!this.exists(Databases.TABLE_ID_ENTITY, methodFqn)){
					logger.info(methodFqn+" didn't exist in db...added");
					App.testToRun.put(methodFqn, true);
					this.sync(Databases.TABLE_ID_ENTITY, methodFqn, currentHashCode+"");
					Map.Entry<String, Method> map = new  AbstractMap.SimpleEntry<String, Method>(methodFqn, m);					
					DependencyExtractor2 dep = new DependencyExtractor2(map, true);
				}
				
				else{
					String prevHashCode = this.getValue(Databases.TABLE_ID_ENTITY, methodFqn);					
					if(!currentHashCode.equals(prevHashCode)){
						App.testToRun.put(methodFqn, true);						
						this.sync(Databases.TABLE_ID_ENTITY, methodFqn, currentHashCode+"");
						Map.Entry<String, Method> map = new  AbstractMap.SimpleEntry<String, Method>(methodFqn, m);	
						DependencyExtractor2 dep = new DependencyExtractor2(map, true);
					}
				}
			}
		}		
	}
	
}
