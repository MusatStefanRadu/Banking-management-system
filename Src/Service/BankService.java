package Service;
import java.util.*;

import Account.BankAccount;
import Card.BankCard;
import Model.Customer;
import Model.Transaction;
import Util.CurrencyConverter;

import java.util.ArrayList;
import java.util.List;

public class BankService {

    //collections of data
    private List<Transaction> transactions;
    private List<BankAccount> accounts;
    private List<BankCard> cards;
    private List<Customer> customers;
    private TreeMap<String, Customer> customerMap;
    private TreeMap<String, Customer> customersByName;

    //constructor
    public BankService() {
        this.transactions = new ArrayList<>();
        this.accounts = new ArrayList<>();
        this.cards = new ArrayList<>();
        this.customers = new ArrayList<>();
        this.customerMap = new TreeMap<>();
        this.customersByName = new TreeMap<>();
    }

    //methods
    public void addCustomer(Customer customer) {
        customers.add(customer);
        customerMap.put(customer.getPersonalIdentificationNumber(), customer);
        String nameKey = customer.getLastName().toLowerCase() + "_" + customer.getFirstName().toLowerCase();
        customersByName.put(nameKey, customer);

        System.out.println("Model.Customer added: " + customer.getFirstName() + " " + customer.getLastName());
        AuditLogger.log("Model.Customer added: " + customer.getFirstName() + " " + customer.getLastName());
    }
    public void addAccount(BankAccount account) {
        accounts.add(account);
        System.out.println("Account added for IBAN: " + account.getIban());
        AuditLogger.log("Account created: " + account.getIban() + " for " +
                account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName());
    }
    public void addCard(BankCard card) {
        cards.add(card);
        System.out.println("Card added for customer: " + card.getCardHolderName());
        AuditLogger.log("Card created for: " + card.getCardHolderName());
    }
    // method for money transfer
    public void transferMoney(BankAccount from, BankAccount to, double amount, String description) {
        if (amount <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }
        if (from.getBalance() < amount) {
            System.out.println("Insufficient funds in source account.");
            return;
        }

        from.withdraw(amount);

        double receivedAmount;
        if (!from.getCurrency().equals(to.getCurrency())) {
            receivedAmount = CurrencyConverter.convert(from.getCurrency(), to.getCurrency(), amount);
        } else {
            receivedAmount = amount;
        }

        to.deposit(receivedAmount);

        Transaction transaction = new Transaction(from, to, amount, description);
        transactions.add(transaction);

        System.out.println("Transfer successful. Model.Transaction ID: " + transaction.getId());
        AuditLogger.log("Transfer of " + amount + " " + from.getCurrency() +
                " from " + from.getIban() +
                " to " + to.getIban() +
                " (" + receivedAmount + " " + to.getCurrency() + ")"
        );
    }

    // returnează tranzacțiile pentru un cont dat
    public List<Transaction> getTransactionsForAccount(BankAccount account) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getSourceAccount().equals(account) || t.getDestinationAccount().equals(account)) {
                result.add(t);
            }
        }
        return result;
    }

    // afișează conturile unui client
    public void showCustomerAccounts(Customer customer) {
        System.out.println("Accounts for customer: " + customer.getFirstName() + " " + customer.getLastName());
        for (BankAccount acc : accounts) {
            if (acc.getCustomer().equals(customer)) {
                System.out.println(acc);
            }
        }
    }

    // gaseste un client dupa CNP
    public Customer findCustomerByCNP(String cnp) {
        return customerMap.get(cnp);
    }

    // gaseste un cont dupa IBAN
    public BankAccount findBankAccountByIban(String iban) {
        for (BankAccount acc : accounts) {
            if (acc.getIban().equals(iban)) {
                return acc;
            }
        }
        return null;
    }

    // gaseste un client dupa nume_prenume
    public void showCustomersSortedByName() {
        for (Map.Entry<String, Customer> entry : customersByName.entrySet()) {
            System.out.println(entry.getValue());
        }
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public BankCard findCardByNumber(String nr) {
        for (BankCard c : cards) {
            if (c.getCardNumber().equals(nr)) {
                return c;
            }
        }
        return null;
    }
}
