import java.time.LocalDate;

public class DebitCard extends BankCard {

    //fields for DebitCard
    private String pin;

    //constructor
    public DebitCard(String cardNumber, String cardHolderName, LocalDate expiryDate, BankAccount linkedAccount, String cvv, String pin) {
        super(cardNumber, cardHolderName, expiryDate, linkedAccount, cvv);
        this.pin = pin;
    }

    //gettres
    public String getPin() {
        return pin;
    }

    //setters
    public void setPin(String pin) {
        this.pin = pin;
    }

    //methods
    public boolean verifyPin(String pin) {
        return this.pin.equals(pin);
    }

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
