package uci.ics.mondego.tldr.extractor;


import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.TraceClassVisitor;


public class ByteCodeParser {
	
	
	public ByteCodeParser(){
		try {
			
			/*visit 
			visitSource
			visitOuterClass
		    visitAnnotation
		    visitAttribute
			visitInnerClass
			visitField
			visitMethod
			visitEnd*/
			
			
			ClassReader cr = new ClassReader("uci.ics.mondego.tldr.App");
			ClassVisitorImpl cv = new ClassVisitorImpl("uci.ics.mondego.tldr.App");
			cr.accept(cv, 0);
			//cv.visitMethod(access, name, desc, signature, exceptions)	;
//			MethodVisitorImpl mv = new MethodVisitorImpl();
//			mv.visitCode();
//		    Label l0 = new Label();
//		    mv.visitLabel(l0);
		    
		    

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private String convertNameToFqn(String name) {
	    return null;
	  }
	
	private String convertBaseType(char type) {
	    switch (type) {
	      case 'B':
	        return "byte";
	      case 'C':
	        return "char";
	      case 'D':
	        return "double";
	      case 'F':
	        return "float";
	      case 'I':
	        return "int";
	      case 'J':
	        return "long";
	      case 'S':
	        return "short";
	      case 'Z':
	        return "boolean";
	      case 'V':
	        return "void";
	      default:
	       // logger.log(Level.SEVERE, "Unexpected type name: " + type);
	        return "" + type;
	    }
	  }
	

}
