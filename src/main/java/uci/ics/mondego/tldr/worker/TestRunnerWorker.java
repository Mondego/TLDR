package uci.ics.mondego.tldr.worker;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import uci.ics.mondego.tldr.App;
import uci.ics.mondego.tldr.exception.NotATestException;
import uci.ics.mondego.tldr.model.TestReport;

public class TestRunnerWorker extends Worker{

	private JUnitCore testCore;
	private static final Logger logger = LogManager.getLogger(TestRunnerWorker.class);
	private String claz;
	private String method;
	private String testFqn;
	
	public TestRunnerWorker(String testFqn){
		this.testFqn = testFqn;
		this.testCore = new JUnitCore();
		String intermediate = testFqn.substring(0,testFqn.indexOf('('));
		this.method = intermediate.substring(intermediate.lastIndexOf('.') +1);
		this.claz = intermediate.substring(0, intermediate.indexOf(this.method) - 1);
	}
	
	public void run() {
		try {
            this.runTest();
        } 
		catch (NoSuchElementException e) {
            e.printStackTrace();
        } 
		catch (IllegalArgumentException e) {
            e.printStackTrace();
        } 
		catch (SecurityException e) {
            e.printStackTrace();
        }		
	}
	/*
	 * Test runner class.
	 * Steps:
	 * 1. discards inner class, constructors, static initializers
	 * 2. check is the class is parameterized. If so, get the children/parameter 
	 *    and run the method with each parameter value
	 * 3. else just run the test case
	 * 4. prepare a report class for the testcase
	 * 5. write it in App concurrent hashMap
	 */
	public void runTest(){
		Result result = null;		
		try {
			if(claz.contains("$") || method.contains("<init>") 
			  || method.contains("<clinit>") || testFqn.contains(".setUp(")
			  || testFqn.contains(".close(") || method.contains("$"))			  	
					return ;
			
			Description description = Request.aClass(Class
		    		.forName(claz)).getRunner()
		    		.getDescription();
			
			boolean successful = true;
		    String annotations = description.getAnnotations().toString();		    
		    if(annotations.contains("org.junit.runners.Parameterized")){
		    	int parametersCount = description.getChildren().size();
			    ArrayList<Description> children = description.getChildren();
			    for (int i = 0; i < parametersCount; i++) {		
			    	result = (new JUnitCore()).run(Request.method(Class.forName(claz), method+children.get(i)));
			    	App.completeTestCaseSet.put(testFqn, App.completeTestCaseSet.get(testFqn) + 1);
			    	successful &= result.wasSuccessful();
			    } 
		    }
		    else{
				Request request = Request.method(Class.forName(claz),method);
				result = testCore.run(request);
				successful = result.wasSuccessful();
		    }
			TestReport tr = new TestReport();
			tr.setTestFqn(claz+"."+method);
			tr.setRuntime(result.getRunTime());
			tr.setSuccessful(successful);
			// cause we had put 1 already...for each paramter we do ++... therefore we put 1 extra.
			tr.setRun(App.completeTestCaseSet.get(testFqn) == 1 ? App.completeTestCaseSet.get(testFqn) 
					: App.completeTestCaseSet.get(testFqn) - 1);
			
			List<Failure> failures = result.getFailures();
			if(failures != null){
				for(int i = 0;i<failures.size();i++){
					if(failures.get(i).getMessage() != null){
						if(failures.get(i).getMessage().contains("No tests found matching Method")){
							throw new NotATestException(claz+"."+method);
						}
						if(failures.get(i).getMessage().contains("No runnable methods")){
							throw new NotATestException(claz+"."+method);
						}
						
						if(failures.get(i).getMessage().contains("Test class can only have one constructor")){
							throw new NotATestException(claz+"."+method);
						}
						
						tr.addFailureMessage(failures.get(i).getMessage());
					}
				}
			}
			
			App.allTestReport.put(claz+"."+method, tr);		    
		} 
		
		catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
		catch(NoClassDefFoundError e){	
			System.out.println(e.getMessage());
		
		} catch (NotATestException e) {
			System.out.println(e.getMessage());
		} 		
	}
}
