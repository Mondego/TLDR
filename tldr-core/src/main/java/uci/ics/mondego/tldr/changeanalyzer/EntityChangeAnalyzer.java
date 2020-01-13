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
import uci.ics.mondego.tldr.TLDR;
import uci.ics.mondego.tldr.exception.DatabaseSyncException;
import uci.ics.mondego.tldr.exception.NullDbIdException;
import uci.ics.mondego.tldr.tool.AccessCodes;
import uci.ics.mondego.tldr.tool.DatabaseIDs;
import uci.ics.mondego.tldr.tool.Constants;
import uci.ics.mondego.tldr.tool.StringProcessor;

/**
 * Change analyzer of the members of a class. 
 * @author demigorgan
 *
 */
public class EntityChangeAnalyzer extends ChangeAnalyzer{
	private HashMap<String, Method> extractedChangedMethods;
	private final ClassParser parser;
	private final JavaClass parsedClass;
	private List<String> allInterfaces;
	private String superClass;
	
	// Needed to remove entities from the database that are deleted in the current version. 
	private Map<String, Integer> allPreviousEntities;
	
	public EntityChangeAnalyzer (String className) throws IOException, DatabaseSyncException {
		super(className);
		this.parser = new ClassParser(this.getEntityName());
		this.parsedClass = parser.parse();
		this.allInterfaces = new ArrayList<String>();
		this.extractedChangedMethods = new HashMap<String, Method>();
		this.superClass = Constants.EMPTY;
				
		Set<String> prevEnt = redisHandler.getAllKeysByPattern(
				DatabaseIDs.TABLE_ID_ENTITY, parsedClass.getClassName()+".*");  
		
		this.allPreviousEntities= new HashMap<String, Integer>();
		for(String entity: prevEnt){
			allPreviousEntities.put(entity, 0);
		}
		
		try {
			parse();
			syncClassHierarchy();
			deleteDepreciatedEntities();
			closeRedis();
		} catch (NullDbIdException nullDbIdException) {
			nullDbIdException.printStackTrace();
		}
	}
	
	/**
	 * Update class hierarchy information in the redis server.
	 * @throws NullDbIdException
	 */
	private void syncClassHierarchy() throws NullDbIdException {	
		parseInterface();
		parseSuperClass();
		
		Set<String> all_superclass_interface = 
				this.redisHandler.getSet(DatabaseIDs.TABLE_ID_INTERFACE_SUPERCLASS, parsedClass.getClassName());
				
		for(int i = 0;i < allInterfaces.size(); i++){
			String interface_ = allInterfaces.get(i);
			if( !all_superclass_interface.contains(interface_) 
					&& !(interface_.startsWith("java.") 
					|| interface_.startsWith("junit."))) {
				
				this.redisHandler.insertInSet(DatabaseIDs.TABLE_ID_INTERFACE_SUPERCLASS, parsedClass.getClassName(), 
						interface_);
				
				this.redisHandler.insertInSet(DatabaseIDs.TABLE_ID_SUBCLASS, interface_, parsedClass.getClassName());
			}	
			
		}
		
		if (!all_superclass_interface.contains(superClass) 
			&& superClass != null 
			&& superClass.length() > 0 
			&& !superClass.startsWith("java") 
			&& !superClass.startsWith("junit")) {
			
			this.redisHandler.insertInSet(DatabaseIDs.TABLE_ID_INTERFACE_SUPERCLASS, parsedClass.getClassName(), 
					this.superClass);
			this.redisHandler.insertInSet(DatabaseIDs.TABLE_ID_SUBCLASS, superClass, parsedClass.getClassName());
		}				
	}
	
	private void parseInterface() {
		try {
			String[] interfaces = parsedClass.getInterfaceNames();
			for (String interface_: interfaces) {
				if (!interface_.startsWith("java")) {
					allInterfaces.add(interface_);
				}					
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}
	
	private void parseSuperClass() {
		try {
			superClass = 
					!parsedClass.getSuperclassName().startsWith("java.") 
					? parsedClass.getSuperclassName(): null;			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}
	
	private void parseFields() throws IOException, DatabaseSyncException {
		Field [] allFields = parsedClass.getFields();
		
		for (Field f: allFields) {
			String fieldFqn = parsedClass.getClassName()+"."+f.getName();
			String currentHashCode = StringProcessor.CreateBLAKE(f.toString());
			
			// Insert this field into DB if it is new in this revision.
			if (!allPreviousEntities.containsKey(fieldFqn)) {
				setChanged(true);
				boolean ret = this.sync(DatabaseIDs.TABLE_ID_ENTITY,fieldFqn, currentHashCode);
				if(!ret){
					throw new DatabaseSyncException(fieldFqn);
				}
				TLDR.allNewAndChangedentities.put(fieldFqn, true);
				TLDR.entityToTest.put(fieldFqn, true);
			}
			// Else insert into DB only if the field signature has been changed. 
			else{
				// allPreviousEntities value are updated. This is needed to delete 
				// deprecated entities (for which the value will be 0).
				allPreviousEntities.put(fieldFqn, allPreviousEntities.get(fieldFqn) + 1);
				
				String prevHashCode = this.getValue(DatabaseIDs.TABLE_ID_ENTITY, fieldFqn);
				if (!currentHashCode.equals(prevHashCode)) {
					setChanged(true);
					boolean ret = this.sync(DatabaseIDs.TABLE_ID_ENTITY,fieldFqn, currentHashCode);
					if(!ret){
						throw new DatabaseSyncException(fieldFqn);
					}
					TLDR.allNewAndChangedentities.put(fieldFqn, true);
					TLDR.entityToTest.put(fieldFqn, true);
				}
			}
		}
	}
	
	private void parseMethods() throws IOException, DatabaseSyncException {
		Method [] allMethods= parsedClass.getMethods();
		
		for(Method m: allMethods){
			if( m.getModifiers() == AccessCodes.ABSTRACT || 
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
				m.getModifiers() == AccessCodes.PUBLIC12) {
					
				String code =  m.getModifiers() 
						+ Constants.NEW_LINE
						+ m.getName()
						+ Constants.NEW_LINE
						+ m.getSignature()
						+ Constants.NEW_LINE
						+ m.getCode();
							
				String lineInfo = code.substring(code.indexOf("Attribute(s)") == -1
						? 0 : code.indexOf("Attribute(s)"), 
						code.indexOf("LocalVariable(") == -1?
						code.length() : code.indexOf("LocalVariable(")) ;
				
				// Changes in other function impacts line# of other functions
				//...so Linecount info of the code must be removed
				code = StringUtils.replace(code, lineInfo, ""); 
					
				// for some reason StackMapTable also change unwanted. WHY??
				code = code.substring(0, code.indexOf("StackMapTable") == -1? 
						code.length() : code.indexOf("StackMapTable"));  
				
				// for some reason StackMap also change unwanted. WHY??
				code = code.substring (0, 
						code.indexOf("StackMap") == -1? code.length() : code.indexOf("StackMap"));  
				
				String methodFqn = parsedClass.getClassName()+"."+m.getName();
	
				methodFqn += ("(");
				for(int i=0;i<m.getArgumentTypes().length;i++)
					methodFqn += ("$"+m.getArgumentTypes()[i]);
				methodFqn += (")");
				
				String currentHashCode = StringProcessor.CreateBLAKE(code);
				//if(!this.exists(Databases.TABLE_ID_ENTITY, methodFqn)){
				
				if (!allPreviousEntities.containsKey(methodFqn)) {				
					setChanged(true);
					boolean ret = sync(DatabaseIDs.TABLE_ID_ENTITY, methodFqn, currentHashCode);
					if (!ret) {
						throw new DatabaseSyncException(methodFqn);
					}
										
					TLDR.entityToTest.put(methodFqn, true);
					TLDR.allNewAndChangedentities.put(methodFqn, true);
					extractedChangedMethods.put(methodFqn, m);
				}
				
				else {
					allPreviousEntities.put(methodFqn, allPreviousEntities.get(methodFqn) + 1);
					String prevHashCode = this.getValue(DatabaseIDs.TABLE_ID_ENTITY, methodFqn);
					
					if (!currentHashCode.equals(prevHashCode)) {	
						setChanged(true);
						boolean ret = sync(DatabaseIDs.TABLE_ID_ENTITY, methodFqn, currentHashCode);
						if(!ret){
							throw new DatabaseSyncException(methodFqn);
						}
						TLDR.entityToTest.put(methodFqn, true);
						TLDR.allNewAndChangedentities.put(methodFqn, true);
						extractedChangedMethods.put(methodFqn, m);
					}
				}
			}
		}
	}
	
	protected void parse()  {
		try {
			parseFields(); // Parses fields
			parseMethods(); // Parses methods
		} catch (IOException  ioException) {
			// TODO Auto-generated catch block
			ioException.printStackTrace();
		} catch (DatabaseSyncException databaseSyncException) {
			databaseSyncException.printStackTrace();
		}	
	}
	
	private long deleteDepreciatedEntities() {
		long count = 0;
		for ( Map.Entry<String, Integer> entry : allPreviousEntities.entrySet()) {
		    Integer val = entry.getValue();
		    if (val == 0) {
		    	count++;
		    	String key = entry.getKey();
		    	this.redisHandler.removeKey(DatabaseIDs.TABLE_ID_ENTITY, key);
		    	Set<String> allDependencies = this.redisHandler.getSet
		    			(DatabaseIDs.TABLE_ID_FORWARD_INDEX_DEPENDENCY, key);
		    	redisHandler.removeKey(DatabaseIDs.TABLE_ID_FORWARD_INDEX_DEPENDENCY, key);
		    	for (String dep: allDependencies) {
		    		redisHandler.removeFromSet(DatabaseIDs.TABLE_ID_DEPENDENCY, dep, key);
		    	}
		    }  
		}
		return count;
	}
	
	//for debug purpose
	private void printMethod (Method m) {
		StringBuilder sb = new StringBuilder();	
		sb.append("NAME : "+m.getName());
		sb.append("CODE : \n"+ m.getCode().toString());
		sb.append("============================");
		sb.append("============================");
		System.out.println(sb.toString());
	}
	
	public HashMap<String, Method> getChangedMethods(){
		return extractedChangedMethods;
	}
}
