package uci.ics.mondego.tldr.ChangeAnalysisExperiment;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.commons.lang3.StringUtils;

import redis.clients.jedis.exceptions.JedisConnectionException;
import uci.ics.mondego.tldr.exception.NullDbIdException;
import uci.ics.mondego.tldr.indexer.RedisHandler;
import uci.ics.mondego.tldr.tool.AccessCodes;
import uci.ics.mondego.tldr.tool.Constants;
import uci.ics.mondego.tldr.tool.DatabaseIDs;
import uci.ics.mondego.tldr.tool.StringProcessor;
import uci.ics.mondego.tldr.worker.Worker;

public class FieldWorker extends Worker{
	
	
	private String className;
	private RedisHandler redisHandler;
	
	private ClassParser parser;
	private JavaClass parsedClass;
	private Map<String, Integer> allPreviousEntities;
	
	public FieldWorker (String className) {
		this.parser = new ClassParser(className);
		try {
			this.parsedClass = parser.parse();
			this.className = className;
		} catch (ClassFormatException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public FieldWorker(String name, String className) {
		super(name);
		this.parser = new ClassParser(className);
		try {
			this.parsedClass = parser.parse();
			this.className = className;
		} catch (ClassFormatException | IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void run() {
		try {
			this.redisHandler = new RedisHandler();
			Set<String> prevEnt = redisHandler.getAllKeysByPattern(
					DatabaseIDs.TABLE_ID_ENTITY, parsedClass.getClassName()+".*");  
			
			this.allPreviousEntities= new HashMap<String, Integer>();
			for(String entity: prevEnt){
				allPreviousEntities.put(entity, 0);
			}
			
			this.anlysis(className);
			this.redisHandler.close();
        } 
		catch (NoSuchElementException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (JedisConnectionException e) {
			e.printStackTrace();
		} catch (ClassFormatException e) {
			e.printStackTrace();
		} catch (NullDbIdException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	private void anlysis(String file) 
			throws JedisConnectionException, NullDbIdException, ClassFormatException, IOException {
				
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

				ChangeAnalysis.methodData.incCount();
					
				String code =  m.getModifiers()+Constants.NEW_LINE + m.getName() + Constants.NEW_LINE + m.getSignature() + Constants.NEW_LINE + m.getCode();
							
				String lineInfo = code.substring(code.indexOf("Attribute(s)") == -1
						? 0 : code.indexOf("Attribute(s)"), 
						code.indexOf("LocalVariable(") == -1?
						code.length() : code.indexOf("LocalVariable(")) ;
				
				code = StringUtils.replace(code, lineInfo, ""); 
							
				code = code.substring(0, code.indexOf("StackMapTable") == -1? code.length() : code.indexOf("StackMapTable"));  
				
				code = code.substring(0, code.indexOf("StackMap") == -1? code.length() : code.indexOf("StackMap"));  
				
				String methodFqn = parsedClass.getClassName()+"."+m.getName();
	
				methodFqn += ("(");
				for(int i=0;i<m.getArgumentTypes().length;i++)
					methodFqn += ("$"+m.getArgumentTypes()[i]);
				methodFqn += (")");
				
				String currentHashCode = StringProcessor.CreateBLAKE(code);
				
				if (!allPreviousEntities.containsKey(methodFqn)) {
					ChangeAnalysis.methodData.addNew(methodFqn);
					redisHandler.update(DatabaseIDs.TABLE_ID_ENTITY, methodFqn, currentHashCode);
				} else {
					allPreviousEntities.put(methodFqn, allPreviousEntities.get(methodFqn) + 1);
					
					String prevHashCode = redisHandler.getValueByKey(DatabaseIDs.TABLE_ID_ENTITY, methodFqn);
					if (!currentHashCode.equals(prevHashCode)) {
						ChangeAnalysis.methodData.addChanged(methodFqn);
						redisHandler.update(DatabaseIDs.TABLE_ID_ENTITY, methodFqn, currentHashCode);
					}
				}
			}
		}
		deleteDepreciatedEntities();
	}	
	
	private void deleteDepreciatedEntities() {
		for ( Map.Entry<String, Integer> entry : allPreviousEntities.entrySet()) {
		    Integer val = entry.getValue();
		    if (val == 0) {
		    	ChangeAnalysis.methodData.addDeleted(entry.getKey());
		    	redisHandler.removeKey(DatabaseIDs.TABLE_ID_ENTITY, entry.getKey());
		    }  
		}
	}
}
