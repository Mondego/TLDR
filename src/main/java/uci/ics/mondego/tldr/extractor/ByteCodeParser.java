package uci.ics.mondego.tldr.extractor;


import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;


public class ByteCodeParser {
	
	
	public ByteCodeParser(){
		try {
			ClassReader cr = new ClassReader("uci.ics.mondego.tldr.tool.RedisHandler");
			ClassVisitorImpl cv = new ClassVisitorImpl();
			cr.accept(cv, 0);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

}
