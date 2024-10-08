import soot.*;
import soot.options.Options;
import soot.jimple.JimpleBody;

import java.util.Collections;

public class BytecodeToJimpleConverter {
    public static void main(String[] args) {
        // Ensure we have an input class file to convert
        if (args.length < 1) {
            System.out.println("Usage: java -cp .:soot-4.3.0-jar-with-dependencies.jar BytecodeToJimpleConverter <target-class>");
            return;
        }
        G.reset();
        Options.v().set_prepend_classpath(true);
        Options.v().set_soot_classpath("target/classes:" + Scene.v().defaultClassPath());
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_process_dir(Collections.singletonList(args[0]));
        Options.v().set_allow_phantom_refs(true);
        SootClass sootClass = Scene.v().loadClassAndSupport(args[0]);
        sootClass.setApplicationClass();
        Scene.v().loadNecessaryClasses();

        for (SootMethod method : sootClass.getMethods()) {
            if (method.isConcrete()) {
                Body body = method.retrieveActiveBody();
                if (body instanceof JimpleBody) {
                    System.out.println("Jimple for method: " + method.getName());
                    System.out.println(body);
                }
            }
        }
        
        PackManager.v().writeOutput();
    }
}