package Card;

import java.sql.SQLException;
import java.time.LocalDate;
import Account.BankAccount;
import Account.SupportsVirtualCard;
import Model.TransactionCard;
import repository.dao.VirtualCardDAO;

public class VirtualCard extends BankCard {

    //fields
    private int usageLimit;

    public VirtualCard(String cardNumber, String cardHolderName, LocalDate expiryDate, BankAccount linkedAccount, String cvv, int usageLimit) {
        super(cardNumber, cardHolderName, expiryDate, linkedAccount, cvv, null);

        if (!(linkedAccount instanceof SupportsVirtualCard)) {
            throw new IllegalArgumentException("VirtualCard must be linked to a compatible account (Current or Business).");
        }

        this.usageLimit = usageLimit;
        this.isActive  = true; //a virtual card is active from the beginning
    }

    //getters
    public int getUsageLimit() {
        return usageLimit;
    }

    //setters
    public void setUsageLimit(int usageLimit) {
        this.usageLimit = usageLimit;
    }

    // method to simulate using the card
    @Override
    public void makePayment(double amount, String merchant) {
        if (!isActive) {
            System.out.println("Card is not active.");
            return;
        }
        if (usageLimit <= 0) {
            System.out.println("Usage limit exceeded.");
            return;
        }
        if (amount <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }
        if (linkedAccount.getBalance() >= amount) {
            linkedAccount.withdraw(amount);

            try {
                if (linkedAccount instanceof Account.CurrentAccount) {
                    repository.dao.CurrentAccountDAO.getInstance().updateAccount(linkedAccount);
                } else if (linkedAccount instanceof Account.BusinessAccount) {
                    repository.dao.BusinessAccountDAO.getInstance().updateAccount(linkedAccount);
                }
            } catch (Exception e) {
                System.out.println("Failed to update linked account in DB: " + e.getMessage());
            }

            usageLimit--;

            TransactionCard transaction = new TransactionCard(merchant, amount, this.cardNumber);
            addTranzactie(transaction);

            System.out.println("Payment successful. Remaining uses: " + usageLimit);

            try {
                repository.dao.VirtualCardDAO.getInstance().updateCard(this);
            } catch (SQLException e) {
                System.err.println("Error updating account in DB: " + e.getMessage());
            }

        } else {
            System.out.println("Insufficient balance in linked account.");
        }
    }


    @Override
    public String toString() {
        return "Card.VirtualCard{\n" +
                "cardNumber: " + cardNumber + "\n" +
                "cardHolderName: " + cardHolderName + "\n" +
                "expiryDate: " + expiryDate + "\n" +
                "linkedAccount IBAN: " + linkedAccount.getIban() + "\n" +
                "usageLimit: " + usageLimit + "\n" +
                "active: " + isActive + "\n" +
                "}";
    }
}
