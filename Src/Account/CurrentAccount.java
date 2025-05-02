package Account;

import Model.Customer;

public class CurrentAccount extends BankAccount {
    // additional fields

    private double overdraftLimit;

    //constructor
    public CurrentAccount(String iban, double balance, Customer customer, String currency, double overdraftLimit) {
        super(iban, balance, customer, currency);
        this.overdraftLimit = overdraftLimit;
    }

    //getters
    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    //setters
    public void setOverdraftLimit(double overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    // implementation of the abstract withdraw method
    @Override
    public void withdraw(double amount) {
        if(amount > 0)
            if(balance + overdraftLimit >= amount)
                balance -= amount;
            else
                System.out.println("Insufficient balance, including overdraft limit");
    }

    @Override
    public String toString()
    {
        return "Account.CurrentAccount{\n" +
                "iban : " + iban + "\n" +
                "balance : " + balance + "\n" +
                "customer : " + customer + "\n" +
                "overdraftLimit : " + overdraftLimit + "\n" +
                "}";
    }
}
