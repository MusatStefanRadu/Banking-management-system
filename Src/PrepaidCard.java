import java.time.LocalDate;

public class PrepaidCard extends BankCard{

    //fields
    private double balance;
    private String pin;

    //constructor
    public PrepaidCard(String cardNumber, String cardHolderName, LocalDate expiryDate, String cvv, double balance, String pin) {
        super(cardNumber, cardHolderName, expiryDate, null, cvv);
        this.balance = balance;
        this.pin = pin;
    }

    //getters
    public double getBalance() {
        return balance;
    }
    public String getPin() {
        return pin;
    }

    //setters
    public void setBalance(double balance) {
        this.balance = balance;
    }
    public void setPin(String pin) {
        this.pin = pin;
    }

    //method for PIN verify
    public boolean verifyPin(String inputPin) {
        return this.pin.equals(inputPin);
    }

    //methods
    public void reload(double amount) {
        if (amount < 0) {
            System.out.println("Reloadd amount must be a positive number");
            return;
        }
        balance += amount;
        System.out.println("Reload successful. New balance: " + balance);
    }

    public void spend(double amount) {
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
            System.out.println("Payment successful. Remaining balance: " + balance);
        } else {
            System.out.println("Insufficient funds on prepaid card.");
        }
    }

    // toString
    @Override
    public String toString() {
        return "PrepaidCard{\n" +
                "cardNumber: " + cardNumber + "\n" +
                "cardHolderName: " + cardHolderName + "\n" +
                "expiryDate: " + expiryDate + "\n" +
                "balance: " + balance + "\n" +
                "active: " + isActive + "\n" +
                "}";
    }

}
