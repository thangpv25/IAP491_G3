package org.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public class Agent {
    static String[] suspiciousClassName = new String[]{"AbstractHandlerMethodMapping", "ProcessBuilder", "Field", 
            "FilterDef", "StandardContext"};

    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("Agent attached.");
        // inst.addTransformer(new DefineTransformer(), true);
        Class<?>[] loadedClasses = inst.getAllLoadedClasses();
        ClassPool classPool = ClassPool.getDefault();

        for (Class<?> clazz : loadedClasses) {
            boolean isSuspiciousClass = false;
            if (clazz != null) {
                try {
                    // Get class
                    String className = clazz.getName();
                    for (String malClassName : suspiciousClassName) {
                        if (className.contains(malClassName)) {
                            isSuspiciousClass = true;
                            break;
                        }
                    }
                    if (isSuspiciousClass) {
                        CtClass ctClass = classPool.get(className);
//                        System.out.println("CT class: " + ctClass.toString());
//                        saveBytecodeToFile(ctClass, className);
                        System.out.println("Suspicious class: " + clazz.getName());
                    }
//                } catch (NotFoundException e) {
//                    System.err.println("Class not found: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Error writing bytecode: " + e.getMessage());
                }
//
                }
            }
        }


    //static class DefineTransformer implements ClassFileTransformer {
//    @Override
//    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
//        System.out.println("Defining class: " + className);
//        return classfileBuffer;
//    }
//}
    private static void saveBytecodeToFile(CtClass ctClass, String className) throws IOException {
        String filePath = "output/" + className.replace('.', '/') + ".class";
        File outputFile = new File(filePath);
        outputFile.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(ctClass.toBytecode());
            System.out.println("Bytecode saved to: " + outputFile.getAbsolutePath());
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }
}
