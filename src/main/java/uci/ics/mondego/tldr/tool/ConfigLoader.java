package uci.ics.mondego.tldr.tool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

	private static InputStream inputStream;
	private String CLASS_DIR;
	private String TEST_DIR;
	private int thread;
 
	public ConfigLoader() {
		try {
			Properties prop = new Properties();
			String propFileName = "config.properties";
			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
 
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException
				    ("property file '" + propFileName + "' not found in the classpath");
			}
 
			CLASS_DIR = prop.getProperty("CLASS_DIR");
			TEST_DIR = prop.getProperty("TEST_DIR");
			thread = Integer.parseInt(prop.getProperty("THREAD_COUNT"));
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getCLASS_DIR() {
		return CLASS_DIR;
	}

	public String getTEST_DIR() {
		return TEST_DIR;
	}

	public int getThread() {
		return thread;
	}
}
