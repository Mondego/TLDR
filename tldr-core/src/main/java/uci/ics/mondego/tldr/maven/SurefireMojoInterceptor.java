package uci.ics.mondego.tldr.maven;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import uci.ics.mondego.tldr.TLDR;
import uci.ics.mondego.tldr.exception.TLDRMojoExecutionException;
import uci.ics.mondego.tldr.tool.Constants;
import uci.ics.mondego.tldr.tool.ReportWriter;

public final class SurefireMojoInterceptor extends AbstractMojoInterceptor {
    static final String UNSUPPORTED_SUREFIRE_VERSION_EXCEPTION = "Unsupported surefire version. ";
	private static ReportWriter reportWriter = new ReportWriter();
    
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
        	reportWriter.logError(
        			System.getProperty(Constants.LOG_DIRECTORY) + Constants.SLASH + "ERROR.txt", 
        			"tests were not updated in SureFire ", 
        			SurefireMojoInterceptor.class.getName());
        	throw new TLDRMojoExecutionException(UNSUPPORTED_SUREFIRE_VERSION_EXCEPTION);
        }
    }

    private static boolean isSurefirePlugin(Object mojo) throws Exception {
        return mojo.getClass().getName().equals(Constants.SUREFIRE_PLUGIN_BIN);
    }

    private static boolean isAlreadyInvoked(Object mojo) throws Exception {
        String key = Constants.TLDR_NAME + System.identityHashCode(mojo);
        String value = System.getProperty(key);
        System.setProperty(key, "TLDR-invoked");
        return value != null;
    }

    private static void checkSurefireVersion(Object mojo) throws Exception {
    	
    	try {
            getField(Constants.TEST_FIELD, mojo);
        } catch (NoSuchMethodException ex) {
        	reportWriter.logError(
        			System.getProperty(Constants.LOG_DIRECTORY) + Constants.SLASH + "ERROR.txt", 
        			"test field not found in Surefire", 
        			SurefireMojoInterceptor.class.getName());
        	throw new TLDRMojoExecutionException(
        			UNSUPPORTED_SUREFIRE_VERSION_EXCEPTION
                    + "Try setting Tests in the surefire configuration.");
        }
    }
    
    private static void updateTests(Object mojo) throws Exception {
        logger.log(Level.FINE, "updating Tests");
        String testsToRun = System.getProperty(Constants.TLDR_TEST_PROPERTY);
        
        if(testsToRun ==  null || testsToRun.length() == 0) {
        	if (System.getProperty(Constants.FIRST_TIME).equals(Constants.TRUE)) {
    			System.out.println(Constants.DISTINCTION_LINE_STAR);
        		System.out.println(" ****** NO TEST SELECTED ********** ");
    			System.out.println(Constants.DISTINCTION_LINE_STAR);
        	}
        	
        	List<String> excludes = new ArrayList<String>();
        	excludes.add(Constants.ALL_TEST_REGEX);
        	setField(Constants.EXCLUDES_FIELD, mojo, excludes);
        	//setField(Constants.TEST_FIELD, mojo, Constants.EMPTY);

        } else {        	

        	if (System.getProperty(Constants.FIRST_TIME).equals(Constants.TRUE)) {
        		if (System.getProperty(Constants.DEBUG_FLAG).equals(Constants.TRUE)) {
        			System.out.println(Constants.DISTINCTION_LINE_STAR);
        			System.out.println( "FIRST TIME SO ALL THE TESTS ARE RUNNING");
                	System.out.println(Constants.DISTINCTION_LINE_STAR);
        		}
        	} else {
        		
        		if (System.getProperty(Constants.DEBUG_FLAG).equals(Constants.TRUE)) {
                	System.out.println("Setting test Field to ");
                	System.out.println(Constants.DISTINCTION_LINE_STAR);
                	System.out.println(Constants.DISTINCTION_LINE_STAR);
                	System.out.println(testsToRun);
                	System.out.println(Constants.DISTINCTION_LINE_STAR);
                	System.out.println(Constants.DISTINCTION_LINE_STAR);
        		}
        		
            	setField(Constants.TEST_FIELD, mojo, testsToRun);
        	}
        }       
    }
}
