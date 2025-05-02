import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        BankService bankService = new BankService();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n===== Welcome to the Banking System =====");
            System.out.println("1. Administrative operations");
            System.out.println("2. Customer services");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    AdminMenu.run(bankService, scanner);
                    break;
                case "2":
                    //ServiceMenu.run(bankService, scanner); // urmează să-l construim
                    break;
                case "3":
                    System.out.println("Exiting system. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
        scanner.close();
    }
}
