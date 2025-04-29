public class SavingsAccount extends BankAccount {

    //additional fields
    private double minimumBalance;
    private double interestRate;

    // constructor
    public SavingsAccount(String iban, double balance, Customer customer, String currency, double minimumBalance, double interestRate)
    {
        super(iban, balance, customer, currency);
        this.minimumBalance = minimumBalance;
        this.interestRate = interestRate;
    }

    //getters
    public double getMinimumBalance() {
        return minimumBalance;
    }
    public double getInterestRate() {
        return interestRate;
    }

    //setters
    public void setMinimumBalance(double minimumBalance) {
        this.minimumBalance = minimumBalance;
    }
    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    // implementation ot the withdraw method
    @Override
    public void withdraw(double amount)
    {
        if(amount > 0)
            if(balance - amount >= minimumBalance)
                balance -= amount;
            else
                System.out.println("Cannot withdraw: balance would drop below the minimum required balance.");
    }

    @Override
    public String toString() {
        return "SavingsAccount{\n" +
                "iban: " + iban + "\n" +
                "balance: " + balance + "\n" +
                "minimumBalance: " + minimumBalance + "\n" +
                "interestRate: " + interestRate + "%\n" +
                "customer: " + customer + "\n" +
                "}";
    }
}
