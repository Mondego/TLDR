package uci.ics.mondego.tldr.tool;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

public class FindAllTestDirectory {

	private Set<String> all_test_urls = new HashSet<String>();
	
	public FindAllTestDirectory(String CLASS_DIR) {
		// TODO Auto-generated method stub
		try {
			scanTestFiles(CLASS_DIR, "/test");
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void scanTestFiles(String directoryName, String pattern) throws InstantiationException, IllegalAccessException,
	IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {	

	File directory = new File(directoryName);
    File[] fList = directory.listFiles();	    	
    if(fList != null)
        for (File file : fList) {    	        	
            if (file.isDirectory()) {
            	if(file.getAbsolutePath().toString().contains(pattern)){
            		String dir = file.getAbsolutePath()
            				.substring(0,file.getAbsolutePath().toString().indexOf(pattern));
            		
            		String temp = file.getAbsolutePath()
            				.toString().substring(file.getAbsolutePath().toString().indexOf(pattern) + 1);            		
            		dir = dir+"/"+temp;
            		
            		if(!all_test_urls.contains(dir)){
            			all_test_urls.add(dir);
            		}
            	}
            	else
            		scanTestFiles(file.getAbsolutePath(), pattern);
            }
        }
	}
	
	public Set<String> getAllTestDir(){
		return all_test_urls;
	}
}

