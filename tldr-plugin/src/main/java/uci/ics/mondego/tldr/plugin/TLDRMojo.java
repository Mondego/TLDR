package uci.ics.mondego.tldr.plugin;

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

	 protected String impactedTests =  null;

	 @Override
	 public void execute() throws MojoExecutionException, MojoFailureException  {	     
	     endTime = System.nanoTime();	 
	     elapsedTime = endTime - /* test selection end time == test run start time*/ tldr.getSelectionEndTime();
	     double testRunTimeInSecond = (double)elapsedTime / 1000000000.0;	
	     
	     String logFileName = 
	    		 Constants.LOG_DIRECTORY + getProjectName()+"_"+commit_serial+"_REPORT_"+commit_hash+"_.txt";
	     
	     ReportWriter reportWriter = new ReportWriter();
	     reportWriter.logExperiment(logFileName, tldr.getSelectionElapsedTimeInSecond(), testRunTimeInSecond);
	 } 
}
