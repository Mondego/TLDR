package uci.ics.mondego.tldr.model;

import java.util.ArrayList;
import java.util.List;

public class Method {
	
	
	private String name;
	private String fqn;
	private LocalVariable returnType;
	private String signature;
	private List<String> uses = new ArrayList<String>();
	private List<LocalVariable> parameter = new ArrayList<LocalVariable>();
	private String body;
	private String annotation;
	private List<LocalVariable> localVariables = new ArrayList<LocalVariable>();
	private List<Operator> operators = new ArrayList<Operator>();
	
	public Method(String name,String fqn, LocalVariable type, String signature, String value, String parameter){
		this.fqn = fqn;
		this.returnType = type;
		this.name = name;
		this.signature = signature;
	}
	
	public Method(String name, String fqn, LocalVariable type, String signature){
		this.name = name;
		this.fqn = fqn;
		this.returnType = type;
		this.signature = signature;
	}
	
	public Method(){
		
	}
	
	
	public void addUses(String h){
		if(h.length() > 0 || h != null)
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
		sb.append("Name : " + this.name+"\n");
		sb.append("Fqn : " + this.fqn+"\n");
		sb.append("Return type : " + this.returnType.getType()+"\n");
		sb.append("Signature : "+this.signature+"\n");
		sb.append("Operators: \n");
		for(int i=0;i<operators.size();i++)
			sb.append(operators.get(i).getOperator()+" "+operators.get(i).getOperand1()+" "
					+operators.get(i).getOperand2()+"\n");
		
		sb.append("Parameters: \n");
		for(int i=0;i<parameter.size();i++)
			sb.append(parameter.get(i).getType()+" "+parameter.get(i).getName()+"\n");
		
		sb.append("Local Variable: \n");
		for(int i=0;i<localVariables.size();i++)
			sb.append(localVariables.get(i).getType()+" "+localVariables.get(i).getName()+"\n");
		
		sb.append("Uses: \n");
		for(int i=0;i<uses.size();i++)
			sb.append(uses.get(i)+"\n");
		
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
	
	public void addOperator(Operator op){
		operators.add(op);
	}
	
	public List<Operator> getOperators(){
		return operators;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}
	
	
	@Override
	public int hashCode() {
		StringBuilder method = new StringBuilder();
		method.append(name);
		method.append(fqn);
		method.append(returnType.hashCode());
		method.append(signature);
		method.append(annotation);
		for(int i=0;i<uses.size();i++)
			method.append(uses.get(i));
		for(int i=0;i<parameter.size();i++)
			method.append(parameter.get(i).hashCode());
		for(int i=0;i<localVariables.size();i++)
			method.append(localVariables.get(i).hashCode());
		for(int i=0;i<operators.size();i++)
			method.append(operators.get(i).hashCode());
		
		return method.toString().hashCode();
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub		
		return super.clone();
	}

	
}
