package repository.dao;

import Model.Transaction;
import Account.BankAccount;
import repository.config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionAccountDAO {


    private static class DummyAccount extends BankAccount {
        public DummyAccount(String iban) {
            super(iban, 0.0, null, null);  // valori fictive
        }
        @Override
        public void withdraw(double amount) {
            // Nu face nimic, este doar o instanță temporară pentru a reconstitui tranzacțiile
        }
    }


    private final Connection connection;

    private static TransactionAccountDAO instance;

    private TransactionAccountDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public static TransactionAccountDAO getInstance() {
        if (instance == null) {
            instance = new TransactionAccountDAO();
        }
        return instance;
    }

    public boolean saveTransaction(Transaction transaction) {
        String sql = "INSERT INTO account_transactions (source_iban, destination_iban, amount, transaction_date, description) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, transaction.getSourceAccount().getIban());
            stmt.setString(2, transaction.getDestinationAccount().getIban());
            stmt.setDouble(3, transaction.getAmount());
            stmt.setDate(4, Date.valueOf(transaction.getTransactionDate()));
            stmt.setString(5, transaction.getDescription());

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error saving account transaction: " + e.getMessage());
            return false;
        }
    }

    public List<Transaction> getTransactionsForIBAN(String iban) {
        String sql = "SELECT * FROM account_transactions WHERE source_iban = ? OR destination_iban = ? ORDER BY transaction_date DESC";
        List<Transaction> transactions = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, iban);
            stmt.setString(2, iban);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Transaction t = new Transaction(
                        new DummyAccount(rs.getString("source_iban")),
                        new DummyAccount(rs.getString("destination_iban")),
                        rs.getDouble("amount"),
                        rs.getString("description")
                );
                // suprascrie data cu cea din DB
                t.setTransactionDate(rs.getDate("transaction_date").toLocalDate());
                transactions.add(t);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving transactions: " + e.getMessage());
        }

        return transactions;
    }
}
