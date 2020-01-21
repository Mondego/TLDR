package uci.ics.mondego.tldr.tool;

/**
 * Property class. This class is created to configure properties to TLDR-core.
 * @author demigorgan
 *
 */
public class TLDRRunProperty {
	
	private String class_dir;
	private String project_name;
	private String commit_hash;
	private String commit_serial;
	private String tool_type;
	
	public TLDRRunProperty (
			String class_dir, 
			String project_name, 
			String commit_hash, 
			String commit_serial, 
			String tool_type) {
		this.class_dir = class_dir;
		this.project_name = project_name;
		this.commit_hash = commit_hash;
		this.commit_serial = commit_serial;
		this.tool_type = tool_type;
	}
	
	public TLDRRunProperty() {
		this.class_dir = this.project_name = this.commit_hash = this.tool_type = this.commit_serial = null;
	}
	
	public String getClass_dir() {
		return class_dir;
	}
	
	public String getProject_name() {
		return project_name;
	}
	
	public String getCommit_hash() {
		return commit_hash;
	}
	
	public String getCommit_serial() {
		return commit_serial;
	}
	
	public String getTool_type() {
		return tool_type;
	}
}
