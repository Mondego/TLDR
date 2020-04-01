package uci.ics.mondego.tldr.ChangeAnalysisExperiment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.opencsv.CSVWriter;
import uci.ics.mondego.tldr.indexer.RedisHandler;
import uci.ics.mondego.tldr.model.Report;
import uci.ics.mondego.tldr.model.ThreadedChannel;
import uci.ics.mondego.tldr.tool.Constants;
import uci.ics.mondego.tldr.tool.DatabaseIDs;

public class ChangeAnalysis {
	protected static final RedisHandler redisHandler = new RedisHandler();
	
	public static Data classData = new Data();
	public static Data jarData = new Data();
	public static Data otherFileData = new Data();
	public static Data methodData = new Data();
	public static Data fieldData = new Data();
	
	public static ThreadedChannel<String> fileWorkerPool;
    public static ThreadedChannel<String> methodWorkerPool;
    public static ThreadedChannel<String> fieldWorkerPool;
    
	private static Map<String, Integer> allPreviousEntities;
	private Set<String> allClass;
	private Set<String> allJar;
	private Set<String> allOther;

	private static String serial;
	private static String projectName;
	private static String projectDirectory;
	private static String commitHashCode;
	
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
				/* serial= */ args[0],
				/* projectDirectory= */ args[1], 
				/* projectName= */ args[2], 
				/* CommitHashCode= */ args[3]);
	
		try {
			List<String> allFiles = scanner(projectDirectory);
			
			for(int i = 0; i < allFiles.size(); i++) {
				if (allFiles.get(i).endsWith(Constants.CLASS_EXTENSION)) {
					ChangeAnalysis.classData.incCount();
				} else if (allFiles.get(i).endsWith(Constants.JAR_EXTENSION)) {
					ChangeAnalysis.jarData.incCount();
				} else {
					ChangeAnalysis.otherFileData.incCount();
				}
				
				ChangeAnalysis.fileWorkerPool.send(allFiles.get(i));
			}
			deleteDepreciatedEntities();
			RedisHandler.destroyPool();
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public ChangeAnalysis(
			String serial, 
			String projectDirectory, 
			String projectName, 
			String commitHashCode) {
		this.serial = serial;
		this.projectDirectory = projectDirectory;
		this.projectName = projectName;
		this.commitHashCode = commitHashCode;
		
		this.allClass = new HashSet<>();
		this.allJar = new HashSet<>();
		this.allOther = new HashSet<>();
		
		this.fileWorkerPool = new ThreadedChannel<String>(16, FileWorker.class);
    	this.methodWorkerPool = new ThreadedChannel<String>(16, MethodWorker.class);
    	this.fieldWorkerPool = new ThreadedChannel<String>(16, FileWorker.class);
    	
		RedisHandler.createPool(); 
	   	String project_id = getProjectId(this.projectName);
	    System.setProperty(Constants.PROJECT_ID, project_id);

	    RedisHandler.destroyPool();
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
	private static List<String> scanner (String directoryName) 
				throws InstantiationException, 
				IllegalAccessException,
				IllegalArgumentException, 
				InvocationTargetException, 
				NoSuchMethodException, 
				SecurityException {	
		
		File directory = new File(directoryName);
	    File[] fList = directory.listFiles();	    	
	    List <String> allFiles = new ArrayList<>();
	    if(fList != null) {
	    	for (File file : fList) {    	        	
	            if (file.isFile()) {	            	
	            	String fileAbsolutePath = file.getAbsolutePath();
	            	allFiles.add(fileAbsolutePath);
	            	allPreviousEntities.put(fileAbsolutePath, allPreviousEntities.get(fileAbsolutePath) + 1);               	         
	            } else if (file.isDirectory()) {	            	
	            	allFiles.addAll(scanner(file.getAbsolutePath()));
	            }
	        }
	    }
	    return allFiles;
	  }	
	
	private void writeReportSummaryInCsv(String csvFileName, Report report) {
	    CSVWriter writer;
		
	    try {
			writer = new CSVWriter(new FileWriter(csvFileName, true));
			String [] record = {
					serial,
					commitHashCode,
										
					Integer.toString(classData.getTotalCount()),
					Integer.toString(classData.getNewCount()),
					Integer.toString(classData.getChangedCount()),
					Integer.toString(classData.getDeletedCount()),
					
					Integer.toString(methodData.getNewCount()),
					Integer.toString(methodData.getChangedCount()),
					Integer.toString(methodData.getDeletedCount()),
					
					Integer.toString(fieldData.getNewCount()),
					Integer.toString(fieldData.getChangedCount()),
					Integer.toString(fieldData.getDeletedCount()),
									
					Integer.toString(jarData.getTotalCount()),
					Integer.toString(jarData.getNewCount()),
					Integer.toString(jarData.getChangedCount()),
					Integer.toString(jarData.getDeletedCount()),
					
					Integer.toString(otherFileData.getTotalCount()),
					Integer.toString(otherFileData.getNewCount()),
					Integer.toString(otherFileData.getChangedCount()),
					Integer.toString(otherFileData.getDeletedCount())};
			
		    writer.writeNext(record);
		    writer.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}      
	}
	
	/** 
     * Gets the ID of a project. If the project is not in db then
     * it inserts the project in the DB and retuns the address.
     * @param projectName
     */
    private String getProjectId(String projectName) {
    	RedisHandler redisHandler = new RedisHandler();
    	if (redisHandler.projectExists(projectName)) {
    		System.setProperty(Constants.FIRST_TIME, Constants.FALSE);
    		String projectId = redisHandler.getProjectId(projectName);
    		
    		Set<String> prevEnt = redisHandler.getAllKeysByPattern(DatabaseIDs.TABLE_ID_FILE, "*");  
			
			this.allPreviousEntities= new HashMap<String, Integer>();
			for(String entity: prevEnt){
				allPreviousEntities.put(entity, 0);
			}
    		
        	redisHandler.close();
    		return projectId;
    	}
    	// Else insert the project and set FIRST_TIME flag to true so that all the tests are run.
    	System.setProperty(Constants.FIRST_TIME, Constants.TRUE);
    	redisHandler.insertProject(projectName);
    	String projectId = redisHandler.getProjectId(projectName);
    	redisHandler.close();
    	return projectId;
    }
    
    private static void deleteDepreciatedEntities() {
    	RedisHandler redisHandler = new RedisHandler();
		for ( Map.Entry<String, Integer> entry : allPreviousEntities.entrySet()) {
		    Integer val = entry.getValue();
		    if (val == 0) {
		    	addDeleted(entry.getKey());
		    	redisHandler.removeKey(DatabaseIDs.TABLE_ID_FILE, entry.getKey());
		    	DeleteAllMethodsAndField(entry.getKey());
		    }  
		}
		redisHandler.close();
	}
    
    private static void addDeleted(String fileName) {
		if(fileName.endsWith(Constants.CLASS_EXTENSION)) {
			ChangeAnalysis.classData.addDeleted(fileName);
		} else if (fileName.endsWith(".jar")) {
			ChangeAnalysis.jarData.addDeleted(fileName);
		} else {
			ChangeAnalysis.otherFileData.addDeleted(fileName);
		}
	}
    
    private static void DeleteAllMethodsAndField(String fileToAnalyze) {
    	try {
    		ClassParser parser = new ClassParser(fileToAnalyze);
			JavaClass parsedClass = parser.parse();
			Method [] allMethods= parsedClass.getMethods();
			
			for(Method m: allMethods){
				String methodFqn = parsedClass.getClassName()+"."+m.getName();
				methodFqn += ("(");
				for(int i=0;i<m.getArgumentTypes().length;i++)
					methodFqn += ("$"+m.getArgumentTypes()[i]);
				methodFqn += (")");
		    	ChangeAnalysis.methodData.addDeleted(methodFqn);
		    	redisHandler.removeKey(DatabaseIDs.TABLE_ID_ENTITY, methodFqn);
			}
			
			Field [] allFields = parsedClass.getFields();
			
			for (Field f: allFields) {
				String fieldFqn = parsedClass.getClassName()+"."+f.getName();
		    	ChangeAnalysis.fieldData.addDeleted(fieldFqn);
		    	redisHandler.removeKey(DatabaseIDs.TABLE_ID_ENTITY, fieldFqn);

			}
		} catch (ClassFormatException | IOException e) {
			e.printStackTrace();
		}    	
	}
}
