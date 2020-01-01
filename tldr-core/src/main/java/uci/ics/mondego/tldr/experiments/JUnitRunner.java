package uci.ics.mondego.tldr.experiments;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class JUnitRunner {

	private ConcurrentHashMap<String, String> testFqn;
	private Set<String> allTestsRun;
	private double totalTestTime;
	private Map<String, Class<?>> packageToClassMap;
	private JUnitCore testCore;

	public JUnitRunner(ConcurrentHashMap<String, Boolean> selectedTests) {
		this.testCore = new JUnitCore();
		for(Map.Entry<String, Boolean> entry: selectedTests.entrySet()){
			testFqn.put(entry.getKey().substring(0 , entry.getKey().indexOf('#')), 
					entry.getKey().substring(entry.getKey().indexOf('#') + 1));
		}
		this.allTestsRun = new HashSet<String>();
		this.packageToClassMap = new HashMap<String, Class<?>>();
		this.totalTestTime = 0;
	}
	
	public JUnitRunner() {

		this.testCore = new JUnitCore();
		this.allTestsRun = new HashSet<String>();
		this.packageToClassMap = new HashMap<String, Class<?>>();
		this.totalTestTime = 0;
	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JUnitRunner runner = new JUnitRunner();
		//runner.classLoad("/Users/demigorgan/TLDR_EXP/projects/commons-io");		
			
		runner.runTest("org.apache.commons.io.input.ReversedLinesFileReaderTestParamBlockSize"
				, "testIsoFileDefaults");	 	
		runner.runTest("org.apache.commons.io.output.FileWriterWithEncodingTest", "sameEncoding_string_string_constructor");
	}

	/*public void printTestRunSummary(Result result) {
		
	}*/
	
	public Result runTest(String claz, String method, boolean hasParamater){
	    Result result = null;
	    try {
		    int parametersCount = Request.aClass(Class.forName(claz)).getRunner().getDescription().getChildren().size();
		    for (int i = 0; i < parametersCount; i++) {			
		    	result = (new JUnitCore()).run(Request.method(Class.forName(claz), method+"[BlockSize=8]"));
		    	//result = (new JUnitCore()).run(Request.method(Class.forName(claz), method+"[1: BlockSize(1)=1]"));
		    } 
			System.out.println("Result " + result.wasSuccessful());		
		}
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public Result runTest(String claz, String method){
		Result result = null;
		try {
			Description description = Request.aClass(Class
		    		.forName(claz)).getRunner()
		    		.getDescription();
			
		    String annotations = description.getAnnotations().toString();		    
		    if(annotations.contains("org.junit.runners.Parameterized")){
		    	int parametersCount = description.getChildren().size();
			    ArrayList<Description> children = description.getChildren();
		    	for (int i = 0; i < parametersCount; i++) {		
			    	result = (new JUnitCore()).run(Request.method(Class.forName(claz), method+children.get(i)));
			    } 
		    }
		    else{
				Request request = Request.method(Class.forName(claz),method);
				result = testCore.run(request);
		    }
		    		
			System.out.println(result.getRunTime());
			System.out.println(result.wasSuccessful());
			System.out.println(result.getFailures());			
			System.out.println("======");
			
		} 
		catch (ClassNotFoundException e) {
			System.out.println(claz+"."+method+"  can't be run");
		}
		catch(NoClassDefFoundError e){
			
		}
		return result;	
	}
	
	@SuppressWarnings("unchecked")
	private void classLoad(String dir){
		
		File directory = new File(dir);
		File[] fList = directory.listFiles();	

		if(fList != null){
			for (File file : fList) {    	        	
				if (!file.isFile() ) {
				     classLoad(file.getAbsolutePath());       	         
				} 
			}
		}
		
		try{
			boolean containsClassFile = false; 
			if(fList != null){
				for (File file : fList) { 
					String fileAbsolutePath = file.getAbsolutePath();	
					if(fileAbsolutePath.endsWith(".class")){
						containsClassFile = true;
						break;
					}
				}
			}
			
			if(containsClassFile){
				System.out.println(dir);
				URL classUrl = new URL("file://"+dir);
				URL[] classUrls = { classUrl };
				URLClassLoader ucl = new URLClassLoader(classUrls);										
				try{
					Class c = ucl.loadClass("LogParser");
					System.out.println(c.getName() );
					packageToClassMap.put("redis.clients.util.Slowlog", c);
				}			
				catch (ClassNotFoundException e) {
					System.out.println("class not found");
				} 				
				finally{
					ucl.close();
				}
			}
		}
		catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
