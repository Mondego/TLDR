package uci.ics.mondego.tldr.model;

public class TestJavaFile extends SourceFile{
	
	enum Annotation 
	{ 
	    Test, 
	    Before, 
	    Aftet,
	    BeforeClass,
	    AfterClass;
	};
	
	private String testClassName;
	
	public TestJavaFile(String name) {
		super(name);
		this.testClassName = name;
	}
	
	
}
