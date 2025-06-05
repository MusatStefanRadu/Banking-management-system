package Card;

import java.sql.SQLException;
import java.time.LocalDate;
import Account.BankAccount;
import Account.SupportsCreditCard;
import Model.TransactionCard;
import repository.dao.CreditCardDAO;

public class CreditCard extends BankCard {

    // fields specific to Card.CreditCard
    private double creditLimit;
    private double amountOwed;

    // constructor
    public CreditCard(String cardNumber, String cardHolderName, LocalDate expiryDate, BankAccount linkedAccount, String cvv, String pin, double creditLimit) {
        super(cardNumber, cardHolderName, expiryDate, linkedAccount, cvv, pin); // apelam constructorul din Card.BankCard

        if (!(linkedAccount instanceof SupportsCreditCard)) {
            throw new IllegalArgumentException("CreditCard must be linked to a compatible account (Current or Business).");
        }

        this.creditLimit = creditLimit;
        this.amountOwed = 0.0; // initial clientul nu are datorii
        this.isActive = false; // cardul este dezactivat initial

    }

    // getters
    public double getCreditLimit() {
        return creditLimit;
    }
    public double getAmountOwed() {
        return amountOwed;
    }

    // setters
    public void setCreditLimit(double creditLimit) {
        this.creditLimit = creditLimit;
    }
    public void setAmountOwed(double amountOwed) {
        if (amountOwed < 0) {
            throw new IllegalArgumentException("amountOwed cannot be negative.");
        }
        this.amountOwed = amountOwed;
    }
    // method to make a purchase
    public void makePayment(double amount, String merchant) {
        if (linkedAccount == null) {
            System.out.println("This credit card is not linked to a valid account.");
            return;
        }

        double balance = linkedAccount.getBalance();

        if (!isActive) {
            System.out.println("Card is not active.");
            return;
        }
        if (amount <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }

        boolean success = false;
        double fromAccount = 0;
        double fromCredit = 0;

        if (balance >= amount) {
            linkedAccount.withdraw(amount);
            fromAccount = amount;
            success = true;

        } else if (amountOwed + (amount - balance) <= creditLimit) {
            if (balance > 0) {
                linkedAccount.withdraw(balance);
                fromAccount = balance;
            }
            fromCredit = amount - fromAccount;
            amountOwed += fromCredit;
            success = true;
        } else {
            System.out.println("Purchase declined: insufficient funds and credit limit exceeded.");
            return;
        }

        if (success) {
            // Actualizeaza contul în DB
            try {
                if (linkedAccount instanceof Account.SavingsAccount) {
                    repository.dao.SavingsAccountDAO.getInstance().updateAccount(linkedAccount);
                } else if (linkedAccount instanceof Account.CurrentAccount) {
                    repository.dao.CurrentAccountDAO.getInstance().updateAccount(linkedAccount);
                } else if (linkedAccount instanceof Account.BusinessAccount) {
                    repository.dao.BusinessAccountDAO.getInstance().updateAccount(linkedAccount);
                }
            } catch (Exception e) {
                System.out.println("Error updating account in DB: " + e.getMessage());
            }

            // Tranzactie
            TransactionCard transaction = new TransactionCard(merchant, amount, this.cardNumber);
            addTranzactie(transaction);

            System.out.println("Purchase successful.");
            if (fromCredit > 0)
                System.out.printf("Partial from account (%.2f), partial on credit (%.2f). Owed now: %.2f%n",
                        fromAccount, fromCredit, amountOwed);
        }

        try {
            CreditCardDAO.getInstance().updateCard(this);
        } catch (SQLException e) {
            System.err.println("Error updating account in DB: " + e.getMessage());
        }
    }


    // method to pay debt
    public void payDebt(double amount) {
        if (amount <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }
        if (amount > amountOwed) {
            System.out.println("You are trying to pay more than you owe. Paying only what you owe: " + amountOwed);
            amountOwed = 0.0;
        } else {
            amountOwed -= amount;
            System.out.println("Debt payment successful! Remaining debt: " + amountOwed);
        }

        try {
            CreditCardDAO.getInstance().updateCard(this);
        } catch (SQLException e) {
            System.err.println("Error updating account in DB:: " + e.getMessage());
        }

    }

    // toString method
    @Override
    public String toString() {
        return "Card.CreditCard{\n" +
                "cardNumber: " + cardNumber + "\n" +
                "cardHolderName: " + cardHolderName + "\n" +
                "expiryDate: " + expiryDate + "\n" +
                "linkedAccount IBAN: " + linkedAccount.getIban() + "\n" +
                "creditLimit: " + creditLimit + "\n" +
                "amountOwed: " + amountOwed + "\n" +
                "active: " + isActive + "\n" +
                "}";
    }
}
