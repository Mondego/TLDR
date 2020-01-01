package uci.ics.mondego.tldr.plugin;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo( 
		name = "run-tldr", 
		requiresDirectInvocation = true, 
		requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "tldr")
public class RunMojo extends BaseMojo {
    private Logger logger;

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().debug("Running the selected tests");
	}
}
