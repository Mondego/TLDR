package uci.ics.mondego.tldr.model;

public class ClassFile extends SourceFile{

	private String classFileName;
	
	public ClassFile(String name) {
		super(name);
		this.classFileName = name;
	}
	
}
