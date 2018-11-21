package uci.ics.mondego.tldr.model;

import java.util.ArrayList;
import java.util.List;

public class Package implements Entity{

	String name;
	List<Package> imports;
	
	public Package(){
		imports = new ArrayList<Package>();
	}
	
	public Package(String name){
		this.name = name;
	}
	
	public String calculateCheckSum() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return name;
	}

	public List<Package> getImports() {
		return imports;
	}
	
}
