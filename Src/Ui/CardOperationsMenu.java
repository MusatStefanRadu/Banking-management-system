package Ui;

import Card.BankCard;
import Service.BankService;

import java.util.Scanner;

public class CardOperationsMenu {

    public static void run(BankService bankService, Scanner scanner) {
        System.out.print("Enter your card number: ");
        String cardNumber = scanner.nextLine();

        BankCard card = bankService.findCardByNumber(cardNumber);
        if (card == null) {
            System.out.println("Card not found.");
            return;
        }

        // dacă are PIN -> autentificare
        if (card.getPin() != null) {
            System.out.print("Enter PIN: ");
            String pin = scanner.nextLine();
            if (!card.getPin().equals(pin)) {
                System.out.println("Incorrect PIN.");
                return;
            }
        }

        System.out.println("Card authenticated successfully!");

        boolean inCardMenu = true;

        while (inCardMenu) {
            System.out.println("\n--- Card Operations ---");
            System.out.println("1. Activate card");
            System.out.println("2. Deactivate card");
            System.out.println("3. Show card details");
            System.out.println("4. Back");
            System.out.print("Choose an option: ");

            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    card.activateCard();
                    System.out.println("Card activated.");
                    break;

                case "2":
                    card.deactivateCard();
                    System.out.println("Card deactivated.");
                    break;

                case "3":
                    System.out.println(card); // se apelează toString()
                    break;

                case "4":
                    inCardMenu = false;
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }
}
