package uci.ics.mondego.tldr.model;

import java.util.ArrayList;
import java.util.List;

public class Field {

	
	private String name;
	private String fqn;
	private String type;
	private String signature;
	private String value;
	private List<String> holds;
	
	public Field(String name,String fqn, String type, String signature, String value, String parameter){
		this.fqn = fqn;
		this.type = type;
		this.name = name;
		this.signature = signature;
		this.value = value;
		this.holds = new ArrayList<String>();
	}
	
	public Field(String name, String fqn, String type, String signature){
		this.name = name;
		this.fqn = fqn;
		this.type = type;
		this.signature = signature;
		this.holds = new ArrayList<String>();
	}
	
	public Field(){
		this.holds = new ArrayList<String>();
	}
	
	
	public void addHold(String h){
		holds.add(h);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getFqn() {
		return fqn;
	}
	public void setFqn(String fqn) {
		this.fqn = fqn;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("\nname" + this.name+"\n");
		sb.append("\nfwn" + this.fqn+"\n");
		sb.append("\ntype" + this.type+"\n");
		sb.append("\nholds" + this.holds+"\n");
		
		return sb.toString();
	}
	
	
//	public String getParameter() {
//		return parameter;
//	}
//	public void setParameter(String parameter) {
//		this.parameter = parameter;
//	}
	
}
