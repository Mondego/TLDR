package uci.ics.mondego.tldr.model;

import java.util.ArrayList;
import java.util.List;

public class TestReport {
	
	private long runtime;
	private boolean successful;
	private String testFqn;
	private int run = 0;
	private List<String> failureMessage = new ArrayList<String>();
	
	public long getRuntime() {
		return runtime;
	}
	
	public void addFailureMessage(String msg){
		this.failureMessage.add(msg);
	}
	public void setRun(int r){
		run = r;
	}
	public int getRun(){
		return run;
	}
	
	public String getFailureMessage(){
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<failureMessage.size();i++)
			sb.append(failureMessage.get(i)+",");
		return sb.toString();
	}
	
	public void setRuntime(long runtime) {
		this.runtime = runtime;
	}
	
	public void setTestFqn(String test) {
		this.testFqn = test;
	}
	
	public String getTestFqn() {
		return this.testFqn;
	}
	
	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}
	
}
