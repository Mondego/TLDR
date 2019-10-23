package uci.ics.mondego.tldr.ChangeAnalysisExperiment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.opencsv.CSVWriter;
import com.rfksystems.blake2b.Blake2b;

import redis.clients.jedis.exceptions.JedisConnectionException;
import uci.ics.mondego.tldr.App;
import uci.ics.mondego.tldr.exception.DatabaseSyncException;
import uci.ics.mondego.tldr.exception.NullDbIdException;
import uci.ics.mondego.tldr.indexer.RedisHandler;
import uci.ics.mondego.tldr.tool.AccessCodes;
import uci.ics.mondego.tldr.tool.Databases;
import uci.ics.mondego.tldr.tool.FindAllTestDirectory;
import uci.ics.mondego.tldr.tool.StringProcessor;

public class ChangeAnalysis {
	private static final RedisHandler redisHandler = new RedisHandler();
	private static MessageDigest md;
	private static int totalFile = 0;
	public static void main(String[] args) {
		System.out.println("here at start of main");

		// TODO Auto-generated method stub
		String projectDir = args[0];
		String name = args[1];
		String commit = args[2];
		FindAllTestDirectory find = new FindAllTestDirectory(projectDir);
	    Set<String> allTestDir = find.getAllTestDir();
	    Map<String, Set<String>> changedSourceEntities = new HashMap<String, Set<String>>();
	    
	    try {
	    	md = MessageDigest.getInstance("MD5");
			List<String> allSourceClass = scanClassFiles(projectDir, Optional.of(allTestDir));
		    totalFile+= allSourceClass.size();
			
			List<String> allTestClass = new ArrayList<String>();
		    for (String dir: allTestDir) {
		    	allTestClass.addAll(scanClassFiles(dir, Optional.<Set<String>>absent()));
		    }
		    
		    // find all changed source
		    for (int i = 0; i < allSourceClass.size(); i++) {
		    	String claz = allSourceClass.get(i);
		    	if (hasClassChanged (claz)) {
		    		changedSourceEntities.put(claz, getChangedEntities(claz));
		    	}
		    }
		    
		    // find all changed tests
		    /*for (int i = 0; i < allTestClass.size(); i++) {
		    	String claz = allTestClass.get(i);
		    	if (hasClassChanged (claz)) {
		    		changedSourceEntities.put(claz, getChangedEntities(claz));
		    	}
		    }*/
		    
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
		} catch (JedisConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullDbIdException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			System.out.println("here at finally block");
			print(name, commit, changedSourceEntities);
			redisHandler.close();
			System.out.println("here at the end");
			System.exit(0);
		}
	}
	
	private static void print(String name, String commit, Map<String, Set<String>> map) {
	  String csv ="/Users/demigorgan/Documents/workspace/tldr/common-math.csv";
      try{
    	  FileWriter pw = new FileWriter(csv, true);
	      int entity = 0; 
    	  for(Map.Entry<String, Set<String>> entry : map.entrySet()) {
	    	  entity += entry.getValue().size();
		  }
	      pw.append(name);
          pw.append(",");
          pw.append(commit);
          pw.append(",");
          pw.append(totalFile+"");
          pw.append(",");
          pw.append(map.size()+"");
          pw.append(",");
          pw.append(entity+"");
          pw.append("\n");
	      pw.flush();
          pw.close();
      }
      catch (IOException e) {
    	  e.printStackTrace();
      } 
	}
	/**
	 * Returns the set of changed fields and methods of a given class.
	 */
	private static Set<String> getChangedEntities(String file) 
			throws JedisConnectionException, 
			NullDbIdException, 
			ClassFormatException, 
			IOException {
		ClassParser parser = new ClassParser(file);	
		JavaClass parsedClass = parser.parse();
		Set<String> changedEntities = new HashSet<String>();
		
		Field [] allFields = parsedClass.getFields();
		
		for(Field f: allFields){
			String fieldFqn = parsedClass.getClassName()+"."+f.getName();
			String currentHashCode = StringProcessor.CreateBLAKE(f.toString());
			
			if(!redisHandler.exists(Databases.TABLE_ID_ENTITY, fieldFqn)){
				changedEntities.add(fieldFqn);
				redisHandler.update(Databases.TABLE_ID_ENTITY,fieldFqn, currentHashCode);
			} else {
				String prevHashCode = redisHandler.getValueByKey(Databases.TABLE_ID_ENTITY, fieldFqn);
				if (!currentHashCode.equals(prevHashCode)) {
					changedEntities.add(fieldFqn);
					redisHandler.update(Databases.TABLE_ID_ENTITY,fieldFqn, currentHashCode);
				}
			}
		}
		
		Method [] allMethods= parsedClass.getMethods();
		
		for(Method m: allMethods){
			if( m.getModifiers() == AccessCodes.ABSTRACT || 
				m.getModifiers() == AccessCodes.FINAL ||
				m.getModifiers() == AccessCodes.INTERFACE || 
				m.getModifiers() == AccessCodes.NATIVE || 
				m.getModifiers() == AccessCodes.PRIVATE || 
				m.getModifiers() == AccessCodes.PROTECTED || 
				m.getModifiers() == AccessCodes.PUBLIC || 
				m.getModifiers() == AccessCodes.STATIC || 
				m.getModifiers() == AccessCodes.STATIC_INIT ||
				m.getModifiers() == AccessCodes.STRICT || 
				m.getModifiers() == AccessCodes.SYNCHRONIZED || 
				m.getModifiers() == AccessCodes.TRANSIENT || 
				m.getModifiers() == AccessCodes.VOLATILE ||
				m.getModifiers() == AccessCodes.DEFAULT_INIT ||
				m.getModifiers() == AccessCodes.PUBLIC2 ||
				m.getModifiers() == AccessCodes.INHERIT ||		
				m.getModifiers() == AccessCodes.PUBLIC3 ||			
				m.getModifiers() == AccessCodes.PUBLIC4 ||			
				m.getModifiers() == AccessCodes.PUBLIC5 ||				
				m.getModifiers() == AccessCodes.ABSTRACT2 ||				
				m.getModifiers() == AccessCodes.STATIC2 ||	
				m.getModifiers() == AccessCodes.STATIC3 ||	
				m.getModifiers() == AccessCodes.INNER ||	
				m.getModifiers() == AccessCodes.DEFAULT_INIT2 ||
				m.getModifiers() == AccessCodes.FINAL2 ||
				m.getModifiers() == AccessCodes.STATIC4 ||
				m.getModifiers() == AccessCodes.ABSTRACT3||
				m.getModifiers() == AccessCodes.FINAL3 ||
				m.getModifiers() == AccessCodes.STATIC5 ||
				m.getModifiers() == AccessCodes.STATIC6 ||
				m.getModifiers() == AccessCodes.STATIC7 ||
				m.getModifiers() == AccessCodes.STATIC8 ||
				m.getModifiers() == AccessCodes.FINAL4 ||
				m.getModifiers() == AccessCodes.PUBLIC6 ||
				m.getModifiers() == AccessCodes.PUBLIC7 ||
				m.getModifiers() == AccessCodes.PUBLIC8 ||
				m.getModifiers() == AccessCodes.INNER2 ||	
				m.getModifiers() == AccessCodes.FINAL5 ||
				m.getModifiers() == AccessCodes.PUBLIC9 ||
				m.getModifiers() == AccessCodes.INNER3 ||	
				m.getModifiers() == AccessCodes.INNER4 ||					
				m.getModifiers() == AccessCodes.ABSTRACT4 || 
				m.getModifiers() == AccessCodes.PUBLIC10 ||					
				m.getModifiers() == AccessCodes.PUBLIC11 ||
				m.getModifiers() == AccessCodes.PUBLIC12){
					
				String code =  m.getModifiers()+"\n"+ m.getName()+ 
						"\n"+m.getSignature()+"\n"+ m.getCode();
							
				String lineInfo = code.substring(code.indexOf("Attribute(s)") == -1
						? 0 : code.indexOf("Attribute(s)"), 
						code.indexOf("LocalVariable(") == -1?
						code.length() : code.indexOf("LocalVariable(")) ;
				
				code = StringUtils.replace(code, lineInfo, ""); // changes in other function impacts line# of other functions...so Linecount info of the code must be removed
							
				code = code.substring(0, code.indexOf("StackMapTable") == -1? 
						code.length() : code.indexOf("StackMapTable"));  // for some reason StackMapTable also change unwanted. WHY??
				
				code = code.substring(0, code.indexOf("StackMap") == -1? 
						code.length() : code.indexOf("StackMap"));  // for some reason StackMapTable also change unwanted. WHY??
				
				String methodFqn = parsedClass.getClassName()+"."+m.getName();
	
				methodFqn += ("(");
				for(int i=0;i<m.getArgumentTypes().length;i++)
					methodFqn += ("$"+m.getArgumentTypes()[i]);
				methodFqn += (")");
				
				String currentHashCode = StringProcessor.CreateBLAKE(code);
				
				if (!redisHandler.exists(Databases.TABLE_ID_ENTITY, methodFqn)) {
					changedEntities.add(methodFqn);
					redisHandler.update(Databases.TABLE_ID_ENTITY, methodFqn, currentHashCode);
				} else {
					String prevHashCode = redisHandler.getValueByKey(Databases.TABLE_ID_ENTITY, methodFqn);
					if (!currentHashCode.equals(prevHashCode)) {
						changedEntities.add(methodFqn);
						redisHandler.update(Databases.TABLE_ID_ENTITY, methodFqn, currentHashCode);
					}
				}
			}
		}
		return changedEntities;
	}
	
	/**
	 * Returns true if the specified file has changed.
	 */
	private static boolean hasClassChanged(String file) 
			throws JedisConnectionException, 
			NullDbIdException, 
			IOException, 
			NoSuchAlgorithmException {
		String currentCheckSum = calculateChecksum(file);
		
		if(!redisHandler.exists(Databases.TABLE_ID_FILE, file)){	
			redisHandler.update(Databases.TABLE_ID_FILE, file, currentCheckSum);			
			return true;
		}
		
		String prevCheckSum = redisHandler.getValueByKey(Databases.TABLE_ID_FILE, file);

		if (!prevCheckSum.equals(currentCheckSum)) {
			redisHandler.update(Databases.TABLE_ID_FILE, file, currentCheckSum);		
			return true;
		}
		return false;	
	}
	
	/**
	 * Calculates the checksum of a file using Blake2B algorithm.
	 * @param fileName: the name of the file
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException 
	 */
	private static String calculateChecksum(String fileName) throws IOException, NoSuchAlgorithmException {
		
		InputStream fis = new FileInputStream(fileName);
        byte[] buffer = new byte[1024];
        int nread;        
        while ((nread = fis.read(buffer)) != -1) {
            md.update(buffer, 0, nread);
        }
        
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        
        fis.close();
        return result.toString();
    }
	
	/**
	 * Method to scan repository for .class files. This method can scan a  project's
	 * source and test repository and returns a list of class file. 
	 * @param directoryName The directory from where the scanning is started.
	 * @param exclude an optional set of directories which are excluded from the search. 
	 */
	private static List<String> scanClassFiles(String directoryName, Optional<Set<String>> exclude) 
			throws InstantiationException, 
			IllegalAccessException,
			IllegalArgumentException, 
			InvocationTargetException, 
			NoSuchMethodException, 
			SecurityException {	
		
		List<String> classFiles = new ArrayList<String>();
		File directory = new File(directoryName);
	    File[] fList = directory.listFiles();	    	
	    
	    if(fList != null)
	        for (File file : fList) {    	        	
	            if (file.isFile()) {
	            	String fileAbsolutePath = file.getAbsolutePath();	
	                if(fileAbsolutePath.endsWith(".class")){
	                	classFiles.add(fileAbsolutePath);
	                }	                	         
	            } 
	            else if (file.isDirectory()) {
	            	if (!exclude.isPresent() || !exclude.get().contains(file.getAbsolutePath().toString())) {
		            	classFiles.addAll(scanClassFiles(file.getAbsolutePath(), exclude));
	            	}
	            }
	        }
	    return classFiles;
	  }	
	
}
