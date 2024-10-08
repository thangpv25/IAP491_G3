import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public class ClassExtractorAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Agent attached.");

        Class<?>[] loadedClasses = inst.getAllLoadedClasses();
        ClassPool classPool = ClassPool.getDefault();

        for (Class<?> clazz : loadedClasses) {
            if (clazz != null) {
                try {
                    // Get class 
                    String className = clazz.getName();
                    System.out.println("Extracting class: " + className);
                    CtClass ctClass = classPool.get(className);
                    saveBytecodeToFile(ctClass, className);
                } catch (NotFoundException e) {
                    System.err.println("Class not found: " + e.getMessage());
                } catch (IOException e) {
                    System.err.println("Error writing bytecode: " + e.getMessage());
                }
            }
        }
    }

    private static void saveBytecodeToFile(CtClass ctClass, String className) throws IOException {
        String filePath = "output/" + className.replace('.', '/') + ".class";
        File outputFile = new File(filePath);
        outputFile.getParentFile().mkdirs(); 

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(ctClass.toBytecode());
            System.out.println("Bytecode saved to: " + outputFile.getAbsolutePath());
        }
    }
}