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
import uci.ics.mondego.tldr.exception.UnknownDBIdException;
import uci.ics.mondego.tldr.extractor.MethodParser;
import uci.ics.mondego.tldr.indexer.RedisHandler;
import uci.ics.mondego.tldr.tool.Databases;

public class DependencyExtractor2 {

	protected final Entry<String, Method> changedMethod;
	protected final RedisHandler database;
	public static final Logger logger = LogManager.getLogger(DependencyExtractor2.class);
	private final String dbId;
	private Set<String> fieldValueChanged;
	private Map<String, Integer> previousDependencies;
	private final boolean flag;
	private Set<String> allVirtualDependency;
	private Set<String> allInterfaceDependency;
	private Set<String> allStaticDependency;
	private Set<String> allFinalDependency;
	private Set<String> allSpecialDependency;
	private Set<String> allFieldDependency;
	private Set<String> allStaticFieldUpdated;
	private Set<String> allOwnFieldUpdated;
	
	public DependencyExtractor2(Entry<String, Method> changedMethod) throws IOException {
		this.changedMethod = changedMethod;
		this.flag = false;
		this.dbId = Databases.TABLE_ID_DEPENDENCY;
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
		Set<String> prevDepInSet = database.getSet(Databases.TABLE_ID_FORWARD_INDEX_DEPENDENCY, 
				changedMethod.getKey());
		
		for(String dependency: prevDepInSet){
			previousDependencies.put(dependency, 0);
		}

		this.resolute();
		this.removeAllDepreciateDependency();
		database.close();
	}
	
	public DependencyExtractor2(Entry<String, Method> changedMethod, boolean flag) throws IOException{
		this.changedMethod = changedMethod;
		this.flag = flag;
		this.dbId = Databases.TABLE_ID_TEST_DEPENDENCY;
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
		
		Set<String> prevDepInSet = database.getSet(Databases.TABLE_ID_FORWARD_INDEX_TEST_DEPENDENCY, 
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
					
			
			this.allVirtualDependency = parser.getAllVirtualDependency();
			this.allInterfaceDependency = parser.getAllInterfaceDependency();
			this.allStaticDependency = parser.getAllStaticDependency();
			this.allFinalDependency = parser.getAllFinalDependency();
			this.allSpecialDependency = parser.getAllSpecialDependency();
			this.allStaticFieldUpdated = parser.getAllStaticFieldUpdated();
			this.allOwnFieldUpdated = parser.getAllOwnFieldUpdated();
			this.allFieldDependency = parser.getAllFieldDependency();			
			
			/*if(dependent.contains("testThreadFactory")){
				System.out.println(m.getCode());
				System.out.println(parser);
			}*/
			
			for(String field: allStaticFieldUpdated){
				if(!(field.startsWith("java.lang") || field.startsWith("java.util") || 
						field.startsWith("java.io")|| field.startsWith("java.net") || 
						field.startsWith("java.awt"))){
					this.fieldValueChanged.add(field);
				}
				else if(!(field.startsWith("java/lang") || field.startsWith("java/util") || 
						field.startsWith("java/io")|| field.startsWith("java/net") || 
						field.startsWith("java/awt"))){
					this.fieldValueChanged.add(field);
				}
			}
			
			for(String field: allOwnFieldUpdated){
				if(!(field.startsWith("java.lang") || field.startsWith("java.util") || 
						field.startsWith("java.io")|| field.startsWith("java.net") || 
						field.startsWith("java.awt"))){
					this.fieldValueChanged.add(field);
				}
				else if(!(field.startsWith("java/lang") || field.startsWith("java/util") || 
						field.startsWith("java/io")|| field.startsWith("java/net") || 
						field.startsWith("java/awt"))){
					this.fieldValueChanged.add(field);
				}
			}
			
			/*for(String dep: allOwnFieldUpdated){
				String pkg = dep.substring(0,dep.lastIndexOf('.'));
				if(pkg.equals(dependent.substring(0,dependent.lastIndexOf('.')))){
					this.fieldValueChanged.add(dep);
				}
			}
			this.fieldValueChanged.addAll(allStaticFieldUpdated);
			this.fieldValueChanged.addAll(allOwnFieldUpdated);
			*/

			for(String dep: allVirtualDependency){
				this.syncSingleDependency(dep, dependent);
				this.syncAllPossibleDependency(dep, dependent);
			}
			
			for(String dep: allInterfaceDependency){
				this.syncSingleDependency(dep, dependent);
				this.syncAllPossibleDependency(dep, dependent);
			}
			
			for(String dep: allStaticDependency){
				this.syncSingleDependency(dep, dependent);
			}
			
			for(String dep: allFinalDependency){
				this.syncSingleDependency(dep, dependent);
			}
			
			for(String dep: allSpecialDependency){
				this.syncSingleDependency(dep, dependent);
			}
			
			for(String dep: allFieldDependency){
				this.syncSingleDependency(dep, dependent);
			}
	}
	
	protected void addDependentsInDb(String dependency, String dependents) throws UnknownDBIdException{
		if(this.dbId.length() == 0 || this.dbId == null){
			throw new UnknownDBIdException(dbId);
		}
		
		if(dependency.startsWith("java."))
			return;
		if(dependency.startsWith("org.junit."))
			return;
		if(dependency.startsWith("org.mockito."))
			return;
		if(dependency.startsWith("org.hamcrest."))
			return;		
		
		if(previousDependencies.containsKey(dependency)){
			// we increase the hashmap value which is used later to remove depreciated dependencies
			previousDependencies.put(dependency, previousDependencies.get(dependency) + 1);
			return;
		}
		else{
			this.database.insertInSet(this.dbId, dependency, dependents);
			//logger.info(dependents+ " has been updated as "+dependency+" 's dependent in "+this.dbId);
		}
	}
	
	private long removeAllDepreciateDependency(){
		long count = 0;
		for ( Map.Entry<String, Integer> entry : previousDependencies.entrySet()) {
		    Integer val = entry.getValue();
		    if(val == 0){
		    	String key = entry.getKey();
		    	
		    	count += this.database.removeFromSet( !flag ? Databases.TABLE_ID_FORWARD_INDEX_DEPENDENCY : 
		    		Databases.TABLE_ID_FORWARD_INDEX_TEST_DEPENDENCY, 
		    		changedMethod.getKey(), key);
		    	
		    	this.database.removeFromSet(!flag ? Databases.TABLE_ID_DEPENDENCY: Databases.TABLE_ID_TEST_DEPENDENCY, 
		    			key, changedMethod.getKey());
		    	
		    	//logger.info(changedMethod.getKey()+ " has been removed as "+key+" s dependency  in "+
		    	//		(!flag ? Databases.TABLE_ID_DEPENDENCY: Databases.TABLE_ID_TEST_DEPENDENCY));
		    }  
		}
		return count;
	}
	
	protected List<String> traverseClassHierarchyDownwards(String claz, String pattern){
		
		List<String> toTest = new ArrayList<String>();

		Set<String> entity = this.database.getAllKeysByPattern(Databases.TABLE_ID_ENTITY, claz+"."+pattern);

		//System.out.println(entity);
		
		for(String e: entity){
			toTest.add(e.substring(1));
		}		
		
		Set<String> allSubclass = this.database.getSet(Databases.TABLE_ID_SUBCLASS, claz);
		
		//System.out.println("#####"+allSubclass);
		
		for(String sub: allSubclass){
			List<String> t = traverseClassHierarchyDownwards(sub, pattern);
			
			if(!t.isEmpty() || t!= null)
				toTest.addAll(t);
		}
		return toTest;
	}
	
	protected void syncAllPossibleDependency(String dependency, String dependents){
		
		// JDK DEPENDENCY IGNORED
		if(dependency.startsWith("java.lang") || dependency.startsWith("java.util") || 
				dependency.startsWith("java.io")|| dependency.startsWith("java.net") || 
				dependency.startsWith("java.awt"))
			return;
		
		if(dependency.startsWith("java/lang") || dependency.startsWith("java/util") || 
				dependency.startsWith("java/io")|| dependency.startsWith("java/net") || 
				dependency.startsWith("java/awt"))
			return;
		
		try{
			int index = dependency.indexOf("(");
			StringBuilder sb = new StringBuilder();
			sb.append(dependency.substring(index));
			for(int i = index - 1; i>=0; i--){
				if(dependency.charAt(i) == '.')
					break;
				sb.insert(0, dependency.charAt(i));
			}
			
			String pattern = sb.toString();
			String claz = dependency.substring(0, dependency.indexOf(pattern) >=0? 
					dependency.indexOf(pattern) - 1: dependency.length());
			
			List<String> keys = traverseClassHierarchyDownwards(claz, pattern);
			
			if(CollectionUtils.isEmpty(keys))
				return;
			
			for(String k: keys){
				addDependentsInDb(k, dependents); // because we have to remove table id
			}
		}
		
		catch(NullPointerException e){
			e.printStackTrace();
			logger.error("Problem is syncing dependencies of changed entities"+e.getMessage());
		} 
		catch(StringIndexOutOfBoundsException e){
			e.printStackTrace();
		}
		catch (UnknownDBIdException e) {
			e.printStackTrace();
		}
	}
	
	
	protected void syncSingleDependency(String dependency, String dependents){

		try{
			addDependentsInDb(dependency, dependents);
		}
		catch(NullPointerException e){
			e.printStackTrace();
			logger.error("Problem is syncing dependencies of changed entities"+e.getMessage());
		} 
		catch (UnknownDBIdException e) {
			e.printStackTrace();
		}
	}
	
	public Set<String> getFieldValueChanged(){
		return fieldValueChanged;
	}
}
