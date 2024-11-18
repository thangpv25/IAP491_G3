package target.taint;

public class BasicIOExample {
    public static void main(String[] args) {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        
        // Asking for user input
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();
        
        System.out.print("Enter your age: ");
        int age = scanner.nextInt();

        // Displaying a greeting
        System.out.println("Hello, " + name + "! You are " + age + " years old.");

        // Checking if the user is an adult
        if (age >= 18) {
            System.out.println("You are an adult.");
        } else {
            System.out.println("You are not an adult yet.");
        }
        
        // Closing the scanner
        scanner.close();
    }
}
