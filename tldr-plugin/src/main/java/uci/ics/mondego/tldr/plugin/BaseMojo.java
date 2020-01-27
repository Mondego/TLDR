/*
 * Copyright (c) 2015 - Present. The STARTS Team. All Rights Reserved.
 */

package uci.ics.mondego.tldr.plugin;

import java.lang.reflect.Field;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.surefire.AbstractSurefireMojo;
import org.apache.maven.plugin.surefire.SurefirePlugin;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.surefire.booter.Classpath;

/**
 * Base Mojo for TLDR.
 * @author demigorgan
 *
 */
abstract class BaseMojo extends SurefirePlugin {
	private static final Logger logger = LogManager.getLogger(BaseMojo.class);
    
    /**
     * Hash code of a commit. This is needed to generate report for each 
     * sample commit iteratively in an experiment.
     */
    @Parameter(property = "commit.hash", required = false, defaultValue = "0000F")
    protected String commit_hash;
    
    /**
     * Serial number of a sampled commit. This is needed to generate
     * report for each sample commit iteratively in an experiment.
     */
    @Parameter(property = "commit.serial", required = false, defaultValue = "-1")
    protected String commit_serial;
    
    /**
     * Build directory of the project upon which the plugin is invoked
     */
    @Parameter(defaultValue = "${project.build.directory}")
    protected String projectBuildDir;

    /**
     * Name of the project. This is needed particularly for multi module projects as it is difficult to
     * figure out which project is the parent project.
     */
    @Parameter(property = "multimodule.projectname", required = false, defaultValue = "XXX")
    protected String multi_module_project_name;

    /**
     * Optional flag to write Logs to a particular directory.
     */
    @Parameter(property = "log.directory", required = false, defaultValue = "XXXX")
    protected String log_directory;
    
    /**
     * Optional flag to for debug print statements. For debugging purpose turn this flag ON.
     */
    @Parameter(property = "debug.flag", required = false, defaultValue = "false")
    protected String debug_flag;
    
    protected Classpath sureFireClassPath;
    
    protected static double testRunElapsedTimeInSecond;
    protected static long testRunEndTime;
    protected static long testSelectionEndTime;
    
    public Classpath getSureFireClassPath() throws MojoExecutionException {
        if (sureFireClassPath == null) {
            try {
                sureFireClassPath = new Classpath(getProject().getTestClasspathElements());
            } catch (DependencyResolutionRequiredException drre) {
                drre.printStackTrace();
            }
        }
        return sureFireClassPath;
    }
    
    public String getModuleName() {
    	String moduleName = multi_module_project_name;
    	Field projectField;
		try {
			projectField = AbstractSurefireMojo.class.getDeclaredField("project");
			projectField.setAccessible(true);
	        MavenProject accessedProject = (MavenProject) projectField.get(this);
	        moduleName = accessedProject.getArtifact().getArtifactId();
		} catch (NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return moduleName;
    }
        
    public String getProjectName() {
    	String projectName = multi_module_project_name;
    	try {
    		Field projectField = AbstractSurefireMojo.class.getDeclaredField("project");
    		projectField.setAccessible(true);
            MavenProject accessedProject = (MavenProject) projectField.get(this);
            
            if (multi_module_project_name.equals("XXX")) {          	
                projectName = accessedProject.getArtifact().getArtifactId();
            }
    	} 
    	catch (NoSuchFieldException nsfe) {
            nsfe.printStackTrace();
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
        }
    	return projectName;
    }	
}
