package Ui;// fisier: ui.AccountServiceMenu.java

import Account.BankAccount;
import Service.BankService;
import Model.Transaction;

import java.util.Scanner;

public class AccountServiceMenu {

    public static void run(BankService bankService, Scanner scanner) {
        System.out.print("Enter your IBAN: ");
        String iban = scanner.nextLine();

        BankAccount account = bankService.findBankAccountByIban(iban);
        if (account == null) {
            System.out.println("Account not found.");
            return;
        }

        boolean inAccountMenu = true;

        while (inAccountMenu) {
            System.out.println("\n--- Account Operations ---");
            System.out.println("1. View balance");
            System.out.println("2. Transfer money");
            System.out.println("3. View transactions");
            System.out.println("4. Deposit money");
            System.out.println("5. Back");
            System.out.print("Choose an option: ");

            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    System.out.println("Balance: " + account.getBalance() + " " + account.getCurrency());
                    break;

                case "2":
                    System.out.print("Destination IBAN: ");
                    String destIban = scanner.nextLine();
                    BankAccount destination = bankService.findBankAccountByIban(destIban);
                    if (destination == null) {
                        System.out.println("Destination account not found.");
                        break;
                    }

                    System.out.print("Amount to transfer: ");
                    double amount;
                    try {
                        amount = Double.parseDouble(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount.");
                        break;
                    }

                    System.out.print("Transfer description: ");
                    String description = scanner.nextLine();

                    bankService.transferMoney(account, destination, amount, description);
                    break;

                case "3":
                    System.out.println("--- Model.Transaction History ---");
                    var transactions = bankService.getTransactionsForAccount(account);
                    if (transactions.isEmpty()) {
                        System.out.println("No transactions available.");
                    } else {
                        for (Transaction t : transactions) {
                            System.out.println(t);
                        }
                    }
                    break;

                case "4":
                    bankService.depositFunds(account);
                    break;

                case "5":
                    inAccountMenu = false;
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }
}
