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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opencsv.CSVWriter;
import com.rfksystems.blake2b.Blake2b;

import io.netty.util.internal.ConcurrentSet;
import redis.clients.jedis.exceptions.JedisConnectionException;
import uci.ics.mondego.tldr.TLDR;
import uci.ics.mondego.tldr.exception.DatabaseSyncException;
import uci.ics.mondego.tldr.exception.NullDbIdException;
import uci.ics.mondego.tldr.indexer.RedisHandler;
import uci.ics.mondego.tldr.tool.AccessCodes;
import uci.ics.mondego.tldr.tool.Databases;
import uci.ics.mondego.tldr.tool.FileHashCalculator;
import uci.ics.mondego.tldr.tool.FindAllTestDirectory;
import uci.ics.mondego.tldr.tool.StringProcessor;

public class ChangeAnalysis {
	private static final RedisHandler redisHandler = new RedisHandler();
	private static final FileHashCalculator fileHashCalculator = 
			new FileHashCalculator(); 
	private static final String CLASS_EXT = ".class";
	private static final String JAR_EXT = ".jar";
	
	private static Data sourceClassData = new Data();
	private static Data sourceJarData = new Data();
	private static Data sourceOtherFileData = new Data();
	private static Data sourceMethodData = new Data();
	private static Data sourceFieldData = new Data();
	
	private static Data testClassData = new Data();
	private static Data testJarData = new Data();
	private static Data testOtherFileData = new Data();
	private static Data testMethodData = new Data();
	private static Data testFieldData = new Data();
	
	private String projectName;
	private String projectDirectory;
	private String commitHashCode;
	
	private Set<String> allFieldsAndMethods = new HashSet<String>();
	
	private enum FileType {
		CLASS,
		JAR,
		OTHER
	};
	
	private enum CodeType {
		SOURCE,
		TEST,
		OTHER
	};
	
	public static void main(String[] args) {
		// Get experiment specific information.
		ChangeAnalysis changeAnalysis = new ChangeAnalysis(
				/* projectDirectory= */ args[0], 
				/* projectName= */ args[1], 
				/* CommitHashCode= */ args[2]);
	
	   // Do analysis
		
		// Print
		System.out.println("here at finally block");
		//changeAnalysis.print();
		System.out.println("here at the end");
		System.exit(0);
	}
	
	public ChangeAnalysis(
			String projectDirectory, String projectName, String CommitHashCode) {
		this.projectDirectory = projectDirectory;
		this.projectName = projectName;
		this.commitHashCode = commitHashCode;
		 	
    	if (redisHandler.projectExists(projectName)) {
    		String projectId = redisHandler.getProjectId(projectName);
			allFieldsAndMethods.addAll(redisHandler.getAllMethodsAndFields(projectId));
    	} else {
    		redisHandler.insertProject(projectName);
    	} 
    	
    	// Close the Redis Handler before terminating the program
		redisHandler.close();
	}
	
	/** 
	 * Finds all the changed entities i.e. class, jar, other files
	 */
	private List<String> findChangedEntities (
			FileType filetype,
			CodeType codeType) throws 
		InstantiationException, 
		IllegalAccessException, 
		IllegalArgumentException, 
		InvocationTargetException, 
		NoSuchMethodException, 
		SecurityException, 
		JedisConnectionException, 
		NoSuchAlgorithmException, 
		NullDbIdException, 
		IOException {
		
		// Discover all test directory.
		FindAllTestDirectory find = new FindAllTestDirectory(projectDirectory);
	    Set<String> allTestDirectory = find.getAllTestDir();
		
	    Optional<ImmutableSet<String>> excludeDirOptional = Optional.<ImmutableSet<String>>absent();
		Optional<String> extensionOptional = Optional.absent();
		Optional<ImmutableSet<String>> excludeExtOptional = Optional.<ImmutableSet<String>>absent();
		
		// If only source is intended to be scanned then test repositories must be
		// excluded.
		if (codeType == CodeType.SOURCE) {
			excludeDirOptional = Optional.of(ImmutableSet.copyOf(allTestDirectory));
		}
		
		switch(filetype) {
			case CLASS:
				extensionOptional = Optional.of(CLASS_EXT);
				break;
			case JAR:
				extensionOptional = Optional.of(JAR_EXT);
				break;
			case OTHER:
				// If we want to scan only Other files then class and jar extension 
				// must be avoided
				excludeExtOptional = Optional.of(ImmutableSet.of(CLASS_EXT, JAR_EXT));
				break;
		}
		
		List<String> allInstances = new ArrayList<String>();
		if (codeType == CodeType.SOURCE) {
			allInstances.addAll(
					scanner(projectDirectory, excludeDirOptional, extensionOptional, excludeExtOptional));
		} else {
			for (String testDir: allTestDirectory) {
				allInstances.addAll(
						scanner(testDir, excludeDirOptional, extensionOptional, excludeExtOptional));
			}
		}
		
		List<String> allChangedEntities = new ArrayList<String>();
		
		for(int i= 0; i < allInstances.size(); i++) {
			String file = allInstances.get(i);
			if (hasChanged(file)) {
				allChangedEntities.add(file);
			}
		}
		
		return allChangedEntities;
	}
	
	/**
	 * Returns true if the file is newly added
	 */
	private static boolean isNew (String file) 
			throws JedisConnectionException, 
				NoSuchAlgorithmException, 
				NullDbIdException, 
				IOException {
		if (!redisHandler.exists(Databases.TABLE_ID_FILE, file)){	
			redisHandler.update(
					Databases.TABLE_ID_FILE, 
					file, 
					fileHashCalculator.calculateChecksum(file));			
			return true;
		}
		return false;
	}
		
	/**
	 * Returns true if the specified file has changed.
	 */
	private static boolean hasChanged(String file) 
			throws JedisConnectionException, 
			NullDbIdException, 
			IOException, 
			NoSuchAlgorithmException {
		String currentCheckSum = fileHashCalculator.calculateChecksum(file);
		
		if (!redisHandler.exists(Databases.TABLE_ID_FILE, file)){	
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
	 * Method to scan repository for files with a particular extension 
	 * or all files except some specified extension. This method can scan 
	 * a project's source and test repository and returns a list of files. 
	 * @param directoryName The directory from where the scanning is started.
	 * @param excludeDir an optional set of directories which are excluded from the search. 
	 * @param extension specified extension of the files to be searched
	 * @param excludeExtension extensions that are to be excluded from the search
	 */
	private static List<String> scanner (
			String directoryName, 
			Optional<ImmutableSet<String>> excludeDir, 
			Optional<String> extension, 
			Optional<ImmutableSet<String>> excludeExtension) 
				throws InstantiationException, 
				IllegalAccessException,
				IllegalArgumentException, 
				InvocationTargetException, 
				NoSuchMethodException, 
				SecurityException {	
		
		List<String> filesWithExtension = new ArrayList<String>();
		File directory = new File(directoryName);
	    File[] fList = directory.listFiles();	    	
	    
	    if(fList != null) {
	    	for (File file : fList) {    	        	
	            if (file.isFile()) {
	            	String fileAbsolutePath = file.getAbsolutePath();	
	                
	            	// class file and jar file detection
	            	if (extension.isPresent()) {
	            		String extensionString = extension.get();
	            		if (fileAbsolutePath.endsWith(extensionString)){
	            			filesWithExtension.add(fileAbsolutePath);
		                }
	            	} else {
	            		// All other file detection
	            		if (excludeExtension.isPresent()) {
	            			boolean isExlcuded = false;
	            			// Making sure the file does not have the
	            			// excluded extensions.
	            			for (String ext: excludeExtension.get()) {
	            				if (fileAbsolutePath.endsWith(ext)) {
	            					isExlcuded = true;
	            					break;
	            				}
	            			}
	            			if (!isExlcuded) {
	            				filesWithExtension.add(fileAbsolutePath);
	            			}
	            		}
	            	}                	         
	            } else if (file.isDirectory()) {
	            	if (!excludeDir.isPresent() || 
	            			!excludeDir.get().contains(file.getAbsolutePath().toString())) {
	            		filesWithExtension.addAll(
	            				scanner(file.getAbsolutePath(), excludeDir, extension, excludeExtension));
	            	}
	            }
	        }
	    }
	    return filesWithExtension;
	  }	
	/*
	private static void print() {
		  String csv ="/lv_scratch/scratch/mondego/local/Maruf/TLDR/"+name+".csv";
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
		*/
}
