package uci.ics.mondego.tldr.maven;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import uci.ics.mondego.tldr.exception.TLDRMojoExecutionException;
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
        	throw new TLDRMojoExecutionException(UNSUPPORTED_SUREFIRE_VERSION_EXCEPTION);
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
        	throw new TLDRMojoExecutionException(
        			UNSUPPORTED_SUREFIRE_VERSION_EXCEPTION
                    + "Try setting Tests in the surefire configuration.");
        }
    }
    
    private static void updateTests(Object mojo) throws Exception {
        logger.log(Level.FINE, "updating Tests");
        String testsToRun = System.getProperty(Constants.TLDR_TEST_PROPERTY);
        
        if(testsToRun ==  null || testsToRun.length() == 0) {
        	List<String> excludes = new ArrayList<String>();
        	excludes.add(Constants.ALL_TEST_REGEX);
        	setField(Constants.EXCLUDES_FIELD, mojo, excludes);
        } else {        	
        	if (System.getProperty(Constants.PARALLEL_RETEST_ALL).equals(Constants.TRUE)) {
        		// Set up parallel test running configuration.
            	setField(Constants.PARALLEL_FIELD, mojo, "all");
            	setField(Constants.THREAD_COUNT_FIELD, mojo, 8);
            	setField(Constants.REDIRECT_TEST_OUTPUT_TO_FILE_FIELD, mojo, false);
        	}
        	
        	// Set up the selected test methods to run.
        	setField(Constants.TEST_FIELD, mojo, testsToRun);
        }       
    }
}
