package uci.ics.mondego.tldr.maven;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import uci.ics.mondego.tldr.asm.ClassReader;
import uci.ics.mondego.tldr.asm.ClassVisitor;
import uci.ics.mondego.tldr.asm.ClassWriter;
import uci.ics.mondego.tldr.asm.MethodVisitor;
import uci.ics.mondego.tldr.asm.Opcodes;
import uci.ics.mondego.tldr.tool.Constants;

public final class MavenCFT implements ClassFileTransformer {

    private static class MavenMethodVisitor extends MethodVisitor {
        private final String mavenMethodName;
        private final String mavenMethodDesc;

        private final String mavenInterceptorName;

        public MavenMethodVisitor(
        		String interceptorName, 
        		String methodName, 
        		String methodDesc, 
        		MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
            this.mavenInterceptorName = interceptorName;
            this.mavenMethodName = methodName;
            this.mavenMethodDesc = methodDesc;
        }

        public void visitCode() {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
            		Opcodes.INVOKESTATIC, 
            		mavenInterceptorName, 
            		mavenMethodName,
                    mavenMethodDesc.replace("(", "(Ljava/lang/Object;"), 
                    false);
            
            mv.visitCode();
        }
    }

    private static class MavenClassVisitor extends ClassVisitor {
        @SuppressWarnings("unused")
		private final String mavenClassName;
        private final String mavenInterceptorName;

        public MavenClassVisitor(String className, String interceptorName, ClassVisitor cv) {
            super(Opcodes.ASM5, cv);
            this.mavenClassName = className;
            this.mavenInterceptorName = interceptorName;
        }

        public MethodVisitor visitMethod (
        		int access, 
        		String name, 
        		String desc, 
        		String signature, 
        		String[] exceptions) {
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
            if (name.equals(Constants.EXECUTE_MNAME) && desc.equals(Constants.EXECUTE_MDESC)) {
                mv = new MavenMethodVisitor(mavenInterceptorName, name, desc, mv);
            }
            return mv;
        }
    }

    public byte[] transform(
    		ClassLoader loader, 
    		String className, 
    		Class<?> classBeingRedefined,
    		ProtectionDomain protectionDomain, 
    		byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.equals(Constants.ABSTRACT_SUREFIRE_MOJO_VM) 
        		|| className.equals(Constants.SUREFIRE_PLUGIN_VM)) {
            return addInterceptor(className, classfileBuffer, Constants.SUREFIRE_INTERCEPTOR_CLASS_VM);
        } 
        else {
            return null;
        }
    }

    private byte[] addInterceptor(
    		String className, 
    		byte[] classfileBuffer, 
    		String interceptorName) {
        ClassReader classReader = new ClassReader(classfileBuffer);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = 
        		new MavenClassVisitor(className, interceptorName, classWriter);
        classReader.accept(visitor, 0);
        return classWriter.toByteArray();
    }
}

