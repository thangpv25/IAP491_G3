package org.example;

import fj.data.Set;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class ConvertJimple {

    public static void main(String[] args) {
        AnalysisInputLocation inputLocation =
                new JavaClassPathAnalysisInputLocation("target/classes");
        JavaView view = new JavaView(inputLocation);


        // Load the main class
        ClassType mainClassType = JavaIdentifierFactory.getInstance().getClassType("org.example.Main");
        //System.out.println(mainClassType);
        JavaSootClass mainClass = view.getClass(mainClassType).orElseThrow(() -> new RuntimeException("Main class not found"));
        System.out.println(mainClass);
        MethodSignature methodSignature = view.getIdentifierFactory().getMethodSignature(mainClassType,"main","void",Collections.singletonList("java.lang.String[]")); // args
        MethodSubSignature mss = methodSignature.getSubSignature();
        Optional<JavaSootMethod> opt = mainClass.getMethod(mss);
        if(opt.isPresent()){
            JavaSootMethod method = opt.get();
            System.out.println(method.getBody());
            System.out.println(view.getClasses());


        }
        Collection<JavaSootClass> classes = view.getClasses();
        for (SootClass sootClass : classes) {
            //System.out.println("Class: " + sootClass.getName());

            // Iterate over each method in the class
            for (SootMethod sootMethod : sootClass.getMethods()) {
                //System.out.println(" Method: " + sootMethod.getSignature());

                // Retrieve and print the method's Jimple body if it's present and concrete
                if (sootMethod.isConcrete()) {
                    Body jimpleBody = sootMethod.getBody();
                    System.out.println(jimpleBody);

                }
                }
            }

        System.out.println("Conversion complete.");
    }
}
