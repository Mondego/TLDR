package uci.ics.mondego.tldr.ChangeAnalysisExperiment;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import redis.clients.jedis.exceptions.JedisConnectionException;
import uci.ics.mondego.tldr.exception.DatabaseSyncException;
import uci.ics.mondego.tldr.exception.NullDbIdException;
import uci.ics.mondego.tldr.indexer.RedisHandler;
import uci.ics.mondego.tldr.tool.Constants;
import uci.ics.mondego.tldr.tool.DatabaseIDs;
import uci.ics.mondego.tldr.tool.FileHashCalculator;
import uci.ics.mondego.tldr.worker.Worker;

public class FileWorker extends Worker {
	
	private final String fileToAnalyze;
	private RedisHandler redisHandler;
	private Map<String, Integer> allPreviousEntities;
	
	public FileWorker(String filePath){
		this.redisHandler = new RedisHandler();
		this.fileToAnalyze = filePath;
	}
	
	public FileWorker(String workerName, String filePath){
		super(workerName);
		this.redisHandler = new RedisHandler();
		this.fileToAnalyze = filePath;
	}

	public void run() {
		try {
            this.changeAnalyzer();
            redisHandler.close();
        } catch (NoSuchElementException 
        		| IllegalArgumentException 
        		| SecurityException 
        		| IOException
        		| DatabaseSyncException 
        		| JedisConnectionException 
        		| NoSuchAlgorithmException 
        		| InstantiationException 
        		| IllegalAccessException 
        		| InvocationTargetException 
        		| NoSuchMethodException 
        		| NullDbIdException e) {
        	
            e.printStackTrace();
        }	
	}
	
	private void changeAnalyzer() throws 
		IOException, 
		DatabaseSyncException, 
		NoSuchAlgorithmException, 
		JedisConnectionException, 
		NullDbIdException, 
		InstantiationException, 
		IllegalAccessException, 
		IllegalArgumentException, 
		InvocationTargetException, 
		NoSuchMethodException, 
		SecurityException {
		
		if(!redisHandler.exists(DatabaseIDs.TABLE_ID_FILE, fileToAnalyze)){	
			String currentCheckSum = new FileHashCalculator().calculateChecksum(fileToAnalyze);
			redisHandler.update(
					DatabaseIDs.TABLE_ID_FILE, 
					fileToAnalyze, 
					currentCheckSum);
			addNew(fileToAnalyze);
			
			// If class file, then further analyze for field and method
			if(fileToAnalyze.endsWith(Constants.CLASS_EXTENSION)) { 
				ChangeAnalysis.methodWorkerPool.send(fileToAnalyze);
				ChangeAnalysis.fieldWorkerPool.send(fileToAnalyze);
			}
		}
		
		else {
			allPreviousEntities.put(fileToAnalyze, allPreviousEntities.get(fileToAnalyze) + 1);
			
			String prevCheckSum = redisHandler.getValueByKey(DatabaseIDs.TABLE_ID_FILE, fileToAnalyze); 
			String currentCheckSum = new FileHashCalculator().calculateChecksum(fileToAnalyze);
			
			if (!prevCheckSum.equals(currentCheckSum)) {
				addChanged(fileToAnalyze);
				redisHandler.update(DatabaseIDs.TABLE_ID_FILE, fileToAnalyze, currentCheckSum);
				
				if(fileToAnalyze.endsWith(Constants.CLASS_EXTENSION)) { 
					ChangeAnalysis.methodWorkerPool.send(fileToAnalyze);
					ChangeAnalysis.fieldWorkerPool.send(fileToAnalyze);
				}
			}
		}
	}
	
	private void addNew(String fileName) {
		if(fileName.endsWith(".class")) {
			ChangeAnalysis.classData.addNew(fileName);
		} else if (fileName.endsWith(".jar")) {
			ChangeAnalysis.jarData.addNew(fileName);
		} else {
			ChangeAnalysis.otherFileData.addNew(fileName);
		}
	}
	
	private void addChanged(String fileName) {
		if(fileName.endsWith(Constants.CLASS_EXTENSION)) {
			ChangeAnalysis.classData.addChanged(fileName);
		} else if (fileName.endsWith(".jar")) {
			ChangeAnalysis.jarData.addChanged(fileName);
		} else {
			ChangeAnalysis.otherFileData.addChanged(fileName);
		}
	}
}
