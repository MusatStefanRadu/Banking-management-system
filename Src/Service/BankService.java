package Service;

import Account.*;
import Card.*;
import Model.Customer;
import Model.Transaction;
import Util.CurrencyConverter;
import repository.config.DatabaseConnection;
import repository.dao.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class BankService {

    private final Connection connection;

    public BankService() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    private static BankService instance;

    public static BankService getInstance() {
        if (instance == null) {
            instance = new BankService();
        }
        return instance;
    }

    private final CustomerDAO customerDao = CustomerDAO.getInstance();
    private final List<Transaction> transactions = new ArrayList<>();

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
        for (AccountDAO dao : List.of(
                SavingsAccountDAO.getInstance(),
                BusinessAccountDAO.getInstance(),
                CurrentAccountDAO.getInstance(),
                InvestmentAccountDAO.getInstance()
        )) {
            try {
                BankAccount acc = dao.getAccountByIban(iban);
                if (acc != null) return acc;
            } catch (SQLException ignored) {}
        }
        return null;
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
                System.out.println("DEBUG: Account type: " + account.getClass().getSimpleName());
            }

            return deleted;
        } catch (SQLException e) {
            System.err.println("Error deleting account: " + e.getMessage());
            return false;
        }
    }

    public boolean updateAccount(BankAccount account) {
        try {
            AccountDAO dao = getSpecificAccountDAO(account);
            return dao.updateAccount(account);
        } catch (SQLException e) {
            System.err.println("Failed to update account: " + e.getMessage());
            return false;
        }
    }

    public void depositFunds(BankAccount account) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter deposit amount: ");
        double amount;

        try {
            amount = scanner.nextDouble();
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a numeric value.");
            scanner.nextLine(); // curăță bufferul
            return;
        }

        if (amount <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }

        account.deposit(amount);
        System.out.printf("Deposit successful. New balance: %.2f\n", account.getBalance());


        Transaction depositTransaction = new Transaction(null, account, amount, "Cash deposit");
        transactions.add(depositTransaction);
        TransactionAccountDAO.getInstance().saveTransaction(depositTransaction);


        try {
            if (account instanceof CurrentAccount) {
                CurrentAccountDAO.getInstance().updateAccount(account);
            } else if (account instanceof BusinessAccount) {
                BusinessAccountDAO.getInstance().updateAccount(account);
            } else if (account instanceof SavingsAccount) {
                SavingsAccountDAO.getInstance().updateAccount(account);
            }
        } catch (SQLException e) {
            System.err.println("Error updating account in database: " + e.getMessage());
        }
    }



    // ==================== Card logic (in-memory) ====================

    public boolean addCard(BankCard card) {
        try {
            CardDAO dao = getSpecificCardDAO(card);
            boolean success = dao.createCard(card);
            if (success) {
                AuditLogger.log("Card created: " + card.getCardNumber());
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Database error when adding card: " + e.getMessage());
            return false;
        }
    }


    public BankCard findCardByNumber(String cardNumber) {
        try {
            // 1. Află tipul real din DB
            String sql = "SELECT card_type FROM bank_cards WHERE card_number = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, cardNumber);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String type = rs.getString("card_type");

                    // 2. În funcție de tip, folosește DAO-ul potrivit
                    return switch (type.toUpperCase()) {
                        case "DEBIT" -> DebitCardDAO.getInstance().getCardByNumber(cardNumber);
                        case "CREDIT" -> CreditCardDAO.getInstance().getCardByNumber(cardNumber);
                        case "PREPAID" -> PrepaidCardDAO.getInstance().getCardByNumber(cardNumber);
                        case "VIRTUAL" -> VirtualCardDAO.getInstance().getCardByNumber(cardNumber);
                        default -> null;
                    };
                }
            }
        } catch (Exception e) {
            System.out.println("System error while retrieving card: " + e.getMessage());
        }

        return null;
    }



    private CardDAO getSpecificCardDAO(BankCard card) {
        if (card instanceof CreditCard) return CreditCardDAO.getInstance();
        if (card instanceof DebitCard) return DebitCardDAO.getInstance();
        if (card instanceof PrepaidCard) return PrepaidCardDAO.getInstance();
        if (card instanceof VirtualCard) return VirtualCardDAO.getInstance();
        throw new IllegalArgumentException("Unsupported card type: " + card.getClass().getSimpleName());
    }

    public boolean updateCard(BankCard card) {
        try {
            CardDAO dao = getSpecificCardDAO(card);
            return dao.updateCard(card);
        } catch (SQLException e) {
            System.err.println("Failed to update card: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteCardByNumber(String cardNumber) {
        try {
            BankCard card = findCardByNumber(cardNumber);
            if (card == null) return false;

            CardDAO dao = getSpecificCardDAO(card);
            boolean deleted = dao.deleteCard(cardNumber);

            if (deleted) {
                AuditLogger.log("Card deleted: " + cardNumber);
            }

            return deleted;
        } catch (SQLException e) {
            System.err.println("Error deleting card: " + e.getMessage());
            return false;
        }
    }



    // ==================== Transaction logic ======================

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

        double receivedAmount = from.getCurrency().equals(to.getCurrency())
                ? amount
                : CurrencyConverter.convert(from.getCurrency(), to.getCurrency(), amount);

        to.deposit(receivedAmount);

        updateAccount(from);
        updateAccount(to);

        Transaction transaction = new Transaction(from, to, amount, description);
        transactions.add(transaction);
        TransactionAccountDAO.getInstance().saveTransaction(transaction);

        System.out.println("Transfer successful. Transaction ID: " + transaction.getId());
        AuditLogger.log("Transfer of " + amount + " " + from.getCurrency() +
                " from " + from.getIban() + " to " + to.getIban() +
                " (" + receivedAmount + " " + to.getCurrency() + ")");
    }

    public List<Transaction> getTransactionsForAccount(BankAccount account) {
        return TransactionAccountDAO.getInstance().getTransactionsForIBAN(account.getIban());
    }
}
