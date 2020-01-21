package uci.ics.mondego.tldr.maven;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import uci.ics.mondego.tldr.tool.Constants;

public abstract class AbstractMojoInterceptor {

    protected static final Logger logger = Logger.getGlobal();

    public static URL extractJarURL(URL url) throws IOException {
        JarURLConnection connection = (JarURLConnection) url.openConnection();
        return connection.getJarFileURL();
    }

    public static URL extractJarURL(Class<?> clz) throws IOException {
        return extractJarURL(getResource(clz));
    }

    public static URL getResource(Class<?> clz) {
        URL resource = clz.getResource(
        		"/" + clz.getName().replace('.', File.separatorChar) + Constants.CLASS_EXTENSION);
        return resource;
    }

    protected static void setField(
    		String fieldName, Object mojo, Object value) throws Exception {
        Field field;
        try {
            field = mojo.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            try {
            	field = mojo
            			.getClass()
            			.getSuperclass()
            			.getDeclaredField(fieldName);
            } catch (NoSuchFieldException noSuchFileException) {
            	field = mojo
            			.getClass()
            			.getSuperclass()
            			.getSuperclass()
            			.getDeclaredField(fieldName);
            }
        }
        field.setAccessible(true);        
        field.set(mojo, value);
    }
    
    protected static Object getField(String fieldName, Object mojo) throws Exception {
        Field field;
        try {
            field = mojo.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            field = mojo.getClass().getSuperclass().getDeclaredField(fieldName);
        }
        field.setAccessible(true);
        return field.get(mojo);
    }

    protected static String getStringField(
    		String fieldName, Object mojo) throws Exception {
        return (String) getField(fieldName, mojo);
    }
}
