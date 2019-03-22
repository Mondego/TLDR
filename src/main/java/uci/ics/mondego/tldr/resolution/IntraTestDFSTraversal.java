package uci.ics.mondego.tldr.resolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uci.ics.mondego.tldr.indexer.RedisHandler;
import uci.ics.mondego.tldr.tool.Databases;

public class IntraTestDFSTraversal {

	private Map<String, Boolean> visitInfo;
	private String source;
	private List<String> trace;
	private final RedisHandler database;
	
	public IntraTestDFSTraversal(){
		this.visitInfo = new HashMap<String, Boolean>();
		this.trace = new ArrayList<String>();
		this.database = new RedisHandler();
	}
	
	public IntraTestDFSTraversal(String source){
		this.source = source;
		visitInfo = new HashMap<String, Boolean>();
		trace = new ArrayList<String>();
		this.database = new RedisHandler();
	}
	
	private void DFS(String node){
		
		visitInfo.put(node, true);
		if(database.exists(Databases.TABLE_ID_TEST_ENTITY, node)){
			trace.add(node);
			Set<String> all_dependents = database.getSet(Databases.TABLE_ID_TEST_DEPENDENCY, node);	
			for(String child: all_dependents){
				if(!visitInfo.containsKey(child) || !visitInfo.get(child))
					DFS(child);
			}
		}
	}
	
	public void closeRedis(){
		database.close();
	}
	
	public List<String> get_all_dependent(String node){
		trace.clear();
		visitInfo.clear();
		this.source = node;
		DFS(node);
		return trace;
	}
	
	public String toString(){
		StringBuilder str = new StringBuilder();
		for(int i=0;i<trace.size();i++)
			str.append(trace.get(i)+"\n");
		return str.toString();
	}
}
