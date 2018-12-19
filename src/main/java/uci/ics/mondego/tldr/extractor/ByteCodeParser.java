package uci.ics.mondego.tldr.extractor;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import javax.naming.spi.DirectoryManager;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.TraceClassVisitor;

import uci.ics.mondego.tldr.model.SourceFile;


public class ByteCodeParser {
	
	
	public ByteCodeParser(SourceFile classFile) throws IOException, ArrayIndexOutOfBoundsException{
		ClassReader cr = new ClassReader(this.getFileAsByteArray(new File(classFile.getName())));
		
		ClassVisitorImpl cv = new ClassVisitorImpl();
		
		cr.accept(cv, 0);
	}
	
	public ByteCodeParser(String name) throws IOException, ArrayIndexOutOfBoundsException{
		
		ClassReader cr = new ClassReader(this.getFileAsByteArray(new File(name)));
		
		ClassVisitorImpl cv = new ClassVisitorImpl();
		
		cr.accept(cv, 0);
	}
	
	public ByteCodeParser() throws IOException, ArrayIndexOutOfBoundsException{

		ClassReader cr = new ClassReader("uci.ics.mondego.tldr.App");
		ClassVisitorImpl cv = new ClassVisitorImpl();
		cr.accept(cv, 0);

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
	        return "" + type;
	    }
	  }
	
	public byte[] getFileAsByteArray(File file) {
	    if (file == null) {
	    	return null;
	    }
	    
	    InputStream is = null;
	    try {
		      long length = file.length();
		      if (length > Integer.MAX_VALUE) {
		        return null;
		      }
		      
		      byte[] retval = new byte[(int) length];
		      is = new FileInputStream(file);
		      int off = 0;
		      for (int read = is.read(retval, off, retval.length - off); read > 0; read = is.read(retval, off, retval.length - off)) {
		        off += read;
		      }
		      if (off < retval.length) {
		      
		        return null;
		      }
		      return retval;
	    } 
	    catch (IOException e) {
	      return null;
	    } 
	    finally {
	      try {
			is.close();
	      }
	      catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	      }
	    }
	  }

}
