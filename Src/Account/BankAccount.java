package Account;

import Model.Customer;

public abstract class BankAccount {

    //------------------------------------------fields-----------------------------------------------------
    protected String iban;
    protected double balance;
    protected Customer customer;
    protected String currency;

    //-------------------------------------------cosntructor-----------------------------------------------
    public BankAccount(String iban, double balance, Customer customer, String currency) {
        this.iban = iban;
        this.balance = balance;
        this.customer  = customer;
        this.currency = currency;
    }

    //--------------------------------------------getters--------------------------------------------------
    public String getIban() {
        return iban;
    }
    public double getBalance() {
        return balance;
    }
    public Customer getCustomer() {
        return customer;
    }
    public String getCurrency() {
        return currency;
    }
    //--------------------------------------------setters--------------------------------------------------
    public void setBalance(double balance) {
        this.balance = balance;
    }

    //------------------------------------------operations------------------------------------------------
    public void deposit(double amount)
    {
        if(amount > 0)
            balance += amount;
    }

    // ---------------------abstract method - every account will implement its own withdraw logic----------
    public abstract void withdraw(double amount);

    //-----------------------------------------toString method---------------------------------------------
    @Override
    public String toString() {
        return "Account.BankAccount{\n" +
                "iban: " + iban + "\n" +
                "balance: " + balance + "\n" +
                "customer: " + customer + "\n" +
                "}";
    }
}
