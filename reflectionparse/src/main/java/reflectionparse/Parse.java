package reflectionparse;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

public class Parse {
	
	static List<String> clazz = new ArrayList<String>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			
			
			scanClassFiles(args[0]);
			System.out.println(args[0] +"  "+ (containsInstrumentation() || containsReflection()));
		} catch (ClassFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	static boolean containsInstrumentation() throws ClassFormatException, IOException {
		for (int i = 0; i < clazz.size(); i++) {
			String claz = clazz.get(i);
			ClassParser parser = new ClassParser(claz);
			JavaClass parsedClass = parser.parse();
			Method [] allMethods= parsedClass.getMethods();
			
			for(Method m: allMethods){
				if(m.getName().contains("premain") || m.getName().contains("agentmain")) {
					return true;
				}
			}
		}
		return false;
	}
	
	static boolean containsReflection() throws ClassFormatException, IOException {
		
		for (int i = 0; i < clazz.size(); i++) {
			String claz = clazz.get(i);

			ClassParser parser = new ClassParser(claz);
			JavaClass parsedClass = parser.parse();
			Method [] allMethods= parsedClass.getMethods();
			
			for(Method m: allMethods){
				String code =  m.getModifiers() 
						+ m.getName()
						+ m.getSignature()
						+ m.getCode();
	
				if (code.contains("Java.lang.Object.getClass") && 
						(code.contains("Java.lang.Class.getMethods") || 
						code.contains("Java.lang.Class.getFields") || 
						code.contains("Java.lang.Class.getMethod") ||
						code.contains("Java.lang.Class.getField") ||
						code.contains("Java.lang.Class.getConstructors") ||
						code.contains("Java.lang.Class.getConstructor") )) {
					return true;
				}	
			}
		}
		return false;
	} 
	
	
	public static void scanClassFiles(String directoryName) 
			throws InstantiationException, 
			IllegalAccessException,
			IllegalArgumentException, 
			InvocationTargetException, 
			NoSuchMethodException, 
			SecurityException {	
		
		File directory = new File(directoryName);
	    File[] fList = directory.listFiles();	    	
	    if(fList != null)
	        for (File file : fList) {    
	        	
	            if (file.isFile()) {
	            	
	            	String fileAbsolutePath = file.getAbsolutePath();	
	                if(fileAbsolutePath.endsWith(".class")){
	                	
	                	clazz.add(fileAbsolutePath);
	                }	                	         
	            } 
	            else {
	            	scanClassFiles(file.getAbsolutePath());
	            }
	        }
	    }	

}
