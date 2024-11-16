package test.taint;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;

public class BytecodeUtils {

    // Define the class from the bytecode and return it as a Class object
    public static Class<?> defineClassFromBytecode(byte[] bytecode) throws Exception {
        // Extract the class name from the bytecode
        String className = getClassNameFromBytecode(bytecode);

        // Using a custom class loader to define the class from bytecode
        ClassLoader customClassLoader = new ClassLoader(BytecodeUtils.class.getClassLoader()) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                try {
                    // Use reflection to invoke defineClass (which is protected)
                    Method defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
                    defineClassMethod.setAccessible(true);  // Make the method accessible
                    return (Class<?>) defineClassMethod.invoke(this, name, bytecode, 0, bytecode.length);
                } catch (Exception e) {
                    throw new ClassNotFoundException("Failed to define class from bytecode", e);
                }
            }
        };

        // Define the class using the name extracted from bytecode
        return customClassLoader.loadClass(className); // Load class with the name extracted from bytecode
    }

    // Method to extract the class name from the bytecode using ASM
    private static String getClassNameFromBytecode(byte[] bytecode) throws Exception {
        ClassReader reader = new ClassReader(bytecode);
        return reader.getClassName().replace('/', '.'); // Convert internal name to class name (using dots instead of slashes)
    }
}