package uci.ics.mondego.tldr.extractor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import uci.ics.mondego.tldr.model.LocalVariable;
import uci.ics.mondego.tldr.model.Method;
import uci.ics.mondego.tldr.tool.StringProcessor;

public class MethodVisitorImpl implements MethodVisitor{
	
	Method method;
	
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
		
		
		
		//System.out.println("inside visitFieldInsn");
		//System.out.println("==========================");
		
		if (name.indexOf('$') == -1) {
	        switch (opcode) {
	          case Opcodes.GETFIELD:
	          case Opcodes.GETSTATIC:
	        	  //System.out.println("READ   " + owner + "    "+name+"  "+desc);
	            break;
	          case Opcodes.PUTFIELD:
	          case Opcodes.PUTSTATIC:
	        	 // System.out.println("WRITE  " + owner + "    "+name+"   "+desc);
	            break;
	          default:
	        }
	      }		
	}

	
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
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
	}

	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		// all function calls
		System.out.println(StringProcessor.pathToFqnConverter(owner) + "."+name);
		method.addHold(StringProcessor.pathToFqnConverter(owner) + "."+name);		
	}
	
	
    /********CONFUSED******/
	public void visitMultiANewArrayInsn(String arg0, int arg1) {
		// TODO Auto-generated method stub		
	}

    /********CONFUSED******/
	public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1, boolean arg2) {
		// TODO Auto-generated method stub
		System.out.println(arg1+"-----"+arg2);
		return null;
	}

    /********CONFUSED******/
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		// TODO Auto-generated method stub
		//System.out.println(handler.toString());
	}

    /********CONFUSED******/
	public void visitTypeInsn(int arg0, String arg1) {
		// TODO Auto-generated method stub
		//System.out.println(arg1);
		
	}
	
	/****** UNNECESSARY METHODS, BUT HAVE TO KEEP TO MAINTAIN IMPLEMENTATION OF SUPERCLASS ****/
	public void visitVarInsn(int arg0, int arg1) {
		// TODO Auto-generated method stub
	}
	
	public void visitAttribute(Attribute arg0) {
		// TODO Auto-generated method stub
	}

	public void visitCode() {
		// TODO Auto-generated method stub
	}

	public void visitEnd() {
		// TODO Auto-generated method stub
	}
	
	public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
		// TODO Auto-generated method stub
	}

	public void visitMaxs(int arg0, int arg1) {
		// TODO Auto-generated method stub
	}
	
	public void visitTableSwitchInsn(int arg0, int arg1, Label arg2, Label[] arg3) {
		// TODO Auto-generated method stub	
	}
	
	public void visitIntInsn(int arg0, int arg1) {
		// TODO Auto-generated method stub
	}

	public void visitJumpInsn(int arg0, Label arg1) {
		// TODO Auto-generated method stub
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
	}

	public void visitInsn(int arg0) {
		// TODO Auto-generated method stub
	}
	
}
