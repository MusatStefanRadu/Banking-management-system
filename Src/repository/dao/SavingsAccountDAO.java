package repository.dao;

import Account.BankAccount;
import Account.SavingsAccount;
import Model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SavingsAccountDAO extends AccountDAO {

    private static SavingsAccountDAO instance;

    private SavingsAccountDAO() {
        super();
    }

    public static SavingsAccountDAO getInstance() {
        if (instance == null) {
            synchronized (SavingsAccountDAO.class) {
                if (instance == null) {
                    instance = new SavingsAccountDAO();
                }
            }
        }
        return instance;
    }

    @Override
    public String getAccountType() {
        return "SAVINGS";
    }

    @Override
    public boolean createAccount(BankAccount account) throws SQLException {
        if (!(account instanceof SavingsAccount)) {
            throw new IllegalArgumentException("Invalid account type");
        }

        SavingsAccount savings = (SavingsAccount) account;

        connection.setAutoCommit(false);

        try{
            int accountId = createBaseAccount(savings);
            savings.setId(accountId);

            String sql = "INSERT INTO savings_accounts (account_id, minimum_balance, interest_rate) VALUES (?, ?, ?)";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, accountId);
                stmt.setBigDecimal(2, java.math.BigDecimal.valueOf(savings.getMinimumBalance()));
                stmt.setBigDecimal(3, java.math.BigDecimal.valueOf(savings.getInterestRate()));

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
        }catch (SQLException e){
            connection.rollback(); // dacă apare o excepție în oricare pas
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    @Override
    public BankAccount loadAccountDetails(int accountId) throws SQLException {
        String sql = "SELECT ba.*, sa.minimum_balance, sa.interest_rate, " +
                "c.customer_id, c.first_name, c.last_name, c.age, " +
                "c.personal_identification_number, c.address, c.is_company, c.registration_date " +
                "FROM bank_accounts ba " +
                "JOIN savings_accounts sa ON ba.account_id = sa.account_id " +
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

                    SavingsAccount acc = new SavingsAccount(
                            rs.getString("iban"),
                            rs.getBigDecimal("balance").doubleValue(),
                            customer,
                            rs.getString("currency"),
                            rs.getBigDecimal("minimum_balance").doubleValue(),
                            rs.getBigDecimal("interest_rate").doubleValue()
                    );
                    acc.setId(rs.getInt("account_id"));

                    return acc;
                }
            }
        }

        return null;
    }

    public List<BankAccount> getAccountsByCustomerId(int customerId) throws SQLException {
        List<BankAccount> accounts = new ArrayList<>();

        String sql = "SELECT ba.*, sa.minimum_balance, sa.interest_rate, " +
                "c.customer_id, c.first_name, c.last_name, c.age, " +
                "c.personal_identification_number, c.address, c.is_company, c.registration_date " +
                "FROM bank_accounts ba " +
                "JOIN savings_accounts sa ON ba.account_id = sa.account_id " +
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

                    SavingsAccount acc = new SavingsAccount(
                            rs.getString("iban"),
                            rs.getBigDecimal("balance").doubleValue(),
                            customer,
                            rs.getString("currency"),
                            rs.getBigDecimal("minimum_balance").doubleValue(),
                            rs.getBigDecimal("interest_rate").doubleValue()
                    );
                    acc.setId(rs.getInt("account_id"));

                    accounts.add(acc);
                }
            }
        }

        return accounts;
    }

    public BankAccount getAccountByIban(String iban) throws SQLException {
        String sql = "SELECT ba.*, sa.minimum_balance, sa.interest_rate, " +
                "c.customer_id, c.first_name, c.last_name, c.age, " +
                "c.personal_identification_number, c.address, c.is_company, c.registration_date " +
                "FROM bank_accounts ba " +
                "JOIN savings_accounts sa ON ba.account_id = sa.account_id " +
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

                    SavingsAccount acc = new SavingsAccount(
                            rs.getString("iban"),
                            rs.getBigDecimal("balance").doubleValue(),
                            customer,
                            rs.getString("currency"),
                            rs.getBigDecimal("minimum_balance").doubleValue(),
                            rs.getBigDecimal("interest_rate").doubleValue()
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
        if (!(account instanceof SavingsAccount)) {
            throw new IllegalArgumentException("Invalid account type");
        }

        SavingsAccount savings = (SavingsAccount) account;

        boolean baseUpdated = updateBaseAccount(savings);

        String sql = "UPDATE savings_accounts SET minimum_balance = ?, interest_rate = ? " +
                "WHERE account_id = (SELECT account_id FROM bank_accounts WHERE iban = ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBigDecimal(1, java.math.BigDecimal.valueOf(savings.getMinimumBalance()));
            stmt.setBigDecimal(2, java.math.BigDecimal.valueOf(savings.getInterestRate()));
            stmt.setString(3, savings.getIban());

            boolean specificUpdated = stmt.executeUpdate() > 0;

            return baseUpdated && specificUpdated;
        }
    }

    @Override
    public boolean deleteAccount(String iban) throws SQLException {
        connection.setAutoCommit(false);
        boolean specificDeleted = false;
        boolean baseDeleted = false;

        try {
            // 1. Ștergere din savings_accounts
            String sqlSpecific = "DELETE FROM savings_accounts WHERE account_id = " +
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
