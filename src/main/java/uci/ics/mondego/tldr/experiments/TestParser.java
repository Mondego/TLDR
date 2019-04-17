package uci.ics.mondego.tldr.experiments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.commons.lang3.StringUtils;
import uci.ics.mondego.tldr.App;
import uci.ics.mondego.tldr.changeanalyzer.ChangeAnalyzer;
import uci.ics.mondego.tldr.exception.DatabaseSyncException;
import uci.ics.mondego.tldr.exception.NullDbIdException;
import uci.ics.mondego.tldr.tool.AccessCodes;
import uci.ics.mondego.tldr.tool.Databases;
import uci.ics.mondego.tldr.tool.StringProcessor;


public class TestParser{
	private  ClassParser parser = null;
	private  JavaClass parsedClass = null;
	public Set<String> fields;
	public Set<String> testMethods;
	public Set<String> helperMethods;
	
	
	public TestParser(String className) {
		try {
			this.parser = new ClassParser(className);

			this.parsedClass = parser.parse();
			this.fields = new HashSet<String>();
			this.testMethods = new HashSet<String>();
			this.helperMethods = new HashSet<String>();
			
			this.parse();	
		} catch (ClassFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
					
	}
	
	private void parse() {
				
		Field [] allFields = parsedClass.getFields();
		
		for(Field f: allFields){
			IntraTestDependencyExperiment.count++;
			fields.add(parsedClass.getClassName()+"."+f.getName());
		}
		
		Method [] allMethods= parsedClass.getMethods();
		
		for(Method m: allMethods){	
			IntraTestDependencyExperiment.count++;

			   AnnotationEntry[] ant = m.getAnnotationEntries();  
			   
			   String annotations = "";
			   for(int i=0;i<ant.length;i++){
				   annotations+=ant[i].getAnnotationType();
			   }
			    
			    if(m.getCode() == null)
			    	continue;
				String code =  m.getCode().toString();
				if(code == null || code.length() == 0)
					continue;
				
				String methodFqn = parsedClass.getClassName()+"."+m.getName();	
				methodFqn += ("(");
				for(int i=0;i<m.getArgumentTypes().length;i++)
					methodFqn += ("$"+m.getArgumentTypes()[i]);
				methodFqn += (")");
				
				
				if(annotations.contains("org/junit"))
					testMethods.add(methodFqn);
				else
					helperMethods.add(methodFqn);				
		}		
	}
}
