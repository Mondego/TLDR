package uci.ics.mondego.tldr.plugin;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import uci.ics.mondego.tldr.maven.AgentLoader;
import uci.ics.mondego.tldr.tool.Constants;

/**
 * Mojo to run TLDR
 * @author demigorgan
 *
 */
@Mojo(name = "tldr", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "tldr")
public class TLDRMojo extends RunMojo {

	 private static final Logger logger = LogManager.getLogger(RunMojo.class);	 
	 protected String impactedTests =  null;

	 @Override
	 public void execute() throws MojoExecutionException, MojoFailureException  {
	     System.out.println("************");
	 }
}
