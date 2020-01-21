package uci.ics.mondego.tldr.maven;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

import uci.ics.mondego.tldr.tool.Constants;


public class JavaAgent {
    public static void agentmain(String options, Instrumentation instrumentation) {
        instrumentation.addTransformer(new MavenCFT(), true);
        instrumentMaven(instrumentation);
    }

    private static void instrumentMaven(Instrumentation instrumentation) {
        try {
            for (Class<?> clz : instrumentation.getAllLoadedClasses()) {
                String name = clz.getName();
                if (name.equals(
                		Constants.ABSTRACT_SUREFIRE_MOJO_BIN) 
                		|| name.equals(Constants.SUREFIRE_PLUGIN_BIN)) {
                    instrumentation.retransformClasses(clz);
                }
            }
        } catch (UnmodifiableClassException uce) {
            uce.printStackTrace();
        }
    }
}
