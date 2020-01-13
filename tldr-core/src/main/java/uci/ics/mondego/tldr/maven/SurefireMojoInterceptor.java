package uci.ics.mondego.tldr.maven;

import java.util.logging.Level;

import uci.ics.mondego.tldr.tool.Constants;

public final class SurefireMojoInterceptor extends AbstractMojoInterceptor {
    static final String UNSUPPORTED_SUREFIRE_VERSION_EXCEPTION = "Unsupported surefire version. ";

    public static void execute(Object mojo) throws Exception {
        if (!isSurefirePlugin(mojo)) {
            return;
        }
        if (isAlreadyInvoked(mojo)) {
            return;
        }
        checkSurefireVersion(mojo);
        try {
        	updateTests(mojo);
        } catch (Exception ex) {
            throwMojoExecutionException(mojo, UNSUPPORTED_SUREFIRE_VERSION_EXCEPTION, ex);
        }
    }

    private static boolean isSurefirePlugin(Object mojo) throws Exception {
        return mojo.getClass().getName().equals(Constants.SUREFIRE_PLUGIN_BIN);
    }

    private static boolean isAlreadyInvoked(Object mojo) throws Exception {
        String key = Constants.TLDR_NAME + System.identityHashCode(mojo);
        String value = System.getProperty(key);
        System.setProperty(key, "STARTS-invoked");
        return value != null;
    }

    private static void checkSurefireVersion(Object mojo) throws Exception {
        try {
            getField(Constants.TEST_FIELD, mojo);
        } catch (NoSuchMethodException ex) {
            throwMojoExecutionException(mojo, UNSUPPORTED_SUREFIRE_VERSION_EXCEPTION
                     + "Try setting Tests in the surefire configuration.", ex);
        }
    }
    
    private static void updateTests(Object mojo) throws Exception {
        logger.log(Level.FINE, "updating Excludes");
        String currentTests = getStringField(Constants.TEST_FIELD, mojo);
        String newTests = System.getProperty(Constants.TLDR_TEST_PROPERTY);
        if (currentTests != null) {
        	newTests += (Constants.COMMA + currentTests);
        } 
        
        setField(Constants.TEST_FIELD, mojo, newTests);
    }
}
