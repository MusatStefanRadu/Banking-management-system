import java.time.LocalDate;

public class VirtualCard extends BankCard {

    //fields
    private int usageLimit;

    public VirtualCard(String cardNumber, String cardHolderName, LocalDate expiryDate, BankAccount linkedAccount, String cvv, int usageLimit) {
        super(cardNumber, cardHolderName, expiryDate, linkedAccount, cvv, null);
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
    public void useCard(double amount) {
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
            usageLimit--;
            System.out.println("Payment successful. Remaining uses: " + usageLimit);
        } else {
            System.out.println("Insufficient balance in linked account.");
        }
    }

    @Override
    public String toString() {
        return "VirtualCard{\n" +
                "cardNumber: " + cardNumber + "\n" +
                "cardHolderName: " + cardHolderName + "\n" +
                "expiryDate: " + expiryDate + "\n" +
                "linkedAccount IBAN: " + linkedAccount.getIban() + "\n" +
                "usageLimit: " + usageLimit + "\n" +
                "active: " + isActive + "\n" +
                "}";
    }
}
