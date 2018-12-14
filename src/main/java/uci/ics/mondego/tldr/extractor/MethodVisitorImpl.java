package uci.ics.mondego.tldr.extractor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javassist.bytecode.Opcode;

public class MethodVisitorImpl implements MethodVisitor{
	
	public MethodVisitorImpl(){
		
	}
	public MethodVisitorImpl(MethodVisitor mv, String name, String className){
		
	}

	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		// TODO Auto-generated method stub
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
		
		
		
		System.out.println("inside visitFieldInsn");
		System.out.println("==========================");
		
		if (name.indexOf('$') == -1) {
	        switch (opcode) {
	          case Opcodes.GETFIELD:
	          case Opcodes.GETSTATIC:
	            //relationWriter.writeRelation(Relation.READS, fqnStack.getFqn(), convertNameToFqn(owner) + "." + name, location);
	            System.out.println("READ   " + owner + "    "+name+"  "+desc);
	            break;
	          case Opcodes.PUTFIELD:
	          case Opcodes.PUTSTATIC:
	            //relationWriter.writeRelation(Relation.WRITES, fqnStack.getFqn(), convertNameToFqn(owner) + "." + name, location);
	        	  System.out.println("WRITE  " + owner + "    "+name+"   "+desc);
	            break;
	            
	          default:
	            //logger.severe("Unknown field instruction: " + opcode);
	        }
	      }
		System.out.println("==========================");
		
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
		// TODO Auto-generated method stub
		System.out.println("inside visitlocal");
		System.out.println("==========================");
		System.out.println(name+"    "+desc+"    "+signature);
		System.out.println("==========================");
	}

	public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
		// TODO Auto-generated method stub
		
	}

	public void visitMaxs(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		// TODO Auto-generated method stub
		System.out.println("inside visitMethodInsn");
		System.out.println("==========================");
		System.out.println(owner+"   "+name+"   "+desc);
		System.out.println("==========================");
		
	}

	public void visitMultiANewArrayInsn(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1, boolean arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	public void visitTableSwitchInsn(int arg0, int arg1, Label arg2, Label[] arg3) {
		// TODO Auto-generated method stub
		
	}

	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		// TODO Auto-generated method stub
		
	}

	public void visitTypeInsn(int arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	public void visitVarInsn(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
	

}
