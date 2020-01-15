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
	 protected static Report report;

	 @Override
	 public void execute() throws MojoExecutionException, MojoFailureException  {
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
