package org.example;

import javassist.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import java.util.Map;


import static org.example.MethodProbe.suspiciousClassAndMethod;


public class TestAgent {
//    private static Instrumentation global_inst = null;

    static class DefineTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
            boolean isSuspiciousClass = false;
            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
            System.out.println("ClassName: " + className);
            System.out.println("Classloader: " + loader);
//
//            if (contextLoader != null && contextLoader != loader) {
//                try {
//                    Class<?> clazz = contextLoader.loadClass(className.replace('/', '.'));
//                    className = clazz.getName();
//                    // Perform your transformation here
//                    for (Map.Entry<String, String> malClassName : suspiciousClassAndMethod.entrySet()) {
//                        if (className.contains(malClassName.getKey())) {
//                            isSuspiciousClass = true;
//                            break;
//                        }
//                    }
//                    if (isSuspiciousClass) {
//                        System.out.println("Suspicious class: " + className);
//                        ClassPool classPool = ClassPool.getDefault();
//                        String targetMethodName = suspiciousClassAndMethod.get(className.split("/")[1]); // get the class only, not package
//                        try {
//                            return MethodProbe.mainProbe(classPool, className);
//                        } catch (Exception e) {
//                            System.out.println("Exception in main probe: " + e.getMessage());
//                        }
//                    }
//                    return classfileBuffer;
//        } catch (ClassNotFoundException e) {
//            // Class not found in context loader, fall back to original loader
//        }

                    for (Map.Entry<String, String> malClassName : suspiciousClassAndMethod.entrySet()) {
                        if (className.contains(malClassName.getKey())) {
                            isSuspiciousClass = true;
                            break;
                        }
                    }
            System.out.println("isSuspiciousClass: " + isSuspiciousClass);
                    if (isSuspiciousClass) {
                        System.out.println("Suspicious class: " + className);
                        ClassPool classPool = ClassPool.getDefault();
                        String targetMethodName = suspiciousClassAndMethod.get(className.split("/")[1]); // get the class only, not package
                        try {
                            return MethodProbe.mainProbe(classPool, className);
                        } catch (Exception e) {
                            System.out.println("Exception in main probe: " + e.getMessage());
                        }
                    }
                    return classfileBuffer;
        }
        }

        public static void premain(String agentArgs, Instrumentation inst) {
            System.out.println("Premain executed: Test Agent attached.");
            if (!inst.isRetransformClassesSupported()) {
                System.out.println("Class retransformation is not supported.");
                return;
            }
            inst.addTransformer(new DefineTransformer(), true);

        }

        public static void agentmain(String agentArgs, Instrumentation inst) {
            System.out.println("Agentmain executed: Test Agent attached.");
            System.out.println("This agent class loader: " + TestAgent.class.getClassLoader());
            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
            System.out.println("Ctx Loader: " + contextLoader);
            if (!inst.isRetransformClassesSupported()) {
                System.out.println("Class retransformation is not supported.");
                return;
            }
            inst.addTransformer(new DefineTransformer(), true);

//        global_inst = inst;
            // printLoadedClass(global_inst);

        }

        public static void printLoadedClass(Instrumentation inst) {
            System.out.println("All loaded classes: ");
            for (Class<?> clazz : inst.getAllLoadedClasses()) {
                if (clazz != null) {
                    try {
                        // Get class
//                    if (clazz.getPackage().toString().contains("org.example")) {
//                        String className = clazz.toString();
//                        System.out.println(className);
//                    }
                        System.out.println("Class: " + clazz.getName() + ", Class Loader: " + clazz.getClassLoader());
                    } catch (Exception e) {
                        System.err.println("Error finding class: " + e.getMessage());
                    }
//
                }
            }

        }


    }