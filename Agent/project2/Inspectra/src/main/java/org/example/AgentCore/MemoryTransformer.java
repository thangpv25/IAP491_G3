package org.example.AgentCore;

import javassist.*;
import org.example.Loader.AgentCache;
import org.example.Loader.CustomClassLoader;
import org.example.Utils.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.*;


public class MemoryTransformer implements ClassFileTransformer {
    public static final Map<String, String> suspiciousClassAndMethod = new HashMap<String, String>() {{
//        put("AbstractHandlerMethodMapping", "registerMapping");
//        put("AbstractUrlHandlerMapping", "registerHandler");
//        put("Field", "get");
        put("FilterDef", "setFilterName");
        put("Runtime", "exec");
        put("ProcessBuilder", "start");
        put("StandardContext", Arrays.toString(new String[]{"addApplicationEventListener", "addServletMappingDecoded", "addServletMapping"}));
        put("Testing", "runCal");
    }};
    private static List<StackTraceElement[]> stackTraceList = new ArrayList<>();
    private static CustomClassLoader classLoader;
    public static String name2 = "duc";
    private final AgentCache agentCache;

     MemoryTransformer(AgentCache agentCache) {
        this.agentCache = agentCache;
    }
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        boolean isSuspiciousClass = false;
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        System.out.println("ClassName: " + className);
        System.out.println("Classloader: " + loader);
        System.out.println("HAHA3333");
        String onlyClassName = className.substring(className.lastIndexOf("/") + 1);
        for (String malClassName : suspiciousClassAndMethod.keySet()) {
            if (onlyClassName.equals(malClassName)) {
                isSuspiciousClass = true;
                break;
            }
        }
        if (isSuspiciousClass) {
            try {
                System.out.println("Suspicious class: " + className);
//                    ClassPool.getDefault().insertClassPath(new ClassClassPath(classBeingRedefined));
                ClassPool classPool = ClassPool.getDefault();
                String targetMethodName = suspiciousClassAndMethod.get(onlyClassName); // get the class only, not package
                System.out.println("targetMethodName: " + targetMethodName);

                return mainProbe(classPool, className, targetMethodName);
            } catch (Exception e) {
                System.out.println("Exception in main probe: " + e.getMessage());
            }
        }
        System.out.println("END");
        return classfileBuffer;
    }
    public byte[] mainProbe(ClassPool classPool, String targetClassName, String targetMethodName) throws Exception {
        System.out.println("Probe executed");
        CtClass ctClazz = null;
        CtMethod ctMethod = null;
        String fullPathClassName = targetClassName.replace("/", ".");  // Remove the "/" in the className. Ex: Utils/StringUtils -> Utils.StringUtils;
        String onlyClassName = targetClassName.split("/")[1];  // Get only the class name, not package Ex: Utils/StringUtils -> StringUtils
        String insertedCode = "{ " +
                "System.out.println(\"PROBE INJECT CODE EXECUTED\"); " +
                "System.out.println(\"Stack trace: \"); " +
                "StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();" +
                "for (int i = 0; i < stackTrace.length; i++) { " +
                "    System.out.println(stackTrace[i]);" +
                "}" +
//                "boolean addSuccess = org.example.AgentCore.MemoryTransformer.addStackTrace(stackTrace);"+
//                "if (addSuccess) {" +
//                "System.out.println(\"Stack trace added successfully.\");" +
//                "} else {" +
//                "System.out.println(\"Failed to add stack trace.\");}" +
                "System.out.println(\"END OF PROBE INJECT CODE\"); " +
                "}";
//                "    sb.append(stackTrace[i].toString()).append(\\\"\\\\n\\\");" +
        this.agentCache.getModifiedClass().add(targetClassName);
        List<String> targetMethodList = new ArrayList<>();

        if (targetMethodName.contains("[") && targetMethodName.contains(",")) {
            String[] targetMethodString = StringUtils.convertToStringArray(targetMethodName);
            targetMethodList.addAll(Arrays.asList(targetMethodString));
        } else {
            targetMethodList.add(targetMethodName);
        }
        for (String methodName : targetMethodList) {
            try {
                ctClazz = classPool.get(fullPathClassName);
                ctMethod = ctClazz.getDeclaredMethod(methodName);
                System.out.println("CtClass: " + ctClazz);
                System.out.println("CtMethod: " + ctMethod);
                ctMethod.insertAfter(insertedCode);
//                ctClazz.defrost();
                System.out.println("SStack trace: ");
                for (StackTraceElement[] stack : stackTraceList) {
                    for (StackTraceElement stackTraceElement : stack) {
                        System.out.println(stackTraceElement);
                    }
                }
                byte[] byteCode = ctClazz.toBytecode();
                ctClazz.detach();
                return byteCode;
            } catch (NotFoundException | CannotCompileException | IOException e) {
                throw new RuntimeException(e);
            }

        }

        return new byte[0];
    }
    public static void print1(){
        System.out.println("HELLO WORD! THIS IS CLASS TRANSFORMER. ");

    }
    public static void print(String str){
        System.out.println("HELLO WORD! THIS IS CLASS TRANSFORMER. Hello: "+str);

    }
    public static void test(Instrumentation inst,  AgentCache agentCache) {
        System.out.println("Method test executed!");
//         classLoader = loader;
        System.out.println("Worker classloader is " + Bootstrap.class.getClassLoader());
        ClassFileTransformer memoryTransformer = new MemoryTransformer(agentCache);
        agentCache.getTransformers().add(memoryTransformer);

        inst.addTransformer(memoryTransformer, true);
    }
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

    public static boolean addStackTrace(StackTraceElement[] stackTrace) {
        int initialSize = stackTraceList.size(); // Get initial size

        stackTraceList.add(stackTrace); // Add the stack trace

        // Check if the size has increased
        return stackTraceList.size() > initialSize;
    }

    // Method to retrieve stack traces for further processing
    public static List<StackTraceElement[]> getStackTraces() {
        return stackTraceList;
    }
}