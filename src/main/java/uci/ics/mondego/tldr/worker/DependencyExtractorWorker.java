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
	private final Map<String, Method> changedMethod;
	private static final Logger logger = LogManager.getLogger(ClassChangeAnalyzer.class);
	
	public DependencyExtractorWorker(HashMap<String, Method> changedMethod){
		this.changedMethod = changedMethod;
	}
	
	public void run() {
		// TODO Auto-generated method stub
		try {
            this.resolute();
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	
	public void resolute() throws InstantiationException, IllegalAccessException, 
		IllegalArgumentException, InvocationTargetException, NoSuchMethodException, 
		SecurityException, IOException{		
		
		Set<Map.Entry<String, Method>> allEntries = changedMethod.entrySet();
		for(Map.Entry<String, Method> entry: allEntries){
			DependencyExtractor2 dep = new DependencyExtractor2(entry);
			Set<String> fieldsChanged = dep.getFieldValueChanged();
			App.traverseDependencyGraph.send(entry.getKey());
			for(String field: fieldsChanged){
				App.traverseDependencyGraph.send(field);
			}
		}
	}
	
}
