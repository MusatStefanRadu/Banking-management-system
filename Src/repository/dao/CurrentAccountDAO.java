package repository.dao;

import Account.BankAccount;
import Account.CurrentAccount;
import Model.Customer;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CurrentAccountDAO extends AccountDAO {

    private static CurrentAccountDAO instance;

    private CurrentAccountDAO() {
        super();
    }

    public static CurrentAccountDAO getInstance() {
        if (instance == null) {
            synchronized (CurrentAccountDAO.class) {
                if (instance == null) {
                    instance = new CurrentAccountDAO();
                }
            }
        }
        return instance;
    }


    @Override
    public String getAccountType() {
        return "CURRENT";
    }

    @Override
    public boolean createAccount(BankAccount account) throws SQLException {
        if (!(account instanceof CurrentAccount)) {
            throw new IllegalArgumentException("Invalid account type");
        }

        CurrentAccount current = (CurrentAccount) account;

        connection.setAutoCommit(false);

        try{
            int accountId = createBaseAccount(current);
            current.setId(accountId);

            String sql = "INSERT INTO current_accounts (account_id, overdraft_limit) VALUES (?, ?)";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, accountId);
                stmt.setBigDecimal(2, BigDecimal.valueOf(current.getOverdraftLimit()));

                int rowInserted = stmt.executeUpdate();

                if (rowInserted > 0) {
                    connection.commit();
                    return true;
                }
                else {
                    connection.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    @Override
    public CurrentAccount loadAccountDetails(int accountId) throws SQLException {
        String sql = "SELECT ba.*, ca.overdraft_limit, " +
                "c.customer_id, c.first_name, c.last_name, c.age, " +
                "c.personal_identification_number, c.address, c.is_company, c.registration_date " +
                "FROM bank_accounts ba " +
                "JOIN current_accounts ca ON ba.account_id = ca.account_id " +
                "JOIN customers c ON ba.customer_id = c.customer_id " +
                "WHERE ba.account_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Customer customer = new Customer(
                            rs.getInt("customer_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getInt("age"),
                            rs.getString("personal_identification_number"),
                            rs.getString("address"),
                            rs.getBoolean("is_company"),
                            rs.getTimestamp("registration_date").toLocalDateTime()
                    );

                    CurrentAccount acc =  new CurrentAccount(
                            rs.getString("iban"),
                            rs.getBigDecimal("balance").doubleValue(),
                            customer,
                            rs.getString("currency"),
                            rs.getBigDecimal("overdraft_limit").doubleValue()
                    );

                    acc.setId(rs.getInt("account_id"));
                    return acc;
                }
            }
        }

        return null;
    }

    @Override
    public boolean updateAccount(BankAccount account) throws SQLException {
        if (!(account instanceof CurrentAccount)) {
            throw new IllegalArgumentException("Invalid account type");
        }

        CurrentAccount current = (CurrentAccount) account;
        boolean baseUpdated = updateBaseAccount(current);

        String sql = "UPDATE current_accounts SET overdraft_limit = ? " +
                "WHERE account_id = (SELECT account_id FROM bank_accounts WHERE iban = ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBigDecimal(1, BigDecimal.valueOf(current.getOverdraftLimit()));
            stmt.setString(2, current.getIban());

            boolean specificUpdated = stmt.executeUpdate() > 0;
            return baseUpdated && specificUpdated;
        }
    }

    public List<BankAccount> getAccountsByCustomerId(int customerId) throws SQLException {
        List<BankAccount> accounts = new ArrayList<>();

        String sql = "SELECT ba.*, ca.overdraft_limit, " +
                "c.customer_id, c.first_name, c.last_name, c.age, " +
                "c.personal_identification_number, c.address, c.is_company, c.registration_date " +
                "FROM bank_accounts ba " +
                "JOIN current_accounts ca ON ba.account_id = ca.account_id " +
                "JOIN customers c ON ba.customer_id = c.customer_id " +
                "WHERE ba.customer_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Customer customer = new Customer(
                            rs.getInt("customer_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getInt("age"),
                            rs.getString("personal_identification_number"),
                            rs.getString("address"),
                            rs.getBoolean("is_company"),
                            rs.getTimestamp("registration_date").toLocalDateTime()
                    );

                    CurrentAccount acc = new CurrentAccount(
                            rs.getString("iban"),
                            rs.getBigDecimal("balance").doubleValue(),
                            customer,
                            rs.getString("currency"),
                            rs.getBigDecimal("overdraft_limit").doubleValue()
                    );

                    acc.setId(rs.getInt("account_id"));
                    accounts.add(acc);
                }
            }
        }

        return accounts;
    }

    public BankAccount getAccountByIban(String iban) throws SQLException {
        String sql = "SELECT ba.*, ca.overdraft_limit, " +
                "c.customer_id, c.first_name, c.last_name, c.age, " +
                "c.personal_identification_number, c.address, c.is_company, c.registration_date " +
                "FROM bank_accounts ba " +
                "JOIN current_accounts ca ON ba.account_id = ca.account_id " +
                "JOIN customers c ON ba.customer_id = c.customer_id " +
                "WHERE ba.iban = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, iban);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Customer customer = new Customer(
                            rs.getInt("customer_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getInt("age"),
                            rs.getString("personal_identification_number"),
                            rs.getString("address"),
                            rs.getBoolean("is_company"),
                            rs.getTimestamp("registration_date").toLocalDateTime()
                    );

                    CurrentAccount acc = new CurrentAccount(
                            rs.getString("iban"),
                            rs.getBigDecimal("balance").doubleValue(),
                            customer,
                            rs.getString("currency"),
                            rs.getBigDecimal("overdraft_limit").doubleValue()
                    );

                    acc.setId(rs.getInt("account_id"));
                    return acc;
                }
            }
        }

        return null;
    }

    @Override
    public boolean deleteAccount(String iban) throws SQLException {
        connection.setAutoCommit(false);
        boolean specificDeleted = false;
        boolean baseDeleted = false;

        try {
            // 1. Ștergere din savings_accounts
            String sqlSpecific = "DELETE FROM current_accounts WHERE account_id = " +
                    "(SELECT account_id FROM bank_accounts WHERE iban = ?)";

            try (PreparedStatement stmt = connection.prepareStatement(sqlSpecific)) {
                stmt.setString(1, iban);
                specificDeleted = stmt.executeUpdate() > 0;
            }

            // 2. Ștergere din bank_accounts doar dacă prima a reușit
            if (specificDeleted) {
                baseDeleted = deleteBaseAccount(iban);
            }

            if (specificDeleted && baseDeleted) {
                connection.commit();
                return true;
            } else {
                connection.rollback();
                return false;
            }

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

}
