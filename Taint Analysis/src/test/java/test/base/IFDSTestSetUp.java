package test.base;

import boomerang.scene.jimple.BoomerangPretransformer;
import soot.*;
import soot.jimple.toolkits.ide.JimpleIFDSSolver;
import soot.options.Options;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class IFDSTestSetUp {

    protected static JimpleIFDSSolver<?, ?> solver = null;

    protected JimpleIFDSSolver<?, ?> executeStaticAnalysis(String targetTestClassName) {
        setupSoot(targetTestClassName);
        registerSootTransformers();
        executeSootTransformers();
        if (solver == null) {
            throw new NullPointerException("Something went wrong solving the IFDS problem!");
        }
        return solver;
    }

    private void executeSootTransformers() {
        //Apply all necessary packs of soot. This will execute the respective Transformer
        PackManager.v().getPack("cg").apply();
        // Must have for Boomerang
        BoomerangPretransformer.v().reset();
        BoomerangPretransformer.v().apply();
        PackManager.v().getPack("wjtp").apply();
    }

    private void registerSootTransformers() {
        Transform transform = new Transform("wjtp.ifds", createAnalysisTransformer());
        PackManager.v().getPack("wjtp").add(transform);
    }

    protected abstract Transformer createAnalysisTransformer();

    private String findJavaHome() {
        File jdk11 = new File(System.getProperty("java.home"));
        File jdksDirectory = jdk11.getParentFile();

        if (jdksDirectory.exists() && jdksDirectory.isDirectory()) {
            File[] directories = jdksDirectory.listFiles(File::isDirectory);
            if (directories != null) {
                for (File dir : directories) {
                    if (dir.getName().contains("1.8.0")) {
                        return dir.getAbsolutePath();  // Return the path of JDK 1.8.0
                    }
                }
            }
        }
        return null;  // Return null if no JDK 1.8.0 is found
    }

    /*
	 * This method provides the options to soot to analyse the respecive
	 * classes.
     */
    private void setupSoot(String targetTestClassName) {
        G.reset();
        String userdir = System.getProperty("user.dir");
        String javaHome = findJavaHome();

        // Set the classpath for Java 11. Use the jmods folder instead of rt.jar
        String sootCp = userdir + File.separator + "target" + File.separator + "test-classes"
                + File.pathSeparator + javaHome + File.separator + "jre\\lib" + File.separator + "rt.jar";

        // Set the Soot classpath
        Options.v().set_soot_classpath(sootCp);

        // Perform a whole program analysis (interprocedural analysis)
        Options.v().set_whole_program(true);
        Options.v().setPhaseOption("cg.cha", "on");
        Options.v().setPhaseOption("cg", "all-reachable:true");

        // Configure Soot options
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().setPhaseOption("jb.ls", "enabled:false");
        Options.v().set_prepend_classpath(false);

        // Add necessary classes for the analysis
        Scene.v().addBasicClass("java.lang.StringBuilder");
        Scene.v().addBasicClass("java.lang.Object", SootClass.SIGNATURES);  // Object is fundamental
        Scene.v().addBasicClass("java.lang.ProcessBuilder", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.Runtime", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.reflect.Method", SootClass.SIGNATURES);

        // Resolve and set the target class
        SootClass c = Scene.v().forceResolve(targetTestClassName, SootClass.BODIES);
        if (c != null) {
            c.setApplicationClass();
        }

        // Load necessary classes
        Scene.v().loadNecessaryClasses();
    }


    protected List<SootMethod> getEntryPointMethods() {
        List<SootMethod> entryPoints = new ArrayList<>();
        for (SootClass c : Scene.v().getApplicationClasses()) {
            for (SootMethod m : c.getMethods()) {
                if (m.hasActiveBody()) {
                    // Add all methods with active bodies as entry points
                    entryPoints.add(m);
                }
            }
        }
        return entryPoints;
    }

}
