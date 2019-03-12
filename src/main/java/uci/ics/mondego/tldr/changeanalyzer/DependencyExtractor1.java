package uci.ics.mondego.tldr.changeanalyzer;

import java.io.IOException;
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

/***
 * 
 * @author demigorgan
 *
 */
// this class is another approach without building and traversing classfierarchy and by simply
// querying method signature

public class DependencyExtractor1 {

	/*private final Map<String, Method> changedMethods;
	private final RedisHandler rh;
	private static final Logger logger = LogManager.getLogger(ClassChangeAnalyzer.class);
	
	public DependencyExtractor1(Map<String, Method> allChangedMethods) {
		this.changedMethods = allChangedMethods;
		this.rh = RedisHandler.getInstane();
		
	}
		
	
	 public void resolute() throws IOException{

		Set<Entry<String, Method>> allEntries = changedMethods.entrySet();
		
		for(Entry<String, Method> entry: allEntries){
			String dependent = entry.getKey();
			Method m = entry.getValue();
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
	}
	
	private void addDependentsInDb(String dependency, String dependents){
		//System.out.println("here : "+dependency+"   "+dependents);
		Set<String> prevDependents = this.rh.getSet(Databases.TABLE_ID_DEPENDENCY, dependency);
		if(!prevDependents.contains(dependents)){
			this.rh.insertInSet(Databases.TABLE_ID_DEPENDENCY, dependency, dependents);
			logger.info(dependents+ " has been updated as "+dependency+" 's dependent");
		}
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
			
			sb.insert(0,'*');
			String pattern = sb.toString();
			Set<String> keys = rh.getAllKeysByPattern(Databases.TABLE_ID_ENTITY, pattern);
			
			if(CollectionUtils.isEmpty(keys))
				return;
			
			for(String k: keys){
				addDependentsInDb(k.substring(1), dependents); // because we have to remove table id
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
	*/
}
