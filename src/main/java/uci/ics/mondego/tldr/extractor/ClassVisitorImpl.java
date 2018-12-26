package uci.ics.mondego.tldr.extractor;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import uci.ics.mondego.tldr.model.Field;
import uci.ics.mondego.tldr.model.LocalVariable;
import uci.ics.mondego.tldr.model.Method;
import uci.ics.mondego.tldr.tool.StringProcessor;

public class ClassVisitorImpl implements ClassVisitor{

	private String className;
	private List<Field> fields;
	private List<Method> methods;
	private String classFqn;
	private String superClass;
	private String[] interfaces;
    private final static Logger logger = LogManager.getLogger(ClassVisitorImpl.class);

	
	public ClassVisitorImpl(String className){
		super();
		this.className = className;
		this.fields = new ArrayList<Field>();
		this.methods = new ArrayList<Method>();
	}
	
	public ClassVisitorImpl(){
		super();
		this.className = null;
		this.fields = new ArrayList<Field>();
		this.methods = new ArrayList<Method>();
	}
	
	
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		classFqn = StringProcessor.pathToFqnConverter(name);
		this.superClass = StringProcessor.pathToFqnConverter(superName);
		for(int i=0;i<interfaces.length;i++)
			interfaces[i] = StringProcessor.pathToFqnConverter(interfaces[i]);
		this.interfaces = interfaces;
	}

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		// TODO Auto-generated method stub
    	AnnotationVisitor av = new AnnotationVisitorImpl();
		return av;
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
		
		String [] word = StringProcessor.signatureProcessor(signature);
		if(word != null){
			for(String w: word){
				field.addHold(w);	
			}
		}
		fields.add(field);
		//logger.info(field.getFqn()+" parsed");
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
		String [] word = StringProcessor.signatureProcessor(parameters);
		if(word != null){		
			for(String w: word){
				LocalVariable lv = new LocalVariable();
				lv.setType(w);
				lv.setSignature(w);
				mthd.addParameter(lv);		
			}
		}
		
		String returnType = desc.substring(desc.indexOf(')') + 1);
		
		LocalVariable lv = new LocalVariable();
		lv.setType(StringProcessor.pathToFqnConverter(StringProcessor.typeProcessor(returnType)));
		lv.setSignature(signature);
		mthd.setReturnType(lv);
		
		word = StringProcessor.signatureProcessor(signature);
		if(word != null){
			for(String w: word){
				mthd.addHold(w);
			}
		}
		
		MethodVisitorImpl mv = new MethodVisitorImpl(mthd);
		mthd = mv.getMethod();
		methods.add(mthd);
		System.out.println("===================");
	    return mv;
	}
	
	public void visitOuterClass(String arg0, String arg1, String arg2) {
		
	}

	public void visitSource(String arg0, String arg1) {
		// gives which java files the class belongs to..... it could be the case that single file has multiple classes
	}
	
	public List<Field> getField(){
		return fields;
	}
}
