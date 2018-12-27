package uci.ics.mondego.tldr.extractor;

import java.io.IOException;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

public class ClassParserExample {
	
	public void parse(String cls){
		try {
			ClassParser cp = new ClassParser(cls);
			JavaClass parsedClass = cp.parse();
			
			System.out.println("Class Name: "+parsedClass.getClassName());
			System.out.println("Package Name: "+parsedClass.getPackageName());
			Field [] allFields = parsedClass.getFields();
			/*System.out.println("Fields are : ");
			for(int i=0;i<allFields.length;i++){
				System.out.println(allFields[i].getType()+"   "+allFields[i].getName());
			}*/
			
			Method [] allMethods= parsedClass.getMethods();
			
			System.out.println("Methods are : ");
			for(Method m: allMethods){
				System.out.println("=============");
				System.out.println(m.getName());
				System.out.println(m.getCode().toString());
				System.out.println("=============");
				
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
