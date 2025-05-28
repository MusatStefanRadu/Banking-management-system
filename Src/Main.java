import Service.BankService;
import Ui.AdminMenu;
import Ui.ServiceMenu;
import repository.config.DatabaseConnection;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            // Initialize database connection
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();

            BankService bankService = new BankService();
            Scanner scanner = new Scanner(System.in);
            boolean running = true;

            // Main application loop
            while (running) {
                System.out.println("\n===== Banking System Main Menu =====");
                System.out.println("1. Administrative Operations");
                System.out.println("2. Customer Services");
                System.out.println("3. Exit");
                System.out.print("Enter your choice: ");

                String option = scanner.nextLine().trim();

                switch (option) {
                    case "1":
                        AdminMenu.run(bankService, scanner);
                        break;
                    case "2":
                        ServiceMenu.run(bankService, scanner);
                        break;
                    case "3":
                        System.out.println("Thank you for using our banking system. Goodbye!");
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }

            // Cleanup resources
            scanner.close();
            dbConnection.closeConnection();

        } catch (Exception e) {
            System.err.println("System error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}