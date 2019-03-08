package uci.ics.mondego.tldr.worker;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import uci.ics.mondego.tldr.App;
import uci.ics.mondego.tldr.resolution.DFSTraversal;

public class DFSTraversalWorker extends Worker{

	private final String entity;
	private static final Logger logger = LogManager.getLogger(DFSTraversalWorker.class);
	public DFSTraversalWorker(String entity){
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
	
	private void extractTransitiveDependency() throws InstantiationException, 
	    IllegalAccessException, IllegalArgumentException, InvocationTargetException, 
	    NoSuchMethodException, SecurityException{
		DFSTraversal dfs = new DFSTraversal();	             
	    List<String> dep = dfs.get_all_dependent(entity);
	    dfs.closeRedis();
	   // logger.debug(entity+" -- DFS TRaversal done, entity to test is written to App");
	    for(int i=0;i<dep.size();i++){
	    	App.entityToTest.put(dep.get(i), true);
	    		//App.entityToTestMap.send(dep.get(i));
	    }      
	}

}
