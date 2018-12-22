package uci.ics.mondego.tldr.extractor;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;

import uci.ics.mondego.tldr.model.Field;
import uci.ics.mondego.tldr.model.LocalVariable;
import uci.ics.mondego.tldr.model.Method;
import uci.ics.mondego.tldr.tool.StringProcessor;

public class ClassVisitorImpl implements ClassVisitor{

	//MethodVisitor mv = new MethodVisitorImpl();
	String className;
	List<Field> fields;
	String classFqn;
	
	
	public ClassVisitorImpl(String className){
		super();
		this.className = className;
		this.fields = new ArrayList<Field>();
	}
	
	public ClassVisitorImpl(){
		super();
		this.className = null;
		this.fields = new ArrayList<Field>();
	}
	
	
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		classFqn = StringProcessor.pathToFqnConverter(name);	
	}

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		// TODO Auto-generated method stub
    	//System.out.println(desc);
    	
    	
		return null;
	}

	public void visitAttribute(Attribute arg0) {
		// TODO Auto-generated method stub
		
	}

	public void visitEnd() {
		// TODO Auto-generated method stub
		
	}

	public FieldVisitor visitField(int access, String name, String desc, String signature, Object arg4) {
		// TODO Auto-generated method stub
		//System.out.println("FIELD: " + name +" ------- "+ desc + " ---- "+signature);
		
		Field field = new Field();
		
		field.setName(name);
		field.setFqn(classFqn+'.'+name);
		field.setType(StringProcessor.pathToFqnConverter(StringProcessor.typeProcessor(desc)));
		field.setSignature(signature);
		
		if(signature != null){
			//signature = signature.replace('*', '\0');
			String [] word = signature.split(";|<|>|\\*");
			for(String w: word){
				if(w.length() != 0){
					field.addHold(StringProcessor.pathToFqnConverter(w).substring(1));
				}		
			}
		}
		
		fields.add(field);
		return null;
	}

	public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		// TODO Auto-generated method stub
		
		System.out.println("METHOD: " + name +"-------"+ desc+ "--------"+ signature);
		Method mthd = new Method();
		mthd.setName(name);
		mthd.setFqn(classFqn+'.'+name);
		
		
		String parameters = desc.substring(desc.indexOf('(') + 1, desc.indexOf(')'));
		
		if(parameters != null){
			String [] word = parameters.split(";|<|>|\\*");
			for(String w: word){
				if(w.length() != 0){
					LocalVariable lv = new LocalVariable();
					lv.setType(StringProcessor.pathToFqnConverter(w).substring(1));
					//System.out.println(lv.getType());
					lv.setSignature(w);
					mthd.addParameter(lv);
				}		
			}
		}
		
		String returnType = desc.substring(desc.indexOf(')') + 1);
		
		LocalVariable lv = new LocalVariable();
		lv.setType(StringProcessor.pathToFqnConverter(StringProcessor.typeProcessor(returnType)));
		lv.setSignature(signature);
		mthd.setReturnType(lv);
		
		if(signature != null){
			//signature = signature.replace('*', '\0');
			String [] word = signature.split(";|<|>|\\*");
			for(String w: word){
				if(w.length() != 0){
					mthd.addHold(StringProcessor.pathToFqnConverter(w).substring(1));
				}		
			}
		}
		
		MethodVisitorImpl mv = new MethodVisitorImpl(mthd);
		
		mthd = mv.getMethod();
		
		System.out.println("===================");
	    return mv;
		//return null;
	}

	public void visitOuterClass(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	public void visitSource(String arg0, String arg1) {
		// TODO Auto-generated method stub
		//System.out.println("inside visitSource : "+arg0+"    "+ arg1);	
	}
	
	public List<Field> getField(){
		return fields;
	}
}
