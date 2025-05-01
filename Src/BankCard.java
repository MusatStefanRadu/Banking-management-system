import java.time.LocalDate;

public abstract class BankCard {

    // fields
    protected String cardNumber;
    protected String cardHolderName;
    protected LocalDate expiryDate;
    protected BankAccount linkedAccount;
    protected boolean isActive;
    protected String cvv;
    protected String pin;

    // constructor
    public BankCard(String cardNumber, String cardHolderName, LocalDate expiryDate, BankAccount linkedAccount, String cvv, String pin) {
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate;
        this.linkedAccount = linkedAccount;
        this.cvv = cvv;
        this.isActive = false; // default: cand creezi cardul, e inactiv
        this.pin = pin;
    }

    // getters
    public String getCardNumber() {
        return cardNumber;
    }
    public String getCardHolderName() {
        return cardHolderName;
    }
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    public BankAccount getLinkedAccount() {
        return linkedAccount;
    }
    public boolean isActive() {
        return isActive;
    }
    public String getCvv() {
        return cvv;
    }
    public String getPin() {
        return pin;
    }

    // setters
    public void setActive(boolean active) {
        this.isActive = active;
    }

    // common methods
    public void activateCard() {
        this.isActive = true;
    }
    public void deactivateCard() {
        this.isActive = false;
    }

    @Override
    public String toString() {
        return "BankCard{\n" +
                "cardNumber: " + cardNumber + "\n" +
                "cardHolderName: " + cardHolderName + "\n" +
                "expiryDate: " + expiryDate + "\n" +
                "linkedAccount IBAN: " + linkedAccount.getIban() + "\n" +
                "active: " + isActive + "\n" +
                "}";
    }
}
