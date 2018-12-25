package uci.ics.mondego.tldr.model;

public class JavaFile extends SourceFile{

	private String javaFileName;
	
	public JavaFile(String name) {
		super(name);
		this.javaFileName = name;
	}
}
