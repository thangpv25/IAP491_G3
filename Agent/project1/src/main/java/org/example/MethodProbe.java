package org.example;

import javassist.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MethodProbe {
    public static final Map<String, String> suspiciousClassAndMethod = new HashMap<String, String>() {{
//        put("AbstractHandlerMethodMapping", "registerMapping");
//        put("AbstractUrlHandlerMapping", "registerHandler");
//        put("Field", "get");
        put("FilterDef", "setFilterName");
//        put("Runtime", "exec");
//        put("StandardContext", Arrays.toString(new String[]{"addApplicationEventListener", "addServletMappingDecoded"}));
//        put("Testing", "printing");
        put("StringUtils", "toLowerCase");
    }};

    public void extractClass() {

    }

    public static byte[] mainProbe(ClassPool classPool, String targetClassName) throws Exception{
        System.out.println("Probe executed");
        CtClass ctClazz = null;
        CtMethod ctMethod = null;
        String fullPathClassName = targetClassName.replace("/", ".");  // Remove the "/" in the className. Ex: Utils/StringUtils -> Utils.StringUtils;
        String onlyClassName = targetClassName.split("/")[1];  // Get only the class name, not package Ex: Utils/StringUtils -> StringUtils
        String insertedCode = "{ " +
                "System.out.println(\"Probe Inject code executed\"); " +
                "StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();" +
                "for (int i = 2; i < stackTrace.length; i++) { " +  // Skip the first two elements
                "    System.out.println(stackTrace[i].toString()); " +
                "}" +
                "}";
        try {
            ctClazz = classPool.get(fullPathClassName);
            ctMethod = ctClazz.getDeclaredMethod(suspiciousClassAndMethod.get(onlyClassName));
            System.out.println("CtClass: " + ctClazz);
            System.out.println("CtMethod: " + ctMethod);
            ctMethod.insertAfter(insertedCode);
            ctClazz.defrost();
            byte[] byteCode = ctClazz.toBytecode();
            ctClazz.detach();
            return byteCode;
        }
        catch (NotFoundException | CannotCompileException | IOException e) {
            throw new RuntimeException(e);
        }
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

}
