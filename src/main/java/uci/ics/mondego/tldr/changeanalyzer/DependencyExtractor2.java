package uci.ics.mondego.tldr.changeanalyzer;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.bcel.classfile.Method;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uci.ics.mondego.tldr.extractor.MethodParser;
import uci.ics.mondego.tldr.indexer.RedisHandler;
import uci.ics.mondego.tldr.tool.Databases;

public class DependencyExtractor2 {

	private final Entry<String, Method> changedMethod;
	private final RedisHandler rh;
	private static final Logger logger = LogManager.getLogger(ClassChangeAnalyzer.class);
	
	public DependencyExtractor2(Entry<String, Method> changedMethod) throws IOException {
		this.changedMethod = changedMethod;
		this.rh = new RedisHandler();
		this.resolute();
		rh.close();
	}
	
	 public void resolute() throws IOException{

			String dependent = changedMethod.getKey();
			Method m = changedMethod.getValue();
			MethodParser parser = new MethodParser(m);
			List<String> allVirtualDependency = parser.getAllVirtualDependency();
			List<String> allInterfaceDependency = parser.getAllInterfaceDependency();
			List<String> allStaticDependency = parser.getAllStaticDependency();
			List<String> allFinalDependency = parser.getAllFinalDependency();
						
			for(int i = 0 ;i<allVirtualDependency.size();i++){
				this.syncAllPossibleDependency(allVirtualDependency.get(i), dependent);
			}
			
			for(int i = 0;i<allInterfaceDependency.size();i++){
				this.syncAllPossibleDependency(allInterfaceDependency.get(i), dependent);
			}
			
			for(int i = 0;i<allStaticDependency.size();i++){
				this.syncSingleDependency(allStaticDependency.get(i), dependent);
			}
			
			for(int i = 0;i<allFinalDependency.size();i++){
				this.syncSingleDependency(allFinalDependency.get(i), dependent);
			}
	}
	
	private void addDependentsInDb(String dependency, String dependents){
		//System.out.println("inside add db: "+dependency+"  "+dependents);

		if(dependency.contains("java."))
			return;

		Set<String> prevDependents = this.rh.getSet(Databases.TABLE_ID_DEPENDENCY, dependency);
		if(!prevDependents.contains(dependents)){
			this.rh.insertInSet(Databases.TABLE_ID_DEPENDENCY, dependency, dependents);
			logger.info(dependents+ " has been updated as "+dependency+" 's dependent");
		}
	}
	
	private List<String> traverseClassHierarchy(String claz, String pattern){
		List<String> toTest = new ArrayList<String>();

		Set<String> entity = this.rh.getAllKeys(Databases.TABLE_ID_ENTITY, claz+"."+pattern);
		
		for(String e: entity){
			toTest.add(e.substring(1));
		}		
		Set<String> allSubclass = this.rh.getSet(Databases.TABLE_ID_SUBCLASS, claz);
		
		for(String sub: allSubclass){
			List<String> t = traverseClassHierarchy(sub, pattern);
			
			if(!t.isEmpty() || t!= null)
				toTest.addAll(t);
		}

		return toTest;
	}
	
	private void syncAllPossibleDependency(String dependency, String dependents){
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
			String claz = dependency.substring(0, dependency.indexOf(pattern) - 1);
						
			List<String> keys = traverseClassHierarchy(claz, pattern);
			
			if(CollectionUtils.isEmpty(keys))
				return;
			//System.out.println("inside sync all: "+claz+"  "+pattern);
			//System.out.println("inside ss db: "+keys+"\n========================\n");

			for(String k: keys){
				addDependentsInDb(k, dependents); // because we have to remove table id
			}
		}
		
		catch(NullPointerException e){
			logger.error("Problem is syncing dependencies of changed entities"+e.getMessage());
		}
	}
	
	
	private void syncSingleDependency(String dependency, String dependents){

		try{
			addDependentsInDb(dependency, dependents);
		}
		catch(NullPointerException e){
			logger.error("Problem is syncing dependencies of changed entities"+e.getMessage());
		}
	}
}
