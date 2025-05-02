package Account;

import Model.Customer;

public class InvestmentAccount extends BankAccount {

    //fields
    private double portfolioValue;
    private boolean locked;

    //constructor
    public InvestmentAccount(String iban, double balance, Customer customer, String currency, double portfolioValue, boolean locked) {
        super(iban, balance, customer, currency);
        this.portfolioValue = portfolioValue;
        this.locked = locked;
    }

    //getters
    public boolean getLocked() {
        return locked;
    }
    public double getPortfolioValue() {
        return portfolioValue;
    }

    //setters
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    public void setPortfolioValue(double portfolioValue) {
        this.portfolioValue = portfolioValue;
    }

    //methods
    // invest money from the account balance in investments
    public void invest(double amount) {
        if (amount <= 0) {
            System.out.println("Investment amount must be positive.");
            return;
        }
        if (balance >= amount) {
            balance -= amount;
            portfolioValue += amount;
            System.out.println("Investment successful. Portfolio value is now: " + portfolioValue);
        } else {
            System.out.println("Insufficient balance for investment.");
        }
    }

    // withdraw money from the investment back to the balance (if the investment is not blocked)
    public void withdrawInvestment(double amount) {
        if (locked) {
            System.out.println("Cannot withdraw: investment is currently locked.");
            return;
        }
        if (amount <= 0) {
            System.out.println("Withdrawal amount must be positive.");
            return;
        }
        if (portfolioValue >= amount) {
            portfolioValue -= amount;
            balance += amount;
            System.out.println("Withdrawal from investment successful. New portfolio value: " + portfolioValue);
        } else {
            System.out.println("Insufficient investment value to withdraw.");
        }
    }

    @Override
    public void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Withdrawal amount must be positive.");
            return;
        }
        if (balance >= amount) {
            balance -= amount;
            System.out.println("Withdrawal successful. New balance: " + balance);
        } else {
            System.out.println("Insufficient balance.");
        }
    }

    // toString method
    @Override
    public String toString() {
        return "Account.InvestmentAccount{\n" +
                "iban: " + iban + "\n" +
                "balance: " + balance + "\n" +
                "portfolioValue: " + portfolioValue + "\n" +
                "locked: " + locked + "\n" +
                "customer: " + customer + "\n" +
                "}";
    }
}
