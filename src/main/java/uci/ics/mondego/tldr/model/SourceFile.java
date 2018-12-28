package uci.ics.mondego.tldr.model;

public abstract class SourceFile implements Entities{
 
	
	private String filePath;
	private String fileName;
	
	public SourceFile(){
		
	}
	
	public SourceFile(String name){
		filePath = name;
		fileName = getNameFromAbsolutePath(filePath);
	}
	
	public String getName(){
		return fileName;
	}
	
	public String getPath(){
		return filePath;
	}
	
	private String getNameFromAbsolutePath(String path){
		return path.lastIndexOf('/') == -1 ? path : path.substring(path.lastIndexOf('/') + 1);
	}
	
}
