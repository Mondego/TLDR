package uci.ics.mondego.tldr.plugin;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo( name = "report-tldr")
public class ReportMojo extends AbstractMojo {
    private Logger logger;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().debug("Printing Reports");
	}

}
