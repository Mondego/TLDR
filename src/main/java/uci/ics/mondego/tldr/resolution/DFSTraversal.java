package uci.ics.mondego.tldr.resolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DFSTraversal<T> {

	Map<T, Boolean> visitInfo;
	T source;
	List<T> all_dependents;
	
	public DFSTraversal(T source){
		this.source = source;
		visitInfo = new HashMap<T, Boolean>();
		all_dependents = new ArrayList<T>();
		
	}
	
	
}
