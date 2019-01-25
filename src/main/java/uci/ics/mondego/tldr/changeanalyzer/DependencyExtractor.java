package uci.ics.mondego.tldr.changeanalyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import uci.ics.mondego.tldr.extractor.MethodParser;
import uci.ics.mondego.tldr.indexer.RedisHandler;
import uci.ics.mondego.tldr.tool.AccessCodes;
import uci.ics.mondego.tldr.tool.Databases;

public class DependencyExtractor {

	private final Map<String, Method> changedMethods;
	private final RedisHandler rh;
	private static final Logger logger = LogManager.getLogger(ClassChangeAnalyzer.class);
	
	public DependencyExtractor(Map<String, Method> allChangedMethods) {
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
			Set<String> keys = rh.getAllKeys(Databases.TABLE_ID_ENTITY, pattern);
			
			if(CollectionUtils.isEmpty(keys))
				return;
			
			for(String k: keys){
				addDependentsInDb(k, dependents);
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
