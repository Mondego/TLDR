package uci.ics.mondego.tldr.plugin;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.surefire.SurefirePlugin;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class BaseMojo extends SurefirePlugin {
    private Logger logger;

	@Parameter(defaultValue = "${project.build.directory}")
    protected String projectBuildDir;

    @Parameter(defaultValue = "${basedir}")
    protected File basedir;
}
