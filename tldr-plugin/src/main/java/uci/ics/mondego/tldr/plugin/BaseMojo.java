/*
 * Copyright (c) 2015 - Present. The STARTS Team. All Rights Reserved.
 */

package uci.ics.mondego.tldr.plugin;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import uci.ics.mondego.tldr.TLDR;
import uci.ics.mondego.tldr.tool.PomUtil;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.surefire.AbstractSurefireMojo;
import org.apache.maven.plugin.surefire.SurefirePlugin;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.surefire.booter.Classpath;
import org.apache.maven.surefire.util.DefaultScanResult;

/**
 * Base Mojo for TLDR.
 * @author demigorgan
 *
 */
abstract class BaseMojo extends SurefirePlugin {
    static final String STAR = "*";
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

    @Parameter(defaultValue = "${basedir}")
    protected File basedir;
    
    protected Classpath sureFireClassPath;
    
    protected long elapsedTime;
    protected long startTime;
    protected long endTime;
    
    protected TLDR tldr = new TLDR();

    public List<String> getTestClasses() {
        DefaultScanResult defaultScanResult = null;
        try {
            Method scanMethod = AbstractSurefireMojo.class.getDeclaredMethod("scanForTestClasses", null);
            scanMethod.setAccessible(true);
            defaultScanResult = (DefaultScanResult) scanMethod.invoke(this, null);
        } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
        } catch (InvocationTargetException ite) {
            ite.printStackTrace();
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
        }
        return (List<String>) defaultScanResult.getFiles();
    }
    
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
    
    public void setIncludesExcludes() throws MojoExecutionException {
        try {
        	Field projectField = AbstractSurefireMojo.class.getDeclaredField("project");
            projectField.setAccessible(true);
            MavenProject accessedProject = (MavenProject) projectField.get(this);
            List<String> includes = PomUtil.getFromPom("include", accessedProject);
            List<String> excludes = PomUtil.getFromPom("exclude", accessedProject);
            logger.info("@@Excludes: " + excludes);
            logger.info("@@Includes: " + includes);
           
            setIncludes(includes);
            setExcludes(excludes);
        } catch (NoSuchFieldException nsfe) {
            nsfe.printStackTrace();
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
        }
    }
    
    public String getProjectName() {
    	String projectName = "XXX";
    	try {
    		Field projectField = AbstractSurefireMojo.class.getDeclaredField("project");
    		projectField.setAccessible(true);
            MavenProject accessedProject = (MavenProject) projectField.get(this);
            projectName = accessedProject.getArtifact().getArtifactId();
    	} catch (NoSuchFieldException nsfe) {
            nsfe.printStackTrace();
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
        }
    	
    	return projectName;
    }
    
}
