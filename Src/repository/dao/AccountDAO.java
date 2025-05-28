package repository.dao;

import java.math.BigDecimal;
import java.sql.*;

import Account.BankAccount;
import repository.config.DatabaseConnection;

public abstract class AccountDAO {
    protected final Connection connection;

    public AccountDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // Returns the specific account type (e.g., "savings", "business", etc.)
    public abstract String getAccountType();

    // Inserts into bank_accounts table and returns generated account ID
    protected int createBaseAccount(BankAccount account) throws SQLException {
        String sql = "INSERT INTO bank_accounts (iban, balance, currency, customer_id, account_type) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, account.getIban());
            stmt.setBigDecimal(2, BigDecimal.valueOf(account.getBalance()));
            stmt.setString(3, account.getCurrency());
            stmt.setInt(4, account.getCustomer().getId());
            stmt.setString(5, getAccountType());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Failed to create base account");
    }

    // Updates common fields from bank_accounts
    protected boolean updateBaseAccount(BankAccount account) throws SQLException {
        String sql = "UPDATE bank_accounts SET balance = ?, currency = ? WHERE iban = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBigDecimal(1, BigDecimal.valueOf(account.getBalance()));
            stmt.setString(2, account.getCurrency());
            stmt.setString(3, account.getIban());

            return stmt.executeUpdate() > 0;
        }
    }

    // Deletes from bank_accounts (the child table record should be deleted first)
    protected boolean deleteBaseAccount(String iban) throws SQLException {
        String sql = "DELETE FROM bank_accounts WHERE iban = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, iban);
            return stmt.executeUpdate() > 0;
        }
    }

    // Loads base account data from bank_accounts by IBAN
    protected ResultSet loadBaseAccountByIban(String iban) throws SQLException {
        String sql = "SELECT * FROM bank_accounts WHERE iban = ?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, iban);
        return stmt.executeQuery(); // Important: caller must close this ResultSet
    }

    // Abstract methods to be implemented by each account type
    public abstract boolean createAccount(BankAccount account) throws SQLException;

    public abstract BankAccount loadAccountDetails(int accountId) throws SQLException;

    public abstract boolean updateAccount(BankAccount account) throws SQLException;

    public abstract boolean deleteAccount(String iban) throws SQLException;
}
