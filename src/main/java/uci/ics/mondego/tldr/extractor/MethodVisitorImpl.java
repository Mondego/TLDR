package uci.ics.mondego.tldr.extractor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import uci.ics.mondego.tldr.model.LocalVariable;
import uci.ics.mondego.tldr.model.Method;
import uci.ics.mondego.tldr.model.Operator;
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
		
		if (name.indexOf('$') == -1) {
	        switch (opcode) {
	          case Opcodes.GETFIELD:
	          case Opcodes.GETSTATIC:
	        	  System.out.println("READ   " + owner + "    "+name+"  "+desc+"   "+opcode);
	            break;
	          case Opcodes.PUTFIELD:
	          case Opcodes.PUTSTATIC:
	        	 System.out.println("WRITE  " + owner + "    "+name+"   "+desc+"   "+opcode);
	            break;
	          default:
	        }
	      }		
	}

	
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		System.out.println("inside local variable\n===================");
		System.out.println(name+"  "+desc+"   "+signature+"  "+index);
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
		System.out.println(arg1);
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
		System.out.println(arg0+"  "+arg1);
		
	}
	
	/****** UNNECESSARY METHODS, BUT HAVE TO KEEP TO MAINTAIN IMPLEMENTATION OF SUPERCLASS ****/
	public void visitVarInsn(int arg0, int arg1) {
		// TODO Auto-generated method stub
		System.out.println(arg1+"  "+arg1);
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
		System.out.println(arg0+"   "+arg1);

	}
	
	public void visitTableSwitchInsn(int arg0, int arg1, Label arg2, Label[] arg3) {
		// TODO Auto-generated method stub
		System.out.println(arg0+"   "+arg1);
	}
	
	// add single operand operator
	public void visitIntInsn(int opcode, int operand) {
		// TODO Auto-generated method stub
		method.addOperator(new Operator(opcode, operand));
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
		System.out.println(arg0+"   "+arg1);
	}

	//visit a zero operand operator
	public void visitInsn(int opcode) {		
		method.addOperator(new Operator(opcode));
		
		//System.out.println(opcode);
		// TODO Auto-generated method stub
		/*switch (opcode) {
        case Opcodes.ACONST_NULL: 
        	System.out.println("null");
        	break;
        case Opcodes.ICONST_M1: 
        	System.out.println("$int-1"); 
        	break;
        case Opcodes.ICONST_0: System.out.println("$int0"); break;
        case Opcodes.ICONST_1: System.out.println("$int1"); break;
        case Opcodes.ICONST_2: System.out.println("$int2"); break;
        case Opcodes.ICONST_3: System.out.println("$int3"); break;
        case Opcodes.ICONST_4: System.out.println("$int4"); break;
        case Opcodes.ICONST_5: System.out.println("$int5"); break;
        case Opcodes.LCONST_0: System.out.println("$long0"); break;
        case Opcodes.LCONST_1: System.out.println("$long1"); break;
        case Opcodes.FCONST_0: System.out.println("$float0"); break;
        case Opcodes.FCONST_1: System.out.println("$float1"); break;
        case Opcodes.FCONST_2: System.out.println("$float2"); break;
        case Opcodes.DCONST_0: System.out.println("$double0"); break;
        case Opcodes.DCONST_1: System.out.println("$double1"); break;
        case Opcodes.IALOAD:
        case Opcodes.LALOAD:
        case Opcodes.FALOAD:
        case Opcodes.DALOAD:
        case Opcodes.AALOAD:
        case Opcodes.BALOAD:
        case Opcodes.CALOAD:
        case Opcodes.SALOAD:
        case Opcodes.IASTORE:
        case Opcodes.LASTORE:
        case Opcodes.FASTORE:
        case Opcodes.DASTORE:
        case Opcodes.AASTORE:
        case Opcodes.BASTORE:
        case Opcodes.CASTORE:
        case Opcodes.SASTORE:
        	System.out.println("$arr-deref");
          break;
        case Opcodes.IADD:
        case Opcodes.LADD:
        case Opcodes.FADD:
        case Opcodes.DADD:
        	System.out.println("$add");
          break;
        case Opcodes.ISUB:
        case Opcodes.LSUB:
        case Opcodes.FSUB:
        case Opcodes.DSUB:
        	System.out.println("$sub");
          break;
        case Opcodes.IMUL:
        case Opcodes.LMUL:
        case Opcodes.FMUL:
        case Opcodes.DMUL:
        	System.out.println("$mul");
          break;
        case Opcodes.IDIV:
        case Opcodes.LDIV:
        case Opcodes.FDIV:
        case Opcodes.DDIV:
        	System.out.println("$div");
          break;
        case Opcodes.IREM:
        case Opcodes.LREM:
        case Opcodes.FREM:
        case Opcodes.DREM:
        	System.out.println("$rem");
          break;
        case Opcodes.INEG:
        case Opcodes.LNEG:
        case Opcodes.FNEG:
        case Opcodes.DNEG:
        	System.out.println("$neg");
          break;
        case Opcodes.ISHL:
        case Opcodes.LSHL:
        	System.out.println("$shl");
          break;
        case Opcodes.ISHR:
        case Opcodes.LSHR:
        	System.out.println("$shr");
          break;
        case Opcodes.IUSHR:
        case Opcodes.LUSHR:
        	System.out.println("$ushr");
          break;
        case Opcodes.IAND:
        case Opcodes.LAND:
        	System.out.println("$bw-and");
          break;
        case Opcodes.IOR:
        case Opcodes.LOR:
        	System.out.println("$bw-or");
          break;
        case Opcodes.IXOR:
        case Opcodes.LXOR:
        	System.out.println("$bw-xor");
          break;
        case Opcodes.I2L:
        case Opcodes.F2L:
        case Opcodes.D2L:
        	System.out.println("long");
          break;
        case Opcodes.I2F:
        case Opcodes.L2F:
        case Opcodes.D2F:
        	System.out.println("float");
          break;
        case Opcodes.I2D:
        case Opcodes.L2D:
        case Opcodes.F2D:
        	System.out.println("double");
          break;
        case Opcodes.L2I:
        case Opcodes.F2I:
        case Opcodes.D2I:
        	System.out.println("int");
          break;
        case Opcodes.I2B:
        	System.out.println("byte");
          break;
        case Opcodes.I2C:
        	System.out.println("char");
          break;
        case Opcodes.I2S:
        	System.out.println("short");
          break;
        case Opcodes.LCMP:
        case Opcodes.FCMPL:
        case Opcodes.FCMPG:
        case Opcodes.DCMPL:
        case Opcodes.DCMPG:
        	System.out.println("$cmp");
          break;
        case Opcodes.IRETURN:
        case Opcodes.LRETURN:
        case Opcodes.FRETURN:
        case Opcodes.DRETURN:
        case Opcodes.ARETURN:
        case Opcodes.RETURN:
        	System.out.println("$return");
          break;
        case Opcodes.ARRAYLENGTH:
        	System.out.println("$length");
          break;
        case Opcodes.ATHROW:
        	System.out.println("throw");
          break;
        case Opcodes.MONITORENTER:
        case Opcodes.MONITOREXIT:
        	System.out.println("synchronized");
          break;
        default:
          break;
      }*/
    }
	}
	

