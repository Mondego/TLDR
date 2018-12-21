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
import uci.ics.mondego.tldr.tool.StringProcessor;

public class ClassVisitorImpl implements ClassVisitor{

	MethodVisitor mv = new MethodVisitorImpl();
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
					//System.out.println(w+"  "+w.length());
					field.addHold(w);
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
		MethodVisitor mv = new MethodVisitorImpl();

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
