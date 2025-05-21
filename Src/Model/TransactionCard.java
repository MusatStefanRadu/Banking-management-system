package Model;
import java.time.LocalDate;


public class TransactionCard {
    private static int idCounter = 1;

    private int id;
    private String comerciant;
    private double amount;
    private LocalDate timestamp;
    private String cardNumber;

    public TransactionCard(String comerciant, double amount, String cardNumber) {
        this.comerciant = comerciant;
        this.amount = amount;
        this.timestamp = LocalDate.now();
        this.cardNumber = cardNumber;
        this.id = idCounter++;
    }

    public String getComerciant() {
        return comerciant;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getTimestamp() {
        return timestamp;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "CardTransaction{\n" +
                "id=" + id + "\n" +
                "comerciant='" + comerciant + "'\n" +
                "amount=" + amount + "\n" +
                "timestamp=" + timestamp + "\n" +
                "cardNumber='" + cardNumber + "'\n" +
                '}';
    }
}
