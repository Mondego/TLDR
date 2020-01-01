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
import uci.ics.mondego.tldr.App;
import uci.ics.mondego.tldr.changeanalyzer.ClassChangeAnalyzer;
import uci.ics.mondego.tldr.changeanalyzer.DependencyExtractor2;


public class DependencyExtractorWorker extends Worker{
	private final Map<String, Method> changedMethods;
	private static final Logger logger = LogManager.getLogger(DependencyExtractorWorker.class);
	
	public DependencyExtractorWorker(HashMap<String, Method> changedMethod){
		this.changedMethods = changedMethod;
	}
	
	public void run() {
		// TODO Auto-generated method stub
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
	
	
	public void resolute() throws InstantiationException, IllegalAccessException, 
		IllegalArgumentException, InvocationTargetException, NoSuchMethodException, 
		SecurityException, IOException{		
		
		Set<Map.Entry<String, Method>> allEntries = changedMethods.entrySet();
		for(Map.Entry<String, Method> entry: allEntries){			
			DependencyExtractor2 dep = new DependencyExtractor2(entry);
			Set<String> fieldsChanged = dep.getFieldValueChanged();
			logger.debug(entry.getKey()+" changed/new, dependency synced, and sent to DFSTraversal");
			App.DependencyGraphTraversalPool.send(entry.getKey());
			for(String field: fieldsChanged){
				logger.debug(field+" value changed, sent to DFS");
				App.DependencyGraphTraversalPool.send(field);
			}
		}
	}
}
