package uci.ics.mondego.tldr.model;

import java.util.HashMap;
import java.util.Map;

public class Report {
	
	private Map<String, Boolean> newOrChangedEntities = new HashMap<String, Boolean>();
	private Map<String, Boolean> entitiesToTest = new HashMap<>() ;
	private Map<String, Integer> testsToRun = new HashMap<>();
	private double selectionTimeInSecond;

	public Report(
			Map<String, Boolean> newOrChangedEntities, 
			Map<String, Boolean> entitiesToTest, 
			Map<String, Integer> testsToRun, 
			double selectionTimeInSecond) {
		this.newOrChangedEntities = newOrChangedEntities;
		this.entitiesToTest = entitiesToTest;
		this.testsToRun = testsToRun;
		this.selectionTimeInSecond = selectionTimeInSecond;
		
	}

	public Map<String, Boolean> getNewOrChangedEntities() {
		return newOrChangedEntities;
	}

	public void setNewOrChangedEntities(Map<String, Boolean> newOrChangedEntities) {
		this.newOrChangedEntities = newOrChangedEntities;
	}

	public Map<String, Boolean> getEntitiesToTest() {
		return entitiesToTest;
	}

	public void setEntitiesToTest(Map<String, Boolean> entitiesToTest) {
		this.entitiesToTest = entitiesToTest;
	}

	public Map<String, Integer> getTestsToRun() {
		return testsToRun;
	}

	public void setTestsToRun(Map<String, Integer> testsToRun) {
		this.testsToRun = testsToRun;
	}

	public double getSelectionTimeInSecond() {
		return selectionTimeInSecond;
	}

	public void setSelectionTimeInSecond(double selectionTimeInSecond) {
		this.selectionTimeInSecond = selectionTimeInSecond;
	}
	
}
