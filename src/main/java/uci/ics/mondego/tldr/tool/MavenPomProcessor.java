package uci.ics.mondego.tldr.tool;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;


public class MavenPomProcessor {
	private static final Logger logger = LogManager.getLogger(MavenPomProcessor.class);


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String projectLocation = args[0];
		String pomLocation = projectLocation+"/pom.xml";
		String pom2Location = projectLocation+"/pom2.xml";
		Reader reader = null;
		boolean changed = false;
		
		try {
			reader = new FileReader(pomLocation);
			
			MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
		
		    Model model = xpp3Reader.read(reader);
		    Build build = model.getBuild();
		    List<Plugin> oldPlugins = build.getPlugins();
		    List<Dependency> oldDependencies = model.getDependencies();
		    if(args[1].equals("surefire")){
		    	for(int i=0;i<oldPlugins.size();i++){
			    	if(oldPlugins.get(i).getArtifactId().equals("maven-surefire-plugin")){
			    		Writer writer = new FileWriter(pom2Location);
			    		MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
			    		oldPlugins.get(i).setVersion("2.19.1");
			    		build.setPlugins(oldPlugins);
					    /***** for single test run surefire 2.19.1 and junit 4.11/4.8.1 needed
					     * VERIFIED BY MANUAL LABOR SO ACCURATE
					     */
					    for(int j=0;i<oldDependencies.size();i++){
					    	if(oldDependencies.get(i).getArtifactId().equals("junit")){				    	
					    		if(oldDependencies.get(i).getVersion() == null ||
					    			oldDependencies.get(i).getVersion().length() == 0){
					    			oldDependencies.get(i).setVersion("4.11");
						    		model.setDependencies(oldDependencies);
					    		}
					    		else{
					    			String version = oldDependencies.get(i).getVersion();
					    			if(!(version.contains("4.11") || version.contains("4.12") ||
					    					version.contains("4.10") || version.contains("4.9") || 
					    					version.contains("4.8"))){
						    			oldDependencies.get(i).setVersion("4.8.1");
							    		model.setDependencies(oldDependencies);
					    			}
					    			else{
							    		model.setDependencies(oldDependencies);
					    			}

					    		}
					    	}
					    }
					    model.setBuild(build);
					    xpp3Writer.write( writer, model );
					    
					    writer.close();
					    changed = true;
					    break;
			    	}
			    }
		    }
		    else if(args[1].equals("ekstazi")){
		    	oldPlugins.add(createPlugin("org.ekstazi", 
		    			"ekstazi-maven-plugin", "5.2.0", "ekstazi", "select"));
		    	build.setPlugins(oldPlugins);
			    model.setBuild(build);
			    Writer writer = new FileWriter(pom2Location);
	    		MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
			    xpp3Writer.write( writer, model );
			    writer.close();
			    changed = true;
		    } 
		    
		    else if(args[1].equals("jar")){
		    	Plugin newPlugIn = createPlugin("org.apache.maven.plugins", 
		    			"maven-jar-plugin", "3.0.2",null, "test-jar");
		    	System.out.println(newPlugIn.getGroupId());
		    	oldPlugins.add(newPlugIn);
		    	build.setPlugins(oldPlugins);
			    model.setBuild(build);
			    Writer writer = new FileWriter(pom2Location);
	    		MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
			    xpp3Writer.write( writer, model );
			    writer.close();
			    changed = true;
		    }
		    
		    else if(args[1].equals("import")){
		        UpdateSelfPOM(pomLocation);   	
		    }
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (XmlPullParserException e) {
			e.printStackTrace();
		}	
		finally {
		    try {
				reader.close();
				if(changed){
					File pom = new File(pomLocation);
					if(pom.delete()){
						File newPom = new File(pom2Location);
						newPom.renameTo(new File(pomLocation));
					}
					else{
						logger.error("PROBLEM IS DELETING OLD POM FILE : "+pomLocation);
					}
				}
			} 
		    catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void UpdateSelfPOM(String otherProjectPom){
		String projectLocation = "/Users/demigorgan/Documents/workspace/tldr";
		String pomLocation = projectLocation+"/pom.xml";
		String pom2Location = projectLocation+"/pom2.xml";
		Reader readerSource = null;
		Reader readerTarget = null;
		
		try {
			readerSource = new FileReader(otherProjectPom);
			readerTarget = new FileReader(pomLocation);
			
			MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
		
		    Model sourceModel = xpp3Reader.read(readerSource);
		    Model targetModel = xpp3Reader.read(readerTarget);
		    boolean exists = false;
	
		    List<Dependency> oldDependencies = targetModel.getDependencies();
		    
		    for(int i=0;i<oldDependencies.size();i++){
		    	if(oldDependencies.get(i).getGroupId().equals(sourceModel.getGroupId()) &&
		    	  oldDependencies.get(i).getArtifactId().equals(sourceModel.getArtifactId())){
		    		exists = true;
		    		oldDependencies.get(i).setVersion(sourceModel.getVersion());
		    	}	
		    }
		    if(!exists){
		    	Dependency newDependencyTest = createDependency(sourceModel.getGroupId(), 
			    		sourceModel.getArtifactId(), sourceModel.getVersion(), "tests", "test-jar", null);
			    Dependency newDependencyMain = createDependency(sourceModel.getGroupId(), 
			    		sourceModel.getArtifactId(), sourceModel.getVersion(), null, null, null);
				    
			    oldDependencies.add(newDependencyMain);
			    oldDependencies.add(newDependencyTest);
		    }
		    
		    targetModel.setDependencies(oldDependencies);
    		Writer writerTarget = new FileWriter(pom2Location);
    		MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
    		xpp3Writer.write( writerTarget, targetModel);
    		readerSource.close();
    		readerTarget.close();
    		writerTarget.close();

		}
		catch(Exception e){
			
		}
		finally{
			File pom = new File(pomLocation);
			if(pom.delete()){
				File newPom = new File(pom2Location);
				newPom.renameTo(new File(pomLocation));
			}
			else{
				logger.error("PROBLEM IS DELETING OLD POM FILE : "+pomLocation);
			}			
		}
	}
	
	private static Dependency createDependency(String groupId, 
			String artifactId, String version, String classifier,
			String type, String scope){
		
		 Dependency dep = new Dependency();
		 dep.setGroupId(groupId);
		 dep.setArtifactId(artifactId);
		 dep.setVersion(version);
		 if(classifier != null)
			 dep.setClassifier(classifier);
		 if(type != null)
			 dep.setType(type);
		 if(scope != null)
			 dep.setScope(scope);
		 return dep;
		
	}
	
	private static Plugin createPlugin(String groupId, String artifactId, String version,
			String executionId, String goal){
		
		Plugin plugin = new Plugin();
		plugin.setGroupId(groupId);
		plugin.setArtifactId(artifactId);
		
		if(version != null)
			plugin.setVersion(version);
		
		PluginExecution ex = new PluginExecution();
		if(executionId != null)
			ex.setId(executionId);
		List<String> g = new ArrayList<String>();
		g.add(goal);
		ex.setGoals(g);
		List<PluginExecution> executions = new ArrayList<PluginExecution>();
		executions.add(ex);
		plugin.setExecutions(executions);
		
		return plugin;
	}
}
