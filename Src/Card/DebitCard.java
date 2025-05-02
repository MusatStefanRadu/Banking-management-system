package Card;

import java.time.LocalDate;
import Account.BankAccount;

public class DebitCard extends BankCard {

    //fields for Card.DebitCard

    //constructor
    public DebitCard(String cardNumber, String cardHolderName, LocalDate expiryDate, BankAccount linkedAccount, String cvv, String pin) {
        super(cardNumber, cardHolderName, expiryDate, linkedAccount, cvv, pin);
    }

    //gettres


    //setters


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
