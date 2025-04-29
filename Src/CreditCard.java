import java.time.LocalDate;

public class CreditCard extends BankCard {

    // fields specific to CreditCard
    private String pin;
    private double creditLimit;
    private double amountOwed;

    // constructor
    public CreditCard(String cardNumber, String cardHolderName, LocalDate expiryDate, BankAccount linkedAccount, String cvv, String pin, double creditLimit) {
        super(cardNumber, cardHolderName, expiryDate, linkedAccount, cvv); // apelam constructorul din BankCard
        this.pin = pin;
        this.creditLimit = creditLimit;
        this.amountOwed = 0.0; // initial clientul nu are datorii
        this.isActive = false; // cardul este dezactivat initial
    }

    // getters
    public String getPin() {
        return pin;
    }
    public double getCreditLimit() {
        return creditLimit;
    }
    public double getAmountOwed() {
        return amountOwed;
    }

    // setters
    public void setPin(String pin) {
        this.pin = pin;
    }
    public void setCreditLimit(double creditLimit) {
        this.creditLimit = creditLimit;
    }

    // method to verify if the entered pin is correct
    public boolean verifyPin(String inputPin) {
        return this.pin.equals(inputPin);
    }

    // method to make a purchase
    public void makePurchase(double amount) {
        if (!isActive) {
            System.out.println("Cannot make purchase: the card is not active.");
            return;
        }
        if (amount <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }
        if (amountOwed + amount <= creditLimit) {
            amountOwed += amount;
            System.out.println("Purchase successful! New amount owed: " + amountOwed);
        } else {
            System.out.println("Purchase declined: credit limit exceeded.");
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
    }

    // toString method
    @Override
    public String toString() {
        return "CreditCard{\n" +
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
