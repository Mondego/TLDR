package uci.ics.mondego.tldr.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

public class ClassFile{

	private final String classFqn;
	private final JavaClass classCode;
	private List<String> superClasses;
	private List<String> subClasses;
	private List<Method> methods;
	private List<Field> fields;
		
	public ClassFile(String name, JavaClass code) {
		this.classFqn = name;
		this.classCode = code;
		this.superClasses = new ArrayList<String>();
		this.subClasses = new ArrayList<String>();	
		this.methods = new ArrayList<Method>();
		this.fields = new ArrayList<Field>();
	}
	
	public void addTestCase(Method m){
		this.methods.add(m);
	}
	public void addSuperClass(String sup){
		this.superClasses.add(sup);
	}
	public void addSubclass(String sub){
		this.subClasses.add(sub);
	}
	public String getName(){
		return this.classFqn;
	}
	public List<Method> getMethods(){
		return methods;
	}
	public List<Field> getFields(){
		return fields;
	}
	public List<String> getAllSuperClasses(){
		return superClasses;
	}	
	public List<String> getAllSubClasses(){
		return subClasses;
	}	
}
