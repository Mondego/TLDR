package uci.ics.mondego.tldr.extractor;

import java.io.File;
import java.util.List;
import org.eclipse.core.*;
import org.eclipse.jdt.core.dom.ASTParser;


public class Scanner {
	
	private String PROJ_DIR;
	
	List<String> all_files;
	
	public Scanner(){
		PROJ_DIR = System.getProperty("user.dir");
	}
	
	
	/* gets all file from the project directory*/
	
	public void listf(String directoryName, List<File> files) {
	    File directory = new File(directoryName);

	    File[] fList = directory.listFiles();
	    
	    if(fList != null)
	        for (File file : fList) {      
	            if (file.isFile()) {
	                files.add(file);
	            } else if (file.isDirectory()) {
	                listf(file.getAbsolutePath(), files);
	            }
	        }
	    }
	}


