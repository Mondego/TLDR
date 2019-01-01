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
import uci.ics.mondego.tldr.tool.Databases;


public class ClassChangeAnalyzer extends ChangeAnalyzer{
	private List<String> changedAttributes;
	private Map<String, String> hashCodes; // stores all the hashcodes of all fields and methods
	private final ClassParser parser;
	
	public ClassChangeAnalyzer(String className) throws IOException{
		super(className);
		this.changedAttributes = new ArrayList<String>();
		this.hashCodes = new HashMap<String, String>();
		this.parser = new ClassParser(this.getEntityName());
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
			String fieldFqn = parsedClass.getPackageName()+"."+f.getName();
			String currentHashCode = f.toString().hashCode() +"";
			hashCodes.put(fieldFqn, currentHashCode);
			if(!rh.exists(Databases.TABLE_ID_ENTITY,fieldFqn)){
				logger.info(fieldFqn+" didn't exist in db...added");
				this.setChanged(true);
				changedAttributes.add(fieldFqn);
				this.sync(Databases.TABLE_ID_ENTITY,fieldFqn, currentHashCode+"");
			}
			else{
				String prevHashCode = this.getValue(Databases.TABLE_ID_ENTITY, fieldFqn);
				currentHashCode = f.toString().hashCode() +"";
				if(!currentHashCode.equals(prevHashCode)){
					logger.info(fieldFqn+" changed");
					this.setChanged(true);
					changedAttributes.add(fieldFqn);
					this.sync(Databases.TABLE_ID_ENTITY,fieldFqn, currentHashCode+"");
				}
			}
		}
		
		Method [] allMethods= parsedClass.getMethods();
		
		for(Method m: allMethods){
			
			String code = m.getGenericSignature() +"\n"+ m.getAccessFlags()+"\n"+ m.getName()+ 
			"\n"+m.getSignature()+"\n"+ m.getCode();
			
			//System.out.println("Method "+m.getName()+"=========\n"+m.getCode());
			
			String lineInfo = code.substring(code.indexOf("Attribute(s)") == -1? 0 : code.indexOf("Attribute(s)"), 
					code.indexOf("LocalVariable(") == -1?
					code.length() : code.indexOf("LocalVariable(")) ;
			code = StringUtils.replace(code, lineInfo, ""); // changes in other function impacts line# of other functions...so Linecount info of the code must be removed
						
			code = code.substring(0, code.indexOf("StackMapTable") == -1? code.length() : code.indexOf("StackMapTable"));  // for some reason StackMapTable also change unwanted. WHY??
			
			code = code.substring(0, code.indexOf("StackMap") == -1? code.length() : code.indexOf("StackMap"));  // for some reason StackMapTable also change unwanted. WHY??
			
			String methodFqn = parsedClass.getPackageName()+"."+m.getName();
			
			methodFqn += ("(");
			for(int i=0;i<m.getArgumentTypes().length;i++)
				methodFqn += ("$"+m.getArgumentTypes()[i]);
			methodFqn += (")");
			
			String currentHashCode = code.hashCode()+"";
			hashCodes.put(methodFqn, currentHashCode);
			if(!this.exists(Databases.TABLE_ID_ENTITY, methodFqn)){
				logger.info(methodFqn+" didn't exist in db...added");
				this.setChanged(true);
				changedAttributes.add(methodFqn);
				this.sync(Databases.TABLE_ID_ENTITY, methodFqn, currentHashCode+"");
			}
			else{
				String prevHashCode = this.getValue(Databases.TABLE_ID_ENTITY, methodFqn);
				
				if(!currentHashCode.equals(prevHashCode)){
					logger.info(methodFqn+" changed");
					MethodParser mp = new MethodParser(m);
					this.setChanged(true);
					changedAttributes.add(methodFqn);
					this.sync(Databases.TABLE_ID_ENTITY, methodFqn, currentHashCode+"");	
				}
			}
		}
	}
}
