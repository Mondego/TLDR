package uci.ics.mondego.tldr.changeanalyzer;

import java.io.EOFException;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.commons.lang3.StringUtils;
import uci.ics.mondego.tldr.App;
import uci.ics.mondego.tldr.exception.DatabaseSyncException;
import uci.ics.mondego.tldr.tool.AccessCodes;
import uci.ics.mondego.tldr.tool.Databases;
import uci.ics.mondego.tldr.tool.StringProcessor;

public class TestChangeAnalyzer extends ChangeAnalyzer{

	private final ClassParser parser;
	private final JavaClass parsedClass;
	private Map<String, Integer> allPreviousTestCases;
	private List<String> allInterfaces;
	private String superClass;
	
	public TestChangeAnalyzer(String className) throws IOException, DatabaseSyncException, EOFException{
		super(className);
		this.parser = new ClassParser(this.getEntityName());
		this.parsedClass = parser.parse();
		this.allPreviousTestCases = new HashMap<String, Integer>();
		this.allInterfaces = new ArrayList<String>();
		this.superClass = "";
		Set<String> prevTst = database.getAllKeysByPattern
				(Databases.TABLE_ID_ENTITY, parsedClass.getClassName()+".*");  
		
		for(String e: prevTst){
			allPreviousTestCases.put(e.substring(1), 0); // substring(1) case we have to remove table id
		}
		
		this.parse();
		this.syncClassHierarchy();
		this.deleteDepreciatedTestCases();
		this.closeRedis();
	}
	
	private void syncClassHierarchy(){	
		this.parseInterface();
		this.parseSuperClass();
		
		Set<String> all_superclass_interface = this.database.getSet(Databases.TABLE_ID_INTERFACE_SUPERCLASS, 
				parsedClass.getClassName());
				
		for(int i=0;i<allInterfaces.size();i++){
			if(!all_superclass_interface.contains(allInterfaces.get(i))
				&& !(allInterfaces.get(i).startsWith("java.") 
				|| allInterfaces.get(i).startsWith("junit."))){
				
				this.database.insertInSet(Databases.TABLE_ID_INTERFACE_SUPERCLASS, parsedClass.getClassName(), 
						allInterfaces.get(i));
				this.database.insertInSet(Databases.TABLE_ID_SUBCLASS, allInterfaces.get(i), parsedClass.getClassName());
			}				
		}
		
		if(!all_superclass_interface.contains(this.superClass) 
				&& this.superClass != null 
				&& this.superClass.length() > 0 
				&& !this.superClass.startsWith("java") 
				&& !this.superClass.startsWith("junit")){
			
			this.database.insertInSet(Databases.TABLE_ID_INTERFACE_SUPERCLASS, parsedClass.getClassName(), 
					this.superClass);
			this.database.insertInSet(Databases.TABLE_ID_SUBCLASS, this.superClass, parsedClass.getClassName());
		}				
	}
	
	private void parseInterface(){
		try {
			String[] interfaces = this.parsedClass.getInterfaceNames();
			for(String cls: interfaces){
				this.allInterfaces.add(cls);
			}
		} 
		catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	private void parseSuperClass(){
		try {
			this.superClass = !parsedClass.getSuperclassName().startsWith("java.") 
					? parsedClass.getSuperclassName(): "";			
		} 
		catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	
	protected void parse() throws IOException, DatabaseSyncException{
		
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
				m.getModifiers() == AccessCodes.STATIC_INIT ||
				m.getModifiers() == AccessCodes.STRICT || 
				m.getModifiers() == AccessCodes.SYNCHRONIZED || 
				m.getModifiers() == AccessCodes.TRANSIENT || 
				m.getModifiers() == AccessCodes.VOLATILE ||
				m.getModifiers() == AccessCodes.DEFAULT_INIT ||
				m.getModifiers() == AccessCodes.PUBLIC2 ||
				m.getModifiers() == AccessCodes.INHERIT ||		
				m.getModifiers() == AccessCodes.PUBLIC3 ||			
				m.getModifiers() == AccessCodes.PUBLIC4 ||			
				m.getModifiers() == AccessCodes.PUBLIC5 ||				
				m.getModifiers() == AccessCodes.ABSTRACT2 ||				
				m.getModifiers() == AccessCodes.STATIC2 ||	
				m.getModifiers() == AccessCodes.STATIC3 ||	
				m.getModifiers() == AccessCodes.INNER ||	
				m.getModifiers() == AccessCodes.DEFAULT_INIT2 ||
				m.getModifiers() == AccessCodes.FINAL2 ||
				m.getModifiers() == AccessCodes.STATIC4 ||
				m.getModifiers() == AccessCodes.ABSTRACT3||
				m.getModifiers() == AccessCodes.FINAL3 ||
				m.getModifiers() == AccessCodes.STATIC5 ||
				m.getModifiers() == AccessCodes.STATIC6 ||
				m.getModifiers() == AccessCodes.STATIC7 ||
				m.getModifiers() == AccessCodes.STATIC8 ||
				m.getModifiers() == AccessCodes.FINAL4 ||
				m.getModifiers() == AccessCodes.PUBLIC6 ||
				m.getModifiers() == AccessCodes.PUBLIC7 ||
				m.getModifiers() == AccessCodes.PUBLIC8 ||
				m.getModifiers() == AccessCodes.INNER2 ||	
				m.getModifiers() == AccessCodes.FINAL5 ||
				m.getModifiers() == AccessCodes.PUBLIC9 ||
				m.getModifiers() == AccessCodes.INNER3 ||	
				m.getModifiers() == AccessCodes.INNER4 ||					
				m.getModifiers() == AccessCodes.ABSTRACT4 ||
				m.getModifiers() == AccessCodes.PUBLIC10 ||					
				m.getModifiers() == AccessCodes.PUBLIC11 ||
				m.getModifiers() == AccessCodes.PUBLIC12){
				
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
				
				if(!this.allPreviousTestCases.containsKey(methodFqn)){
					//logger.debug(methodFqn+" is new test, added to testToRun");
					App.testToRun.put(methodFqn, true);
					App.allNewAndChangedTests.put(methodFqn, true);
					
					boolean ret = this.sync(Databases.TABLE_ID_ENTITY, methodFqn, currentHashCode);
					if(!ret){
						throw new DatabaseSyncException(methodFqn);
					}
					//logger.info(methodFqn+" didn't exist in db...added");					
					Map.Entry<String, Method> map = new  AbstractMap.SimpleEntry<String, Method>(methodFqn, m);					
					DependencyExtractor2 dep = new DependencyExtractor2(map, true);
				}
				
				else{
					allPreviousTestCases.put(methodFqn, allPreviousTestCases.get(methodFqn) + 1);
					String prevHashCode = this.getValue(Databases.TABLE_ID_ENTITY, methodFqn);	
					
					if(!currentHashCode.equals(prevHashCode)){
						//logger.debug(methodFqn+" is changed test, added to testToRun");
						App.testToRun.put(methodFqn, true);						
						App.allNewAndChangedTests.put(methodFqn, true);
						boolean ret = this.sync(Databases.TABLE_ID_ENTITY, methodFqn, currentHashCode);
						
						if(!ret){
							throw new DatabaseSyncException(methodFqn);
						}
						
						Map.Entry<String, Method> map = new  AbstractMap.SimpleEntry<String, Method>(methodFqn, m);	
						DependencyExtractor2 dep = new DependencyExtractor2(map, true);
					}
				}
			}
		}		
	}
	
	private long deleteDepreciatedTestCases(){
		long count = 0;
		for ( Map.Entry<String, Integer> entry : allPreviousTestCases.entrySet()) {
		    Integer val = entry.getValue();
		    if(val == 0){
		    	count++;
		    	String key = entry.getKey();
		    	this.database.removeKey(Databases.TABLE_ID_ENTITY, key);
		    	Set<String> allDependencies = this.database.getSet
		    			(Databases.TABLE_ID_FORWARD_INDEX_TEST_DEPENDENCY, key);
		    	this.database.removeKey(Databases.TABLE_ID_FORWARD_INDEX_TEST_DEPENDENCY, key);
		    	for(String dep: allDependencies){
		    		this.database.removeFromSet(Databases.TABLE_ID_TEST_DEPENDENCY, dep, key);
		    	}
		    	//logger.info(key+ " test case is remomved from DB");
		    }  
		}
		return count;
	}
}
