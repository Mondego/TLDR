package uci.ics.mondego.tldr.model;

import java.util.ArrayList;
import java.util.List;

public class Method {
	
	private String name;
	private String fqn;
	private LocalVariable returnType;
	private String signature;
	private List<String> uses;
	private List<LocalVariable> parameter;
	private String body;
	private String annotation;
	private List<LocalVariable> localVariables;
	
	public Method(String name,String fqn, LocalVariable type, String signature, String value, String parameter){
		this.fqn = fqn;
		this.returnType = type;
		this.name = name;
		this.signature = signature;
		this.uses = new ArrayList<String>();
		this.parameter = new ArrayList<LocalVariable>();
		this.localVariables = new ArrayList<LocalVariable>();
	}
	
	public Method(String name, String fqn, LocalVariable type, String signature){
		this.name = name;
		this.fqn = fqn;
		this.returnType = type;
		this.signature = signature;
		this.uses = new ArrayList<String>();
		this.parameter = new ArrayList<LocalVariable>();
		this.localVariables = new ArrayList<LocalVariable>();

	}
	
	public Method(){
		this.uses = new ArrayList<String>();
		this.parameter = new ArrayList<LocalVariable>();
		this.localVariables = new ArrayList<LocalVariable>();
	}
	
	
	public void addHold(String h){
		uses.add(h);
	}
	
	public String getName() {
		return name;
	}
	
	public void addLocalVariable(LocalVariable lv){
		localVariables.add(lv);
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
	public LocalVariable getReturnType() {
		return returnType;
	}
	public void setType(LocalVariable type) {
		this.returnType = type;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("\nname" + this.name+"\n");
		sb.append("\nfqn" + this.fqn+"\n");
		sb.append("\nreturn type" + this.returnType+"\n");
		sb.append("\nholds" + this.uses+"\n");
		
		return sb.toString();
	}
	
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void setReturnType(LocalVariable returnType) {
		this.returnType = returnType;
	}
	
	public List<LocalVariable> getParameter() {
		return parameter;
	}

	public void setParameter(List<LocalVariable> parameter) {
		this.parameter = parameter;
	}
	
	public void addParameter(LocalVariable lv){
		parameter.add(lv);
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}
	
}
