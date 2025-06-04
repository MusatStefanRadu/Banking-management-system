package Card;

import java.time.LocalDate;
import Account.BankAccount;
import Account.SupportsDebitCard;
import Model.TransactionCard;

public class DebitCard extends BankCard {

    //fields for Card.DebitCard

    //constructor
    public DebitCard(String cardNumber, String cardHolderName, LocalDate expiryDate, BankAccount linkedAccount, String cvv, String pin) {
        super(cardNumber, cardHolderName, expiryDate, linkedAccount, cvv, pin);

        if (!(linkedAccount instanceof SupportsDebitCard)) {
            throw new IllegalArgumentException("DebitCard must be linked to a compatible account (Current or Business).");
        }
    }

    //gettres


    //setters

    public void makePayment(double amount, String merchant) {
        if (!isActive) {
            System.out.println("Card is not active.");
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
                System.out.println("Failed to update account in DB: " + e.getMessage());
            }

            addTranzactie(new TransactionCard(merchant, amount, this.cardNumber));
            System.out.println("Payment successful.");
        } else {
            System.out.printf("Insufficient funds in linked account (available: %.2f, needed: %.2f).\n", linkedAccount.getBalance(), amount);
        }
    }


    @Override
    public String toString() {
        return  "Card.DebitCard{\n" +
                "cardNumber: " + cardNumber + "\n" +
                "cardHolderName: " + cardHolderName + "\n" +
                "expiryDate: " + expiryDate + "\n" +
                "linkedAccount IBAN: " + linkedAccount.getIban() + "\n" +
                "active: " + isActive + "\n" +
                "}";
    }
}
