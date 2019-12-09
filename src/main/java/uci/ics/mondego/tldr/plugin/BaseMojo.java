package uci.ics.mondego.tldr.plugin;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class BaseMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project.build.directory}")
    protected String projectBuildDir;

    @Parameter(defaultValue = "${basedir}")
    protected File basedir;
}
