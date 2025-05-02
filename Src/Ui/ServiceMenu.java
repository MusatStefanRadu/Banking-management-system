package Ui;

import java.util.Scanner;
import Service.BankService;

public class ServiceMenu {

    public static void run(BankService bankService, Scanner scanner) {

        boolean inServiceMenu = true;

        while (inServiceMenu) {
            System.out.println("\n--- Customer Services ---");
            System.out.println("1. Access account operations");
            System.out.println("2. Access card operations");
            System.out.println("3. Back to main menu");
            System.out.print("Choose an option: ");

            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    AccountServiceMenu.run(bankService, scanner);
                    break;
                case "2":
                    CardOperationsMenu.run(bankService, scanner);
                    break;
                case "3":
                    inServiceMenu = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
}
