package Card;

import java.time.LocalDate;
import Account.BankAccount;
import Model.TransactionCard;

public class PrepaidCard extends BankCard {

    //fields
    private double balance;

    //constructor
    public PrepaidCard(String cardNumber, String cardHolderName, LocalDate expiryDate, String cvv, String pin, double balance) {
        super(cardNumber, cardHolderName, expiryDate, null, cvv, pin);
        this.balance = balance;
    }

    //getters
    public double getBalance() {
        return balance;
    }

    @Override
    public BankAccount getLinkedAccount() {
        throw new UnsupportedOperationException("Card.PrepaidCard does not support linked accounts.");
    }

    //setters
    public void setBalance(double balance) {
        this.balance = balance;
    }

    //methods
    public void reload(double amount) {
        if (amount < 0) {
            System.out.println("Reload amount must be a positive number");
            return;
        }
        balance += amount;

        TransactionCard transaction = new TransactionCard("Top-up", amount, this.cardNumber);
        addTranzactie(transaction);

        System.out.println("Reload successful. New balance: " + balance);
    }

    public void spend(double amount, String merchant) {
        if (!isActive) {
            System.out.println("Cannot spend: card is not active.");
            return;
        }
        if (amount <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }
        if (balance >= amount) {
            balance -= amount;

            TransactionCard transaction = new TransactionCard(merchant, amount, this.cardNumber);
            addTranzactie(transaction);

            System.out.println("Payment successful. Remaining balance: " + balance);
        } else {
            System.out.println("Insufficient funds on prepaid card.");
        }
    }

    // toString
    @Override
    public String toString() {
        return "Card.PrepaidCard{\n" +
                "cardNumber: " + cardNumber + "\n" +
                "cardHolderName: " + cardHolderName + "\n" +
                "expiryDate: " + expiryDate + "\n" +
                "balance: " + balance + "\n" +
                "active: " + isActive + "\n" +
                "}";
    }

}
