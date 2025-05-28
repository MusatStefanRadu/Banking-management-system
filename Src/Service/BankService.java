package Service;

import Account.*;
import Card.BankCard;
import Model.Customer;
import Model.Transaction;
import Util.CurrencyConverter;
import repository.dao.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BankService {

    private final CustomerDAO customerDao = CustomerDAO.getInstance();

    private final List<Transaction> transactions = new ArrayList<>();
    private final List<BankCard> cards = new ArrayList<>();

    // ==================== Customer logic ====================

    public boolean addCustomer(Customer customer) {
        try {
            if (customerDao.customerExists(customer.getPersonalIdentificationNumber())) {
                System.out.println("Error: Customer with this CNP already exists");
                return false;
            }
            boolean success = customerDao.addCustomer(customer);
            if (success) {
                AuditLogger.log("Customer created: " + customer.getFirstName() + " " + customer.getLastName());
            }

            return success;

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            return false;
        }
    }

    public Customer findCustomerByCNP(String cnp) {
        try {
            return customerDao.getCustomerByCNP(cnp).orElse(null);
        } catch (SQLException e) {
            System.err.println("Error finding customer: " + e.getMessage());
            return null;
        }
    }

    public List<Customer> getAllCustomers() {
        try {
            return customerDao.getAllCustomers();
        } catch (SQLException e) {
            System.err.println("Error loading customers: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean deleteCustomerByCNP(String cnp) {
        try {
            Customer customer = findCustomerByCNP(cnp);
            if (customer == null) return false;

            // Optional: șterge conturile sale înainte
            List<BankAccount> accounts = getAllAccountsByCustomer(customer);
            for (BankAccount acc : accounts) {
                deleteAccountByIban(acc.getIban());
            }

            boolean deleted = customerDao.deleteCustomerByCNP(cnp);
            if (deleted) {
                AuditLogger.log("Customer deleted: " + customer.getFirstName() + " " + customer.getLastName());
            }

            return deleted;
        } catch (SQLException e) {
            System.err.println("Error deleting customer: " + e.getMessage());
            return false;
        }
    }

    public List<BankAccount> getAllAccountsByCustomer(Customer customer) {
        List<BankAccount> accounts = new ArrayList<>();

        try {
            accounts.addAll(SavingsAccountDAO.getInstance().getAccountsByCustomerId(customer.getId()));
            accounts.addAll(BusinessAccountDAO.getInstance().getAccountsByCustomerId(customer.getId()));
            accounts.addAll(CurrentAccountDAO.getInstance().getAccountsByCustomerId(customer.getId()));
            accounts.addAll(InvestmentAccountDAO.getInstance().getAccountsByCustomerId(customer.getId()));
        } catch (SQLException e) {
            System.err.println("Error loading accounts for customer: " + e.getMessage());
        }

        return accounts;
    }


    // ==================== Account logic ====================

    public boolean createAccount(BankAccount account) {
        try {
            AccountDAO specificDao = getSpecificAccountDAO(account);
            boolean success = specificDao.createAccount(account);

            if (success) {
                AuditLogger.log("Account created: " + account.getIban() + " for " + account.getCustomer().getFirstName());
            }

            return success;
        } catch (SQLException e) {
            System.err.println("Error creating account: " + e.getMessage());
            return false;
        }
    }

    public BankAccount findBankAccountByIban(String iban) {
        // This requires each DAO to implement a method getByIban().
        // For demo purposes, we will try with SavingsAccountDAO (you can generalize later)
        try {
            return SavingsAccountDAO.getInstance().getAccountByIban(iban);
        } catch (SQLException e) {
            System.err.println("Error finding account: " + e.getMessage());
            return null;
        }
    }

    public void showCustomerAccounts(Customer customer) {
        System.out.println("Accounts for customer: " + customer.getFirstName() + " " + customer.getLastName());

        // For demo: load savings accounts only
        try {
            List<BankAccount> accounts = new ArrayList<>();
            accounts.addAll(SavingsAccountDAO.getInstance().getAccountsByCustomerId(customer.getId()));
            accounts.addAll(BusinessAccountDAO.getInstance().getAccountsByCustomerId(customer.getId()));
            accounts.addAll(CurrentAccountDAO.getInstance().getAccountsByCustomerId(customer.getId()));
            accounts.addAll(InvestmentAccountDAO.getInstance().getAccountsByCustomerId(customer.getId()));

            for (BankAccount acc : accounts) {
                System.out.println(acc);
            }

        } catch (SQLException e) {
            System.err.println("Failed to load accounts: " + e.getMessage());
        }
    }

    private AccountDAO getSpecificAccountDAO(BankAccount account) {
        if (account instanceof BusinessAccount) {
            return BusinessAccountDAO.getInstance();
        } else if (account instanceof CurrentAccount) {
            return CurrentAccountDAO.getInstance();
        } else if (account instanceof InvestmentAccount) {
            return InvestmentAccountDAO.getInstance();
        } else if (account instanceof SavingsAccount) {
            return SavingsAccountDAO.getInstance();
        }
        throw new IllegalArgumentException("Unknown account type");
    }

    public boolean deleteAccountByIban(String iban) {
        try {
            BankAccount account = findBankAccountByIban(iban);
            if (account == null) return false;

            AccountDAO dao = getSpecificAccountDAO(account);
            boolean deleted = dao.deleteAccount(iban);

            if (deleted) {
                AuditLogger.log("Account deleted: " + iban);
            }

            return deleted;
        } catch (SQLException e) {
            System.err.println("Error deleting account: " + e.getMessage());
            return false;
        }
    }


    // ==================== Card logic (in-memory) ====================

    public void addCard(BankCard card) {
        cards.add(card);
        System.out.println("Card added for customer: " + card.getCardHolderName());
        AuditLogger.log("Card created for: " + card.getCardHolderName());
    }

    public BankCard findCardByNumber(String nr) {
        for (BankCard c : cards) {
            if (c.getCardNumber().equals(nr)) {
                return c;
            }
        }
        return null;
    }

    // ==================== Transaction logic (in-memory) ====================

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

        System.out.println("Transfer successful. Transaction ID: " + transaction.getId());
        AuditLogger.log("Transfer of " + amount + " " + from.getCurrency() +
                " from " + from.getIban() +
                " to " + to.getIban() +
                " (" + receivedAmount + " " + to.getCurrency() + ")");
    }

    public List<Transaction> getTransactionsForAccount(BankAccount account) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getSourceAccount().equals(account) || t.getDestinationAccount().equals(account)) {
                result.add(t);
            }
        }
        return result;
    }
}
