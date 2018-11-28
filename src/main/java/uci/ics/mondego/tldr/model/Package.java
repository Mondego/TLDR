package uci.ics.mondego.tldr.model;

import java.util.ArrayList;
import java.util.List;

public class Package implements Entities{

	String name;
	List<Package> imports;
	
	public Package(){
		imports = new ArrayList<Package>();
	}
	
	public Package(String name){
		this.name = name;
		imports = new ArrayList<Package>();
	}
	
	public String calculateCheckSum() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return name;
	}
	
	public void addImport(Package imp){
		imports.add(imp);
	}

	public List<Package> getImports() {
		return imports;
	}
	
}
