package uci.ics.mondego.tldr.tool;

/**
 * All constant Strings that are being used across TLDR. 
 * @author demigorgan
 *
 */
public class Constants {
	
	public static final String SUREFIRE_PLUGIN_VM = "org/apache/maven/plugin/surefire/SurefirePlugin";
	public static final String SUREFIRE_PLUGIN_BIN = "org.apache.maven.plugin.surefire.SurefirePlugin";

	public static final String ABSTRACT_SUREFIRE_MOJO_VM = "org/apache/maven/plugin/surefire/AbstractSurefireMojo";
	public static final String ABSTRACT_SUREFIRE_MOJO_BIN = "org.apache.maven.plugin.surefire.AbstractSurefireMojo";
	public static final String SUREFIRE_INTERCEPTOR_CLASS_VM = "uci/ics/mondego/tldr/maven/SurefireMojoInterceptor";
	
	public static final String EXECUTE_MNAME = "execute";
	public static final String EXECUTE_MDESC = "()V";

	public static final String TLDR_NAME = "TLDR";

	public static final String TEST_FIELD = "test";

	public static final String TLDR_TEST_PROPERTY = "TLDR_TESTS";
    public static String MOJO_EXECUTION_EXCEPTION_BIN = "org.apache.maven.plugin.MojoExecutionException";

    // simple characters:
    public static final Character DOT = '.';
    public static final String EMPTY = "";
    public static final String COMMA = ",";
    public static final String SEMI_COLON = ";";
    public static final String HYPHEN = "-";
    public static final String STAR = "*";
    public static final String NEW_LINE = "\n";
    
    public static final String CLASSES = "classes";
    public static final String JAVA_HOME = "java.home";
    public static final String SF_CLASSPATH = "sf-classpath";
    public static final String TEST_CLASSES = "test-classes";
    public static final String JAR_CHECKSUMS = "jar-checksums";
    public static final String CHANGED_CLASSES = "changed-classes";
    public static final String CHECK_IF_ALL_AFFECTED = "checkIfAllAffected";

    public static final String JAR_EXTENSION = ".jar";
    public static final String CLASS_EXTENSION = ".class";
    public static final String PROJECT_ID = "PROJECT_ID";

}