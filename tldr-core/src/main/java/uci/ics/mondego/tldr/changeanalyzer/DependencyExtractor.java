package uci.ics.mondego.tldr.changeanalyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.bcel.classfile.Method;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uci.ics.mondego.tldr.exception.NullDbIdException;
import uci.ics.mondego.tldr.exception.UnknownDBIdException;
import uci.ics.mondego.tldr.extractor.MethodParser;
import uci.ics.mondego.tldr.indexer.RedisHandler;
import uci.ics.mondego.tldr.tool.Constants;
import uci.ics.mondego.tldr.tool.DatabaseIDs;

public class DependencyExtractor {

	protected final Entry<String, Method> changedMethod;
	protected final RedisHandler database;
	public static final Logger logger = LogManager.getLogger(DependencyExtractor.class);
	private final String dbId;
	private final boolean flag;
	
	private Set<String> fieldValueChanged;
	private Map<String, Integer> previousDependencies;

	private Set<String> allVirtualDependency;
	private Set<String> allInterfaceDependency;
	private Set<String> allStaticDependency;
	private Set<String> allFinalDependency;
	private Set<String> allSpecialDependency;
	private Set<String> allFieldDependency;
	
	private Set<String> allStaticFieldUpdated;
	private Set<String> allOwnFieldUpdated;
	
	public DependencyExtractor(Entry<String, Method> changedMethod) throws IOException {
		this.changedMethod = changedMethod;
		this.flag = false;
		this.dbId = DatabaseIDs.TABLE_ID_DEPENDENCY;
		this.database = new RedisHandler();
		this.fieldValueChanged = new HashSet<String>();
		this.allVirtualDependency = new HashSet<String>();
		this.allInterfaceDependency = new HashSet<String>();
		this.allStaticDependency = new HashSet<String>();
		this.allFinalDependency = new HashSet<String>();
		this.allSpecialDependency = new HashSet<String>();
		this.allStaticFieldUpdated = new HashSet<String>();
		this.allOwnFieldUpdated = new HashSet<String>();
		this.allFieldDependency = new HashSet<String>();
		
		this.previousDependencies = new HashMap<String, Integer>();	
		Set<String> prevDepInSet = database.getSet (
				DatabaseIDs.TABLE_ID_FORWARD_INDEX_DEPENDENCY, 
				changedMethod.getKey());
		
		for(String dependency: prevDepInSet){
			previousDependencies.put(dependency, 0);
		}

		this.resolute();
		this.removeAllDepreciateDependency();
		database.close();
	}
	
	public DependencyExtractor (Entry<String, Method> changedMethod, boolean flag) throws IOException {
		this.changedMethod = changedMethod;
		this.flag = flag;
		this.dbId = DatabaseIDs.TABLE_ID_TEST_DEPENDENCY;
		this.fieldValueChanged = new HashSet<String>();
		this.database = new RedisHandler();
		this.previousDependencies = new HashMap<String, Integer>();	
		this.allVirtualDependency = new HashSet<String>();
		this.allInterfaceDependency = new HashSet<String>();
		this.allStaticDependency = new HashSet<String>();
		this.allFinalDependency = new HashSet<String>();
		this.allSpecialDependency = new HashSet<String>();
		this.allStaticFieldUpdated = new HashSet<String>();
		this.allOwnFieldUpdated = new HashSet<String>();
		this.allFieldDependency = new HashSet<String>();
		
		Set<String> prevDepInSet = database.getSet(
				DatabaseIDs.TABLE_ID_FORWARD_INDEX_TEST_DEPENDENCY, 
				changedMethod.getKey());
		
		for(String dependency: prevDepInSet){
			previousDependencies.put(dependency, 0);
		}
				
		this.resolute();
		this.removeAllDepreciateDependency();
		database.close();
	}
	
	 public void resolute() throws IOException{
		 
			String dependent = changedMethod.getKey();
			Method m = changedMethod.getValue();
			MethodParser parser = new MethodParser(m);
								
			allVirtualDependency = parser.getAllVirtualDependency();
			allInterfaceDependency = parser.getAllInterfaceDependency();
			allStaticDependency = parser.getAllStaticDependency();
			allFinalDependency = parser.getAllFinalDependency();
			allSpecialDependency = parser.getAllSpecialDependency();
			allStaticFieldUpdated = parser.getAllStaticFieldUpdated();
			allOwnFieldUpdated = parser.getAllOwnFieldUpdated();
			allFieldDependency = parser.getAllFieldDependency();			
			
			for(String field: allStaticFieldUpdated) {
				if( !(field.startsWith("java.lang") 
						|| field.startsWith("java.util") 
						|| field.startsWith("java.io")
						|| field.startsWith("java.net") 
						|| field.startsWith("java.awt"))) {
					
					this.fieldValueChanged.add(field);
				}
				else if(!(field.startsWith("java/lang") 
						|| field.startsWith("java/util") 
						|| field.startsWith("java/io")
						|| field.startsWith("java/net") 
						|| field.startsWith("java/awt"))) {
					
					this.fieldValueChanged.add(field);
				}
			}
			
			for(String field: allOwnFieldUpdated){
				if(!(field.startsWith("java.lang") 
						|| field.startsWith("java.util") 
						|| field.startsWith("java.io")
						|| field.startsWith("java.net") 
						|| field.startsWith("java.awt"))){
					
					this.fieldValueChanged.add(field);
				}
				else if(!(field.startsWith("java/lang") 
						|| field.startsWith("java/util")
						|| field.startsWith("java/io")
						|| field.startsWith("java/net") 
						|| field.startsWith("java/awt"))) {
					
					this.fieldValueChanged.add(field);
				}
			}
			
			for(String dep: allVirtualDependency) {				
				this.syncAllPossibleDependency(dep, dependent);
			}
			
			for(String dep: allInterfaceDependency) {
				this.syncAllPossibleDependency(dep, dependent);
			}
			
			for (String dep: allStaticDependency) {
				this.syncAllPossibleDependency(dep, dependent);
				
				/*** this is commented out based on reviews got *****/
				//this.syncSingleDependency(dep, dependent);
			}
			
			for(String dep: allFinalDependency) {
				this.syncSingleDependency(dep, dependent);
			}
			
			for(String dep: allSpecialDependency) {
				this.syncSingleDependency(dep, dependent);
			}
			
			for(String dep: allFieldDependency) {
				this.syncSingleDependency(dep, dependent);
			}
	}
	
	protected void addDependentsInDb(String dependency, String dependents) 
			throws UnknownDBIdException, NullDbIdException {
		if(dbId.length() == 0 || dbId == null){
			throw new UnknownDBIdException(dbId);
		}
		if(dependency.startsWith("java.")
				|| dependency.startsWith("org.junit.")
				|| dependency.startsWith("org.mockito.")
				|| dependency.startsWith("org.hamcrest.")) {
			return;
		}
		
		if (previousDependencies.containsKey(dependency)) {
			// we increase the hashmap value which is used later to remove depreciated dependencies
			previousDependencies.put(dependency, previousDependencies.get(dependency) + 1);
			return;
		}
		else{
			this.database.insertInSet(dbId, dependency, dependents);
		}
	}
	
	private long removeAllDepreciateDependency() {
		long count = 0;
		for ( Map.Entry<String, Integer> entry : previousDependencies.entrySet()) {
		    Integer val = entry.getValue();
		    if (val == 0) {
		    	String key = entry.getKey();
		    	
		    	count += this.database.removeFromSet( 
		    			!flag 
		    			? DatabaseIDs.TABLE_ID_FORWARD_INDEX_DEPENDENCY 
		    			: DatabaseIDs.TABLE_ID_FORWARD_INDEX_TEST_DEPENDENCY, 
		    		    changedMethod.getKey(), 
		    		    key);
		    	
		    	this.database.removeFromSet(
		    			!flag 
		    			? DatabaseIDs.TABLE_ID_DEPENDENCY
		    			: DatabaseIDs.TABLE_ID_TEST_DEPENDENCY, 
		    			key, 
		    			changedMethod.getKey());		    	
		    }  
		}
		return count;
	}
	
	protected List<String> traverseClassHierarchyDownwards(String claz, String pattern){	
		List<String> toTest = new ArrayList<String>();
		if(this.database.exists(DatabaseIDs.TABLE_ID_ENTITY, claz+"."+pattern)) {
			toTest.add(claz+"."+pattern);	
		}
		
		for(String sub: database.getSet(DatabaseIDs.TABLE_ID_SUBCLASS, claz)) {
			List<String> t = traverseClassHierarchyDownwards(sub, pattern);	
			if(!t.isEmpty() && t!= null) {
				toTest.addAll(t);
			}				
		}
		return toTest;
	}
	
	protected List<String> traverseClassHierarchyUpwards(String claz, String pattern){	
		List<String> toTest = new ArrayList<String>();
		
		if (this.database.exists(DatabaseIDs.TABLE_ID_ENTITY, claz+"."+pattern)) {
			toTest.add(claz+"."+pattern);
			return toTest;
		}
		
		for (String sup: database.getSet(DatabaseIDs.TABLE_ID_INTERFACE_SUPERCLASS, claz)) {
			List<String> t = traverseClassHierarchyUpwards(sup, pattern);	
			if (!t.isEmpty() && t!= null) {
				toTest.addAll(t);
			}
		}
		return toTest;
	}
		
	protected void syncAllPossibleDependency(String dependency, String dependents){
		
		// JDK DEPENDENCY IGNORED
		if(dependency.startsWith("java.lang") 
				|| dependency.startsWith("java.util") 
				|| dependency.startsWith("java.io")
				|| dependency.startsWith("java.net") 
				|| dependency.startsWith("java.awt")) {
			return;
		}
		
		if(dependency.startsWith("java/lang") 
				|| dependency.startsWith("java/util") 
				|| dependency.startsWith("java/io")
				|| dependency.startsWith("java/net") 
				|| dependency.startsWith("java/awt")) {
			return;
		}
		
		try {
			int index = dependency.indexOf("(");
			StringBuilder sb = new StringBuilder();
			sb.append(dependency.substring(index));
			for (int i = index - 1; i >= 0; i--) {
				if (dependency.charAt(i) == Constants.DOT) {
					break;
				}
				sb.insert(0, dependency.charAt(i));
			}
			
			String pattern = sb.toString();
			String claz = dependency.substring(0, dependency.indexOf(pattern) >= 0 ? 
					dependency.indexOf(pattern) - 1: dependency.length());
				
			List<String> keysDown = traverseClassHierarchyDownwards(claz, pattern);
			if (!CollectionUtils.isEmpty(keysDown)) {
				for(String k: keysDown) {
					addDependentsInDb(k, dependents); // because we have to remove table id
				}
			}
						
			List<String> keysUp = traverseClassHierarchyUpwards(claz, pattern);
			
			if(CollectionUtils.isEmpty(keysUp)) {
				return;
			}
			
			for(String k: keysUp) {
				addDependentsInDb(k, dependents); // because we have to remove table id
			}			
		} 
		catch(NullPointerException e) {
			e.printStackTrace();
		} catch(StringIndexOutOfBoundsException e) {
			e.printStackTrace();
		} catch (UnknownDBIdException e) {
			e.printStackTrace();
		} catch (NullDbIdException e) {
			e.printStackTrace();
		}
	}
	
	protected void syncSingleDependency(String dependency, String dependents){
		try {
			addDependentsInDb(dependency, dependents);
		} 
		catch(NullPointerException e){
			e.printStackTrace();
		} catch (UnknownDBIdException e) {
			e.printStackTrace();
		} catch (NullDbIdException e) {
			e.printStackTrace();
		}
	}
	
	public Set<String> getFieldValueChanged(){
		return fieldValueChanged;
	}
}
