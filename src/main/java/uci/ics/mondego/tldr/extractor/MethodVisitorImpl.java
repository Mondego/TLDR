package uci.ics.mondego.tldr.extractor;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import uci.ics.mondego.tldr.model.LocalVariable;
import uci.ics.mondego.tldr.model.Method;
import uci.ics.mondego.tldr.model.Operator;
import uci.ics.mondego.tldr.tool.StringProcessor;

public class MethodVisitorImpl implements MethodVisitor{
	
	Method method;
	List<String> uses = new ArrayList<String>();
	
	public MethodVisitorImpl(Method method){
		this.method = method;
	}
	
	public MethodVisitorImpl(MethodVisitor mv, String name, String className){
		
		method = new Method();
	}
	
	public Method getMethod(){
		return method;
	}
	
	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		// TODO Auto-generated method stub
		method.setAnnotation(arg0);
		AnnotationVisitor av = new AnnotationVisitorImpl();
		return av;
	}

	public AnnotationVisitor visitAnnotationDefault() {
		// TODO Auto-generated method stub
		AnnotationVisitor av = new AnnotationVisitorImpl();
		return av;
	}

	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		// TODO Auto-generated method stub
		
		if (name.indexOf('$') == -1) {
			String fieldFqn = StringProcessor.pathToFqnConverter(owner) + "."+name;
			
			uses.add(fieldFqn);
			method.addUses(fieldFqn);
			
			method.addOperator(new Operator(opcode, fieldFqn.hashCode()));
	        switch (opcode) {
	          case Opcodes.GETFIELD:
	          case Opcodes.GETSTATIC:
	        	  //System.out.println("READ   " + owner + "    "+name+"  "+desc+"   "+opcode);
	            break;
	          case Opcodes.PUTFIELD:
	          case Opcodes.PUTSTATIC:
	        	 //System.out.println("WRITE  " + owner + "    "+name+"   "+desc+"   "+opcode);
	            break;
	          default:
	        }
	      }		
	}

	
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		//System.out.println("inside local variable\n===================");
		//System.out.println(name+"  "+desc+"   "+signature+"  "+index);
		LocalVariable lv = new LocalVariable();
		lv.setName(name);
		lv.setType(StringProcessor.pathToFqnConverter(StringProcessor.typeProcessor(desc)));
		if(signature != null){
			String [] word = signature.split(";|<|>|\\*");
			for(String w: word){
				if(w.length() != 0){
					lv.addHold(StringProcessor.pathToFqnConverter(StringProcessor.typeProcessor(desc)));
				}		
			}
		}	
		method.addLocalVariable(lv);
		//System.out.println(method.toString());
	}

	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		// all function calls
		//System.out.println(StringProcessor.pathToFqnConverter(owner) + "."+name);
		uses.add(StringProcessor.pathToFqnConverter(owner) + "."+name);
		method.addUses(StringProcessor.pathToFqnConverter(owner) + "."+name);		
	}
	
	
    /********CONFUSED******/
	public void visitMultiANewArrayInsn(String arg0, int arg1) {
		// TODO Auto-generated method stub		
		//System.out.println(arg1);
	}

    /********CONFUSED******/
	public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1, boolean arg2) {
		// TODO Auto-generated method stub
		//System.out.println(arg1+"-----"+arg2);
		return null;
	}

    /********CONFUSED******/
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		// TODO Auto-generated method stub
	}

    /********CONFUSED******/
	public void visitTypeInsn(int arg0, String arg1) {
		// TODO Auto-generated method stub
		method.addOperator(new Operator(arg0, arg1.hashCode()));
		method.addUses(arg1+".<init>");
	}
	
	public List<String> getUses(){
		return uses;
	}
	
	public void visitVarInsn(int arg0, int arg1) {
		// TODO Auto-generated method stub
		method.addOperator(new Operator(arg0, arg1));
	}
	
	public void visitAttribute(Attribute arg0) {
		// TODO Auto-generated method stub
	}

	public void visitCode() {
		// TODO Auto-generated method stub
	}

	public void visitEnd() {
		// TODO Auto-generated method stub
		//System.out.println(method.toString());
		if(method.getName().contains("equals"))
			System.out.println(method.hashCode());
	}
	
	public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
		// TODO Auto-generated method stub
	}

	public void visitMaxs(int arg0, int arg1) {
		// TODO Auto-generated method stub
		//System.out.println(arg0+"   "+arg1);

	}
	
	public void visitTableSwitchInsn(int arg0, int arg1, Label arg2, Label[] arg3) {
		// TODO Auto-generated method stub
		//System.out.println(arg0+"   "+arg1);
	}
	
	// add single operand operator
	public void visitIntInsn(int opcode, int operand) {
		// TODO Auto-generated method stub
		method.addOperator(new Operator(opcode, operand));
	}

	public void visitJumpInsn(int opcode, Label label) {
		// TODO Auto-generated method stub
		method.addOperator(new Operator(opcode, label.toString().hashCode()));
		
	}

	public void visitLabel(Label arg0) {
		// TODO Auto-generated method stub
	}

	public void visitLdcInsn(Object arg0) {
		// TODO Auto-generated method stub
	}

	public void visitLineNumber(int arg0, Label arg1) {
		// TODO Auto-generated method stub
	}
	
	public void visitFrame(int arg0, int arg1, Object[] arg2, int arg3, Object[] arg4) {
		// TODO Auto-generated method stub
	}

	public void visitIincInsn(int arg0, int arg1) {
		// TODO Auto-generated method stub
		method.addOperator(new Operator(Opcodes.IADD, arg0, arg1));
		method.addOperator(new Operator(Opcodes.ISTORE, arg0));		
	}

	//visit a zero operand operator
	public void visitInsn(int opcode) {		
		method.addOperator(new Operator(opcode));
		
		//System.out.println(opcode);
		// TODO Auto-generated method stub
    }
	}
	

