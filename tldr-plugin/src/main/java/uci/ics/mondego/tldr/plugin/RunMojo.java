package uci.ics.mondego.tldr.plugin;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import uci.ics.mondego.tldr.TLDR;
import uci.ics.mondego.tldr.maven.AgentLoader;
import uci.ics.mondego.tldr.tool.Constants;
import uci.ics.mondego.tldr.tool.Report;
/**
 * Mojo to run TLDR
 * @author demigorgan
 *
 */
@Mojo(name = "run", requiresDependencyResolution = ResolutionScope.TEST)
public class RunMojo extends DiffMojo {

	 private static final Logger logger = LogManager.getLogger(RunMojo.class);	 
	 protected static Report report = null;

	 @Override
	 public void execute() throws MojoExecutionException, MojoFailureException  {
		 
	     System.setProperty(Constants.PARALLEL_RETEST_ALL, parallel_retest_all);
		 
		 if (parallel_retest_all.equals(Constants.TRUE)) {
	    	 testSelectionEndTime = System.nanoTime();
	    	 logger.info(Constants.DISTINCTION_LINE_STAR);
	    	 logger.info("Parallel Retest-all Turned on!!!");
	         logger.info(Constants.DISTINCTION_LINE_STAR);	    	 
	         if (AgentLoader.loadDynamicAgent()) {
		            System.setProperty(Constants.TLDR_TEST_PROPERTY, Constants.ALL_TEST_REGEX);
		     } else {
		            throw new MojoExecutionException("Agent attachment failed");
		     }
	     } 
	     
	     else {
	    	 // TLDR is flagged on. It gets the impacted tests and set the tests field on.
	    	 setIncludesExcludes();
		     if (AgentLoader.loadDynamicAgent()) {
		        	logger.info("AGENT LOADED!!!");
		            System.setProperty(Constants.TLDR_TEST_PROPERTY, getImpactedTests());
		            report = getTestSelectionReport();
			    	testSelectionEndTime = tldr.getSelectionEndTime();		    	
		     } else {
		            throw new MojoExecutionException("Agent attachment failed");
		     }
	     }		
	 }
}
