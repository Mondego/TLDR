package uci.ics.mondego.tldr.ChangeAnalysisExperiment;

import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import redis.clients.jedis.exceptions.JedisConnectionException;
import uci.ics.mondego.tldr.exception.NullDbIdException;
import uci.ics.mondego.tldr.indexer.RedisHandler;
import uci.ics.mondego.tldr.tool.AccessCodes;
import uci.ics.mondego.tldr.tool.Databases;
import uci.ics.mondego.tldr.tool.StringProcessor;
import uci.ics.mondego.tldr.worker.Worker;

public class MethodChangeAnalyzer extends Worker{
	private final String className;
	private static final RedisHandler redisHandler = new RedisHandler();
	private static final Logger logger = LogManager.getLogger(MethodChangeAnalyzer.class);
	
	public MethodChangeAnalyzer (String className) {
		this.className = className;
	}
	
	public MethodChangeAnalyzer(String name, String className) {
		super(name);
		this.className = className;
	}
	
	public void run() {
		try {
			getChangedEntities(className);
        } 
		catch (NoSuchElementException e) {
            e.printStackTrace();
        } 
		catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JedisConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullDbIdException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	/**
	 * Returns the set of changed fields and methods of a given class.
	 */
	private Set<String> getChangedEntities(String file) 
			throws JedisConnectionException, 
			NullDbIdException, 
			ClassFormatException, 
			IOException {
		ClassParser parser = new ClassParser(file);	
		JavaClass parsedClass = parser.parse();
		Set<String> changedEntities = new HashSet<String>();
		
		Field [] allFields = parsedClass.getFields();
		
		for(Field f: allFields){
			String fieldFqn = parsedClass.getClassName()+"."+f.getName();
			String currentHashCode = StringProcessor.CreateBLAKE(f.toString());
			
			if(!redisHandler.exists(Databases.TABLE_ID_ENTITY, fieldFqn)){
				changedEntities.add(fieldFqn);
				redisHandler.update(Databases.TABLE_ID_ENTITY,fieldFqn, currentHashCode);
			} else {
				String prevHashCode = redisHandler.getValueByKey(Databases.TABLE_ID_ENTITY, fieldFqn);
				if (!currentHashCode.equals(prevHashCode)) {
					changedEntities.add(fieldFqn);
					redisHandler.update(Databases.TABLE_ID_ENTITY,fieldFqn, currentHashCode);
				}
			}
		}
		
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
				
				if (!redisHandler.exists(Databases.TABLE_ID_ENTITY, methodFqn)) {
					changedEntities.add(methodFqn);
					redisHandler.update(Databases.TABLE_ID_ENTITY, methodFqn, currentHashCode);
				} else {
					String prevHashCode = redisHandler.getValueByKey(Databases.TABLE_ID_ENTITY, methodFqn);
					if (!currentHashCode.equals(prevHashCode)) {
						changedEntities.add(methodFqn);
						redisHandler.update(Databases.TABLE_ID_ENTITY, methodFqn, currentHashCode);
					}
				}
			}
		}
		return changedEntities;
	}	
}
