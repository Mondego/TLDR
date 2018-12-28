package uci.ics.mondego.tldr.changeanalyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;


public class ClassChangeAnalyzer extends ChangeAnalyzer{
	private List<String> changedAttributes;
	private Map<String, Long> hashCodes; // stores all the hashcodes of all fields and methods
	private final ClassParser parser;
	
	public ClassChangeAnalyzer(String className) throws IOException{
		super(className);
		this.changedAttributes = new ArrayList<String>();
		this.hashCodes = new HashMap<String, Long>();
		this.parser = new ClassParser(this.getEntityName());
		this.parse();
	}
	
	public Long getHashCodeByAttribute(String attr){
		return hashCodes.get(attr);
	}
	
	public List<String> getChangedAttributes(){
		return changedAttributes;
	}
	
	private long fieldHashCode(Field f){
		StringBuilder sb = new StringBuilder();
		sb.append(f.getName());
		sb.append(f.getModifiers());
		sb.append(f.getSignature());
		sb.append(f.getType());
		sb.append(f.getAccessFlags());
		sb.append(f.getConstantValue());
		sb.append(f.getConstantPool().toString());
		sb.append(f.getAttributes().toString());
		return sb.toString().hashCode();
	}
	
	protected void parse() throws IOException{
		JavaClass parsedClass = parser.parse();
		
		Field [] allFields = parsedClass.getFields();
		for(Field f: allFields){
			String fieldFqn = parsedClass.getPackageName()+"."+f.getName();
			long currentHashCode = fieldHashCode(f);
			hashCodes.put(fieldFqn, currentHashCode);
			long prevHashCode = -1; /******** GET IT FROM DATABASE *******/
			if(currentHashCode != prevHashCode){
				logger.info(fieldFqn+" changed");
				this.setChanged(true);
				changedAttributes.add(fieldFqn);
				this.sync(fieldFqn, currentHashCode+"");
			}
		}
		
		Method [] allMethods= parsedClass.getMethods();
		
		for(Method m: allMethods){
			String methodFqn = parsedClass.getPackageName()+"."+m.getName();
			long currentHashCode = m.getCode().toString().hashCode();
			hashCodes.put(methodFqn, currentHashCode);
			long prevHashCode = -1; /***** GET IT FROM DATABASE *******/
			if(currentHashCode != prevHashCode){
				logger.info(methodFqn+" changed");
				this.setChanged(true);
				changedAttributes.add(methodFqn);
				this.sync(methodFqn, currentHashCode+"");
			}
		}
	}
}
