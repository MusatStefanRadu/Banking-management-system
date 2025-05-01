import java.time.LocalDate;

public class DebitCard extends BankCard {

    //fields for DebitCard

    //constructor
    public DebitCard(String cardNumber, String cardHolderName, LocalDate expiryDate, BankAccount linkedAccount, String cvv, String pin) {
        super(cardNumber, cardHolderName, expiryDate, linkedAccount, cvv, pin);
    }

    //gettres


    //setters


    @Override
    public String toString() {
        return  "DebitCard{\n" +
                "cardNumber: " + cardNumber + "\n" +
                "cardHolderName: " + cardHolderName + "\n" +
                "expiryDate: " + expiryDate + "\n" +
                "linkedAccount IBAN: " + linkedAccount.getIban() + "\n" +
                "active: " + isActive + "\n" +
                "}";
    }
}
