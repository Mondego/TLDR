package uci.ics.mondego.tldr.plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import com.opencsv.CSVWriter;

import uci.ics.mondego.tldr.model.Report;
import uci.ics.mondego.tldr.tool.Constants;
import uci.ics.mondego.tldr.tool.ReportWriter;

/**
 * Mojo to run TLDR
 * @author demigorgan
 *
 */
@Mojo(name = "tldr", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "tldr")
public class TLDRMojo extends RunMojo {
	private static final Logger logger = LogManager.getLogger(TLDRMojo.class);

	@Override
	 public void execute() throws MojoExecutionException, MojoFailureException  {	     
	     testRunEndTime = System.nanoTime();	 
	     
	     // test selection end time == test run start time
	     testRunElapsedTimeInSecond = testRunEndTime - testSelectionEndTime;
	     testRunElapsedTimeInSecond = (double) testRunElapsedTimeInSecond / 1000000000.0;	
	     	     
	     ReportWriter reportWriter = new ReportWriter();
	     
	     if (parallel_retest_all.equals(Constants.TRUE)) {
		     String logFileName = 
		    		 getLogDirectory() 
		    		 + commit_serial 
		    		 + "_RETEST_ALL_REPORT_" 
		    		 + commit_hash 
		    		 + "_.txt";
	    	 reportWriter.logExperiment(logFileName, testRunElapsedTimeInSecond);
	     } else {
		     String logFileName = 
		    		 getLogDirectory() 
		    		 + commit_serial 
		    		 + "_REPORT_" 
		    		 + commit_hash 
		    		 + "_.txt";
		     
	    	 reportWriter.logExperiment(
		    		 logFileName, 
		    		 report, 
		    		 testRunElapsedTimeInSecond); 
	    	 
	    	 // Appends the run summary in a CSV file names SUMMARY.csv
	    	 String csvFileName = getLogDirectory()+ "SUMMARY.csv";
	    	 writeReportSummaryInCsv(csvFileName, report);    	 
	     }
	 } 
	
	/**
	 * Appends the run summary to a CSV file in the Log directory.
	 * CSV Format is : 
	 * <commit serial number, commit hash, number of tests to run, test selection time, test run time>
	 * @param csvFileName
	 * @param report
	 */
	private void writeReportSummaryInCsv(String csvFileName, Report report) {
	    CSVWriter writer;
		
	    try {
			writer = new CSVWriter(new FileWriter(csvFileName, true));
			String [] record = {
					commit_serial,
					commit_hash,
					Integer.toString(report.getTestsToRun().size()), 
					Double.toString(report.getSelectionTimeInSecond()),
					Double.toString(testRunElapsedTimeInSecond)};
			
		    writer.writeNext(record);
		    writer.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}      
	}
	
	private String getLogDirectory () {
		String homeDirectory = System.getProperty("user.home");
		
		if (!log_directory.equals("XXXX")) {
			// If a specific directory is fixed then instead of home directory,
			// in the specified directory the log file is written.
			homeDirectory = log_directory; 
		}
		
		String projectName = getProjectName();
		String logFolder = homeDirectory 
				+ Constants.SLASH 
				+ Constants.LOG_DIRECTORY 
				+ Constants.SLASH 
				+ projectName 
				+ Constants.SLASH;
		
		File file = new File(logFolder);
        if (!file.exists()) {
            if (file.mkdirs()) {
            	logger.debug("Log Directory is created!");
            } 
            else {
            	logger.debug("Failed to create log directory!");
            }
        } 
        else {
        	logger.debug("Log directory exists already!");
        }
        return logFolder;
	}
}