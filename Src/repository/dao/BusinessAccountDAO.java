package repository.dao;

import Account.BankAccount;
import Account.BusinessAccount;
import Model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BusinessAccountDAO extends AccountDAO {

    private static BusinessAccountDAO instance;

    private BusinessAccountDAO() {
        super();
    }

    public static BusinessAccountDAO getInstance() {
        if (instance == null) {
            synchronized (BusinessAccountDAO.class) {
                if (instance == null) {
                    instance = new BusinessAccountDAO();
                }
            }
        }
        return instance;
    }

    @Override
    public String getAccountType() { return "BUSINESS"; }

    @Override
    public boolean createAccount(BankAccount account) throws SQLException {
        if (!(account instanceof BusinessAccount)) {
            throw new IllegalArgumentException("Invalid account type");
        }

        BusinessAccount bizAccount = (BusinessAccount) account;

        connection.setAutoCommit(false);

        try {
            int accountId = createBaseAccount(account);
            bizAccount.setId(accountId);

            String sql = "INSERT INTO business_accounts (account_id, company_name, registration_number, vat_number) "
                    + "VALUES (?, ?, ?, ?)";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, accountId);
                stmt.setString(2, bizAccount.getCompanyName());
                stmt.setString(3, bizAccount.getRegistrationNumber());
                stmt.setString(4, bizAccount.getVatNumber());

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
            connection.rollback(); // dacă apare o excepție în oricare pas
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    @Override
    public BusinessAccount loadAccountDetails(int accountId) throws SQLException {
        String sql = "SELECT ba.*, b.company_name, b.registration_number, b.vat_number, " +
                "c.customer_id, c.first_name, c.last_name, c.age, " +
                "c.personal_identification_number, c.address, c.is_company, c.registration_date " +
                "FROM bank_accounts ba " +
                "JOIN business_accounts b ON ba.account_id = b.account_id " +
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

                    BusinessAccount acc = new BusinessAccount(
                            rs.getString("iban"),
                            rs.getBigDecimal("balance").doubleValue(),
                            customer,
                            rs.getString("currency"),
                            rs.getString("company_name"),
                            rs.getString("registration_number"),
                            rs.getString("vat_number")
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
        if (!(account instanceof BusinessAccount)) {
            throw new IllegalArgumentException("Invalid account type");
        }

        BusinessAccount business = (BusinessAccount) account;
        boolean baseUpdated = updateBaseAccount(business);

        String sql = "UPDATE business_accounts SET company_name = ?, registration_number = ?, vat_number = ? " +
                "WHERE account_id = (SELECT account_id FROM bank_accounts WHERE iban = ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, business.getCompanyName());
            stmt.setString(2, business.getRegistrationNumber());
            stmt.setString(3, business.getVatNumber());
            stmt.setString(4, business.getIban());

            boolean specificUpdated = stmt.executeUpdate() > 0;
            return baseUpdated && specificUpdated;
        }
    }

    @Override
    public boolean deleteAccount(String iban) throws SQLException {
        String sql = "DELETE FROM business_accounts WHERE account_id = " +
                "(SELECT account_id FROM bank_accounts WHERE iban = ?)";

        boolean specificDeleted = false;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, iban);
            specificDeleted = stmt.executeUpdate() > 0;
        }

        boolean baseDeleted = deleteBaseAccount(iban);

        return specificDeleted && baseDeleted;
    }


    public List<BankAccount> getAccountsByCustomerId(int customerId) throws SQLException {
        List<BankAccount> accounts = new ArrayList<>();

        String sql = "SELECT ba.*, b.company_name, b.registration_number, b.vat_number, " +
                "c.customer_id, c.first_name, c.last_name, c.age, " +
                "c.personal_identification_number, c.address, c.is_company, c.registration_date " +
                "FROM bank_accounts ba " +
                "JOIN business_accounts b ON ba.account_id = b.account_id " +
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

                    BusinessAccount acc = new BusinessAccount(
                            rs.getString("iban"),
                            rs.getBigDecimal("balance").doubleValue(),
                            customer,
                            rs.getString("currency"),
                            rs.getString("company_name"),
                            rs.getString("registration_number"),
                            rs.getString("vat_number")
                    );

                    acc.setId(rs.getInt("account_id"));
                    accounts.add(acc);
                }
            }
        }

        return accounts;
    }

    public BankAccount getAccountByIban(String iban) throws SQLException {
        String sql = "SELECT ba.*, b.company_name, b.registration_number, b.vat_number, " +
                "c.customer_id, c.first_name, c.last_name, c.age, " +
                "c.personal_identification_number, c.address, c.is_company, c.registration_date " +
                "FROM bank_accounts ba " +
                "JOIN business_accounts b ON ba.account_id = b.account_id " +
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

                    BusinessAccount acc = new BusinessAccount(
                            rs.getString("iban"),
                            rs.getBigDecimal("balance").doubleValue(),
                            customer,
                            rs.getString("currency"),
                            rs.getString("company_name"),
                            rs.getString("registration_number"),
                            rs.getString("vat_number")
                    );

                    acc.setId(rs.getInt("account_id"));
                    return acc;
                }
            }
        }

        return null;
    }

}
