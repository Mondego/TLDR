package uci.ics.mondego.tldr.model;

public class JarFile extends SourceFile{
	
	private String jarName;
	
	public JarFile(String name) {
		super(name);
		this.jarName = name;
	}

}
