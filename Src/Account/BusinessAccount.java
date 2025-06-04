package Account;

import Model.Customer;

public class BusinessAccount extends BankAccount
        implements SupportsDebitCard, SupportsCreditCard, SupportsVirtualCard{
    //fields
    private String companyName;
    private String registrationNumber;
    private String vatNumber;

    //constructor
    public BusinessAccount(String iban, double balance, Customer customer, String currency, String companyName, String registrationNumber, String vatNumber)
    {
        super(iban, balance, customer, currency);
        if (!customer.getIsCompany()) {
            throw new IllegalArgumentException("A business account can only be created for a company customer.");
        }
        this.companyName = companyName;
        this.registrationNumber = registrationNumber;
        this.vatNumber = vatNumber;
    }

    //getters
    public String getCompanyName() {
        return companyName;
    }
    public String getRegistrationNumber() {
        return registrationNumber;
    }
    public String getVatNumber() {
        return vatNumber;
    }

    //setters
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
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
            System.out.println("Insufficient funds in business account.");
        }
    }

    @Override
    public String toString() {
        return "Account.BusinessAccount{\n" +
                "iban: " + iban + "\n" +
                "balance: " + balance + "\n" +
                "companyName: " + companyName + "\n" +
                "registrationNumber: " + registrationNumber + "\n" +
                "vatNumber: " + vatNumber + "\n" +
                "customer: " + customer + "\n" +
                "}";
    }
}
