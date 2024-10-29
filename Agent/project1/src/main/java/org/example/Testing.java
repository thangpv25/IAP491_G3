package org.example;

import Utils.StringUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Scanner;


public class Testing {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.println("Hello and welcome!");
        long pid = ProcessHandle.current().pid();
        Scanner sc = new Scanner(System.in);
        int num;
        // Print the PID
        System.out.println("The PID of this JVM process is: " + pid);
//        System.out.println("Current Class: "+ Testing.class);

        System.out.println("Enter your first favourite number: ");
        num = sc.nextInt();
        System.out.printf("\nYour favourite number is %d.\n", num);
        //printStackTrace();

        System.out.println(StringUtils.toLowerCase("HELLO FINAL !"));
        printing();
        System.out.println("Enter your second favourite number: ");
        num = sc.nextInt();
        System.out.printf("\nYour favourite number is %d.\n", num);
    }



    public static void runCal() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("calc.exe");
            // Dừng chương trình Java cho đến khi ứng dụng Calculator bị đóng
            System.out.println("Calculator is running...");
            process.waitFor();
            System.out.println("Calculator closed.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public static void printStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // Phần tử thứ 2 trong stack trace là phương thức gọi methodA
        // (Phần tử 0 là getStackTrace, phần tử 1 là methodA, nên phần tử 2 là phương thức gọi methodA)
        StackTraceElement caller = stackTrace[2];
        System.out.println("Method called by: " + caller.getClassName() + "." + caller.getMethodName());

    }




    public static void printing() {
        System.out.println("Method: printing executed!");
    }

    public static void listMethodInClass() {
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


}