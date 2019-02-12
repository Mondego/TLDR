package uci.ics.mondego.tldr.worker;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.bcel.classfile.Method;

import uci.ics.mondego.tldr.App;
import uci.ics.mondego.tldr.changeanalyzer.ClassChangeAnalyzer;

public class ClassChangeAnalyzerWorker extends Worker{

	private final String className;
	public ClassChangeAnalyzerWorker(String name, String className){
		super(name);
		this.className = className;
	}
	public void run() {
		// TODO Auto-generated method stub
		try {
            this.changeAnalyzer();
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }	
	}
	
	private void changeAnalyzer(){
	    ClassChangeAnalyzer cc;
		try {
			cc = new ClassChangeAnalyzer(className);
			Map<String, Method> m = cc.getextractedFunctions();
	 		Set<Entry<String,Method>> allEntries = m.entrySet();
	 		for(Entry<String, Method> e: allEntries){
	 			App.dependencyExtractor.send(e);
	 		}	
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 	 	      
	}	
}
