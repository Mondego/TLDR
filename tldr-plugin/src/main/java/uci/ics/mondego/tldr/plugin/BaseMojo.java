/*
 * Copyright (c) 2015 - Present. The STARTS Team. All Rights Reserved.
 */

package uci.ics.mondego.tldr.plugin;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;

import uci.ics.mondego.tldr.TLDR;
import uci.ics.mondego.tldr.tool.PomUtil;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.surefire.AbstractSurefireMojo;
import org.apache.maven.plugin.surefire.ProviderInfo;
import org.apache.maven.plugin.surefire.SurefirePlugin;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.surefire.booter.Classpath;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.util.DefaultScanResult;

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
     * Optional experiment flag to turn on parallel retest-all. It 
     * expects values -- "true" and "false"
     */
    @Parameter(property = "parallel.retest.all", required = false, defaultValue = "false")
    protected String parallel_retest_all; 
    
    /**
     * Build directory of the project upon which the plugin is invoked
     */
    @Parameter(defaultValue = "${project.build.directory}")
    protected String projectBuildDir;

    @Parameter(defaultValue = "${basedir}")
    protected File basedir;

    /**
     * Optional flag to write Logs to a particular directory.
     */
    @Parameter(property = "log.directory", required = false, defaultValue = "XXXX")
    protected String log_directory;
    
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
    
    @SuppressWarnings("unchecked")
	public List<String> getTestClasses() {
        DefaultScanResult defaultScanResult = null;
        try {
            Method scanMethod = AbstractSurefireMojo.class.getDeclaredMethod("scanForTestClasses", null);
            scanMethod.setAccessible(true);
            defaultScanResult = (DefaultScanResult) scanMethod.invoke(this, null);
        } 
        catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
        } 
        catch (InvocationTargetException ite) {
            ite.printStackTrace();
        } 
        catch (IllegalAccessException iae) {
            iae.printStackTrace();
        }
        
        return (List<String>) defaultScanResult.getFiles();
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
    	} 
    	catch (NoSuchFieldException nsfe) {
            nsfe.printStackTrace();
        } 
    	catch (IllegalAccessException iae) {
            iae.printStackTrace();
        }
    	
    	return projectName;
    }
    
    protected void printResult() {
		
    	try {
            Method createProvidersMethod = 
            		SurefirePlugin.class.getDeclaredMethod("createProviders", null);
            createProvidersMethod.setAccessible(true);                        

            List<ProviderInfo> provideInfos = (List<ProviderInfo>)createProvidersMethod.invoke(this);
                           
            Method executeProviderMethod = 
            		AbstractSurefireMojo.class.getDeclaredMethod("executeProvider", null);
            executeProviderMethod.setAccessible(true);
            RunResult runResult = 
            		(RunResult) executeProviderMethod.invoke(this, provideInfos.get(3));
		} 
		catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
        } 
		catch (InvocationTargetException ite) {
            ite.printStackTrace();
        } 
		catch (IllegalAccessException iae) {
            iae.printStackTrace();
        }
	}	
}
