package uci.ics.mondego.tldr.model;

public class TestFile extends SourceFile{
	
	enum Annotation 
	{ 
	    Test, 
	    Before, 
	    Aftet,
	    BeforeClass,
	    AfterClass;
	};
	
	private String testClassName;
	
	public TestFile(String name) {
		super(name);
		this.testClassName = name;
	}
	

}
