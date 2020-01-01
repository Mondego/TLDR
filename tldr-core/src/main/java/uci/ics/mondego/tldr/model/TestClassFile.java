package uci.ics.mondego.tldr.model;

import org.apache.bcel.classfile.JavaClass;

public class TestClassFile extends ClassFile{
	enum Annotation 
	{ 
	    Test, 
	    Before, 
	    Aftet,
	    BeforeClass,
	    AfterClass;
	};
	
	public TestClassFile(String name, JavaClass code) {
		super(name, code);		
	}
}
