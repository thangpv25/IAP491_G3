package org.example;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

import java.lang.reflect.Method;
import java.util.Scanner;

public class Testing {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.println("Hello and welcome!");
        long pid = ProcessHandle.current().pid();
        // Print the PID
        System.out.println("The PID of this JVM process is: " + pid);
//        System.out.println("Current Class: "+ Testing.class);

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter your favourite number: ");
        int num = sc.nextInt();
        System.out.printf("\nYour favourite number is %d.", num);
        printing();
//        insertMethod();
    }

    public static void doNothing() {
        return;
    }
    public static int  plusOne(int a) {
        return a+1;
    }
    public static int  plusTwo(int a) {
        return a+2;
    }
    public static void printing(){
        System.out.println("Method: printing executed!");
    }
    public static void listMethodInClass(){
        Method[] methods = Testing.class.getDeclaredMethods();

        for (Method method : methods) {
            System.out.println("Method name: " + method.getName());
            System.out.println("Return type: " + method.getReturnType().getName());

            // List parameter types
            Class<?>[] parameterTypes = method.getParameterTypes();
            System.out.print("Parameters: ");
            for (Class<?> param : parameterTypes) {
                System.out.print(param.getName() + " ");
            }
            System.out.println("\n----------------------------");
        }
    }
//    public static void insertMethod() {
//        try {
//            // Create a ClassPool and append the class path
//            ClassPool classPool = ClassPool.getDefault();
//            classPool.appendClassPath(new LoaderClassPath(Testing.class.getClassLoader()));
//
//            // Name of the target class and method
//            String targetClassName = "org.example.Testing";
//            String targetMethodName = "printing";
//
//            // Get the CtClass representation of the target class
//            CtClass ctClass = classPool.get(targetClassName);
//
//            // Get the target method within the class
//            CtMethod method = ctClass.getDeclaredMethod(targetMethodName);
//
//            // Using insertAfter to add code that logs the stack trace
//            method.insertAfter("{ " +
//                    "System.out.println(\"Probe: Method executed\"); " +
//                    "StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();" +
//                    "for (int i = 2; i < stackTrace.length; i++) { " +  // Skip the first two elements
//                    "    System.out.println(stackTrace[i].toString()); " +
//                    "}" +
//                    "System.out.println(\"hello hello!\"); " +
//                    "printing();"+
//                    "}"
//
//            );
//
//            // Load or redefine the modified class into the JVM
//            ctClass.toClass();
//
//            // Optionally create an instance and invoke the method
//            Class<?> clazz = Class.forName(targetClassName);
//            Object instance = clazz.getDeclaredConstructor().newInstance();
//            clazz.getMethod(targetMethodName).invoke(instance);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
