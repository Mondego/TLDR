package uci.ics.mondego.tldr.plugin;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import uci.ics.mondego.tldr.TLDR;
import uci.ics.mondego.tldr.model.Report;
import uci.ics.mondego.tldr.tool.Constants;
import uci.ics.mondego.tldr.tool.TLDRRunProperty;

@Mojo(name = "diff", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST_COMPILE)
public class DiffMojo extends BaseMojo {
	
	private static final Logger logger = LogManager.getLogger(DiffMojo.class);
    protected static TLDR tldr = new TLDR(Integer.parseInt(thread_count));
	
    public void execute() throws MojoExecutionException , MojoFailureException {
    	String impactedTests = getImpactedTests();   	    	
    	logger.info("Following tests were impacted .... ");
    	logger.info(impactedTests);
    }
    
    public Report getTestSelectionReport() {
    	return tldr.getTestSelectionReport();
    }
    
    public String getImpactedTests() {
    	logger.info("TLDR starting....");
    	
    	TLDRRunProperty tldrRunProperty = new TLDRRunProperty(
    			projectBuildDir, 
    			getProjectName(), 
    			commit_hash, 
    			commit_serial, 
    			Constants.BUILD_TOOL_TYPE_MAVEN);
    	String impactedTests = tldr.getImpactedTest(tldrRunProperty);
    	if (impactedTests.length() == 0 || impactedTests == null) {
    		logger.info("No test was impacted.");
    	}
    	return impactedTests;
    } 
}