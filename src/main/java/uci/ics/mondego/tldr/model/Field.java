package uci.ics.mondego.tldr.model;

public class Field {

	
	private String fqn;
	private String type;
	private String signature;
	private String value;
	private String parameter;
	
	public Field(String fqn, String type, String signature, String value, String parameter){
		this.fqn = fqn;
		this.type = type;
		this.signature = signature;
		this.value = value;
		this.parameter = parameter;
	}
	
	public Field(String fqn, String type, String signature){
		this.fqn = fqn;
		this.type = type;
		this.signature = signature;
	}
	
	public Field(){
		
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
	public String getParameter() {
		return parameter;
	}
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}
	
}
