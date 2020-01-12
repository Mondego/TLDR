package uci.ics.mondego.tldr.worker;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.NoSuchElementException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uci.ics.mondego.tldr.TLDR;
import uci.ics.mondego.tldr.resolution.IntraTestDFSTraversal;

public class IntraTestTraversalWorker extends Worker{

	private final String entity;
	private static final Logger logger = LogManager.getLogger(DFSTraversalWorker.class);
	
	public IntraTestTraversalWorker(String entity){
		this.entity = entity;
	}
	
	public void run() {
		// TODO Auto-generated method stub
		try {
            this.extractTransitiveDependency();
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	private void extractTransitiveDependency() 
			throws InstantiationException, 
			IllegalAccessException, 
			IllegalArgumentException, 
			InvocationTargetException, 
			NoSuchMethodException, 
			SecurityException {
				
		IntraTestDFSTraversal dfs = new IntraTestDFSTraversal();	             
	    List<String> dep = dfs.get_all_dependent(entity);
	    dfs.closeRedis();
	    
	   // logger.debug(entity+" -- Intratest DFS TRaversal done, test is written to App");
	    for(int i=0;i<dep.size();i++) {
	    	TLDR.completeTestCaseSet.put(dep.get(i), 1);
	    }      
	}
}
