package uci.ics.mondego.tldr.worker;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.bcel.classfile.Method;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uci.ics.mondego.tldr.TLDR;
import uci.ics.mondego.tldr.changeanalyzer.DependencyExtractor;

/**
 * This worker extracts dependencies of a method and update the dependencies in the 
 * redis database.
 * @author demigorgan
 *
 */
@SuppressWarnings("rawtypes")
public class DependencyExtractorWorker extends Worker { 
	private final Map<String, Method> changedMethods;
	private static final Logger logger = LogManager.getLogger(DependencyExtractorWorker.class);
	
	public DependencyExtractorWorker(HashMap<String, Method> changedMethod){
		this.changedMethods = changedMethod;
	}
	
	public void run() {
		try {
            this.resolute();
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
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	
	public void resolute() 
			throws InstantiationException, 
			IllegalAccessException, 
			IllegalArgumentException, 
			InvocationTargetException, 
			NoSuchMethodException, 
			SecurityException, 
			IOException {		
		
		Set<Map.Entry<String, Method>> allEntries = changedMethods.entrySet();
		for (Map.Entry<String, Method> entry: allEntries) {			
			DependencyExtractor dep = new DependencyExtractor(entry);
			Set<String> fieldsChanged = dep.getFieldValueChanged();
			logger.debug(entry.getKey()+" changed/new, dependency synced, and sent to DFSTraversal");
			TLDR.DependencyGraphTraversalPool.send(entry.getKey());
			for (String field: fieldsChanged) {
				logger.debug(field+" value changed, sent to DFS");
				TLDR.DependencyGraphTraversalPool.send(field);
			}
		}
	}
}
