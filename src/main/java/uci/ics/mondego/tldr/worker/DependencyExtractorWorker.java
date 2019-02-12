package uci.ics.mondego.tldr.worker;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import org.apache.bcel.classfile.Method;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uci.ics.mondego.tldr.App;
import uci.ics.mondego.tldr.changeanalyzer.ClassChangeAnalyzer;
import uci.ics.mondego.tldr.changeanalyzer.DependencyExtractor2;
import uci.ics.mondego.tldr.indexer.RedisHandler;


public class DependencyExtractorWorker extends Worker{
	private final Entry<String, Method> changedMethod;
	private final RedisHandler rh;
	private static final Logger logger = LogManager.getLogger(ClassChangeAnalyzer.class);
	

	public DependencyExtractorWorker(Entry<String, Method> changedMethod){
		this.changedMethod = changedMethod;
		this.rh = RedisHandler.getInstane();
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
		DependencyExtractor2 dep = new DependencyExtractor2(changedMethod);
		App.traverseDependencyGraph.send(changedMethod.getKey());
	}
	
}
