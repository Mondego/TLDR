package uci.ics.mondego.tldr.plugin;

import java.io.File;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

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
	     
	     String logFileName = getLogDirectory() + commit_serial + "_REPORT_" + commit_hash + "_.txt";
	     
	     ReportWriter reportWriter = new ReportWriter();
	     reportWriter.logExperiment(
	    		 logFileName, 
	    		 report, 
	    		 testRunElapsedTimeInSecond);
	 } 
	
	
	private String getLogDirectory () {
		String homeDirectory = System.getProperty("user.home");
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
            } else {
            	logger.debug("Failed to create log directory!");
            }
        } else {
        	logger.debug("Log directory exists already!");
        }
        return logFolder;
	}
}
