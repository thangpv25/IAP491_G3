package org.example;
import java.util.Scanner;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Testing {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.println("Hello and welcome!");
        long pid = ProcessHandle.current().pid();
        // Print the PID
        System.out.println("The PID of this JVM process is: " + pid);

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter your favourite number: ");
        int num = sc.nextInt();
        System.out.printf("\nYour favourite number is %d.", num);

    }
}
