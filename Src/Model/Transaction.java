package Model;

import Account.BankAccount;

import java.time.LocalDate;

public class Transaction {
    private static int idCounter = 1;

    //fields
    private int id;
    private BankAccount sourceAccount;
    private BankAccount destinationAccount;
    private double amount;
    private LocalDate transactionDate;
    private String description;

    //constructor
    public Transaction(BankAccount sourceAccount, BankAccount destinationAccount, double amount, String description) {
        this.id = idCounter++;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.transactionDate = LocalDate.now();
        this.description = description;
    }

    //getters
    public int getId() {
        return id;
    }
    public BankAccount getSourceAccount() {
        return sourceAccount;
    }
    public BankAccount getDestinationAccount() {
        return destinationAccount;
    }
    public double getAmount() {
        return amount;
    }
    public LocalDate getTransactionDate() {
        return transactionDate;
    }
    public String getDescription() {
        return description;
    }

    //setters

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    //toString method
    @Override
    public String toString() {
        return "Model.Transaction{\n" +
                "id: " + id + "\n" +
                "from: " + (sourceAccount != null ? sourceAccount.getIban() : "CASH") + "\n" +
                "to: " + destinationAccount.getIban() + "\n" +
                "amount: " + amount + "\n" +
                "date: " + transactionDate + "\n" +
                "description: " + description + "\n" +
                "}";
    }

}
