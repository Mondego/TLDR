package uci.ics.mondego.tldr.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo( name = "run-tldr")
public class RunMojo extends AbstractMojo {
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().debug("Running the selected tests");
	}
}
