package uci.ics.mondego.tldr.extractor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javassist.bytecode.Opcode;
import uci.ics.mondego.tldr.model.LocalVariable;
import uci.ics.mondego.tldr.model.Method;
import uci.ics.mondego.tldr.tool.StringProcessor;

public class MethodVisitorImpl implements MethodVisitor{
	
	Method m;
	
	public MethodVisitorImpl(Method m){
		this.m = m;
	}
	
	public MethodVisitorImpl(MethodVisitor mv, String name, String className){
		m =new Method();
	}
	
	public Method getMethod(){
		return m;
	}
	
	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		// TODO Auto-generated method stub
		m.setAnnotation(arg0);
		return null;
	}

	public AnnotationVisitor visitAnnotationDefault() {
		// TODO Auto-generated method stub
		return null;
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

	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		// TODO Auto-generated method stub
		
		
		
		//System.out.println("inside visitFieldInsn");
		//System.out.println("==========================");
		
		if (name.indexOf('$') == -1) {
	        switch (opcode) {
	          case Opcodes.GETFIELD:
	          case Opcodes.GETSTATIC:
	        	 // System.out.println("READ   " + owner + "    "+name+"  "+desc);
	            break;
	          case Opcodes.PUTFIELD:
	          case Opcodes.PUTSTATIC:
	        	  //System.out.println("WRITE  " + owner + "    "+name+"   "+desc);
	            break;
	          default:
	        }
	      }		
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

	public void visitLocalVariable(String name, String desc, String signature,
			Label start, Label end, int index) {
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
		
		m.addLocalVariable(lv);
	}

	public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
		// TODO Auto-generated method stub
		
	}

	public void visitMaxs(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		// TODO Auto-generated method stub
		//System.out.println("inside visitMethodInsn");
		//System.out.println("==========================");
		//System.out.println(owner+"   "+name+"   "+desc);
		//System.out.println("==========================");
		
	}

	public void visitMultiANewArrayInsn(String arg0, int arg1) {
		// TODO Auto-generated method stub
		//System.out.println(arg0);
		
	}

	public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1, boolean arg2) {
		// TODO Auto-generated method stub
		//System.out.println(arg1+"-----"+arg2);
		return null;
	}

	public void visitTableSwitchInsn(int arg0, int arg1, Label arg2, Label[] arg3) {
		// TODO Auto-generated method stub
		
	}

	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		// TODO Auto-generated method stub
		//System.out.println(type);
		
	}

	public void visitTypeInsn(int arg0, String arg1) {
		// TODO Auto-generated method stub
		//System.out.println(arg1);
		
	}

	public void visitVarInsn(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
}
