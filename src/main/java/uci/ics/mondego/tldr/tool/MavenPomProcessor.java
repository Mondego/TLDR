package uci.ics.mondego.tldr.tool;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
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
		    for(int i=0;i<oldPlugins.size();i++){
		    	if(oldPlugins.get(i).getArtifactId().equals("maven-surefire-plugin")){
		    		System.out.println("here");
		    		Writer writer = new FileWriter(pom2Location);
		    		MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
		    		oldPlugins.get(i).setVersion("2.12.1");
		    		build.setPlugins(oldPlugins);
				    model.setBuild(build);
				    xpp3Writer.write( writer, model );
				    writer.close();
				    changed = true;
				    break;
		    	}
		    }
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
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
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
