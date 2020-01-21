package uci.ics.mondego.tldr.worker;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import uci.ics.mondego.tldr.TLDR;
import uci.ics.mondego.tldr.resolution.DFSTraversal;

/**
 * This worker traverses the dependency graphs from the nodes i.e. methods/fields that 
 * have been impacted.
 * @author demigorgan
 *
 */
@SuppressWarnings("rawtypes")
public class DFSTraversalWorker extends Worker {
	private final String entity;
	private static final Logger logger = LogManager.getLogger(DFSTraversalWorker.class);
	
	public DFSTraversalWorker(String entity){
		this.entity = entity;
	}
	
	public void run() {
		try {
            this.extractTransitiveDependency();
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
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
		
		DFSTraversal dfs = new DFSTraversal();	             
	    List<String> dep = dfs.get_all_dependent(entity);
	    dfs.closeRedis();
	    logger.debug(entity+" -- DFS TRaversal done, entity to test is written to App");
	    for(int i=0;i<dep.size();i++){
	    	TLDR.entityToTest.put(dep.get(i), true);
	    }      
	}
}
