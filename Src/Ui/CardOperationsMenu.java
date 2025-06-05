package Ui;

import Card.*;
import Model.TransactionCard;
import Service.BankService;
import repository.dao.TransactionCardDAO;

import java.sql.SQLException;
import java.util.*;

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
            System.out.println("4. Make a payment card");
            System.out.println("5. Show all transactions");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");

            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    card.activateCard();
                    bankService.updateCard(card);
                    System.out.println("Card activated.");
                    break;

                case "2":
                    card.deactivateCard();
                    bankService.updateCard(card);
                    System.out.println("Card deactivated.");
                    break;

                case "3":
                    System.out.println(card); // se apelează toString()
                    break;

                case "4":
                    System.out.print("Enter merchant name: ");
                    String merchant = scanner.nextLine();

                    System.out.print("Enter amount: ");
                    double amount;
                    try {
                        amount = Double.parseDouble(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount.");
                        break;
                    }

                    if (card instanceof CreditCard) {
                        ((CreditCard) card).makePayment(amount, merchant);
                    } else if (card instanceof PrepaidCard) {
                        ((PrepaidCard) card).makePayment(amount, merchant);
                    } else if (card instanceof VirtualCard) {
                        ((VirtualCard) card).makePayment(amount, merchant);
                    } else if (card instanceof DebitCard) {
                        ((DebitCard) card).makePayment(amount, merchant);
                    }else{
                        System.out.println("This card type does not support payments.");
                    }

                    break;

                case "5":
                    List<TransactionCard> txList = card.getTranzactii();
                    if (txList.isEmpty()) {
                        System.out.println("No transactions available.");
                    } else {
                        txList.sort(Comparator.comparing(TransactionCard::getTimestamp).reversed());
                        System.out.println("--- Transaction History ---");
                        txList.forEach(System.out::println);
                    }
                    break;

                case "6":
                    bankService.updateCard(card);
                    inCardMenu = false;
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }
}
