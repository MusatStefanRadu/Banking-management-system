package repository.dao;

import Account.BankAccount;
import Account.InvestmentAccount;
import Model.Customer;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvestmentAccountDAO extends AccountDAO {

    private static InvestmentAccountDAO instance;

    private InvestmentAccountDAO() {
        super();
    }

    public static InvestmentAccountDAO getInstance() {
        if (instance == null) {
            synchronized (InvestmentAccountDAO.class) {
                if (instance == null) {
                    instance = new InvestmentAccountDAO();
                }
            }
        }
        return instance;
    }


    @Override
    public String getAccountType() {
        return "INVESTMENT";
    }

    @Override
    public boolean createAccount(BankAccount account) throws SQLException {
        if (!(account instanceof InvestmentAccount)) {
            throw new IllegalArgumentException("Invalid account type");
        }

        InvestmentAccount investment = (InvestmentAccount) account;

        connection.setAutoCommit(false);

        try{
            int accountId = createBaseAccount(investment);
            investment.setId(accountId);

            String sql = "INSERT INTO investment_accounts (account_id, portfolio_value, is_locked) VALUES (?, ?, ?)";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, accountId);
                stmt.setBigDecimal(2, BigDecimal.valueOf(investment.getPortfolioValue()));
                stmt.setBoolean(3, investment.getLocked());

                int rowInserted = stmt.executeUpdate();

                if(rowInserted > 0){
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
    public InvestmentAccount loadAccountDetails(int accountId) throws SQLException {
        String sql = "SELECT ba.*, ia.portfolio_value, ia.is_locked, " +
                "c.customer_id, c.first_name, c.last_name, c.age, " +
                "c.personal_identification_number, c.address, c.is_company, c.registration_date " +
                "FROM bank_accounts ba " +
                "JOIN investment_accounts ia ON ba.account_id = ia.account_id " +
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

                    InvestmentAccount acc = new InvestmentAccount(
                            rs.getString("iban"),
                            rs.getBigDecimal("balance").doubleValue(),
                            customer,
                            rs.getString("currency"),
                            rs.getBigDecimal("portfolio_value").doubleValue(),
                            rs.getBoolean("is_locked")
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
        if (!(account instanceof InvestmentAccount)) {
            throw new IllegalArgumentException("Invalid account type");
        }

        InvestmentAccount investment = (InvestmentAccount) account;

        boolean baseUpdated = updateBaseAccount(investment);

        String sql = "UPDATE investment_accounts SET portfolio_value = ?, is_locked = ? " +
                "WHERE account_id = (SELECT account_id FROM bank_accounts WHERE iban = ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBigDecimal(1, BigDecimal.valueOf(investment.getPortfolioValue()));
            stmt.setBoolean(2, investment.getLocked());
            stmt.setString(3, investment.getIban());

            boolean specificUpdated = stmt.executeUpdate() > 0;
            return baseUpdated && specificUpdated;
        }
    }


    public List<BankAccount> getAccountsByCustomerId(int customerId) throws SQLException {
        List<BankAccount> accounts = new ArrayList<>();

        String sql = "SELECT ba.*, ia.portfolio_value, ia.is_locked, " +
                "c.customer_id, c.first_name, c.last_name, c.age, " +
                "c.personal_identification_number, c.address, c.is_company, c.registration_date " +
                "FROM bank_accounts ba " +
                "JOIN investment_accounts ia ON ba.account_id = ia.account_id " +
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

                    InvestmentAccount acc = new InvestmentAccount(
                            rs.getString("iban"),
                            rs.getBigDecimal("balance").doubleValue(),
                            customer,
                            rs.getString("currency"),
                            rs.getBigDecimal("portfolio_value").doubleValue(),
                            rs.getBoolean("is_locked")
                    );

                    acc.setId(rs.getInt("account_id"));
                    accounts.add(acc);
                }
            }
        }

        return accounts;
    }

    public BankAccount getAccountByIban(String iban) throws SQLException {
        String sql = "SELECT ba.*, ia.portfolio_value, ia.is_locked, " +
                "c.customer_id, c.first_name, c.last_name, c.age, " +
                "c.personal_identification_number, c.address, c.is_company, c.registration_date " +
                "FROM bank_accounts ba " +
                "JOIN investment_accounts ia ON ba.account_id = ia.account_id " +
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

                    InvestmentAccount acc = new InvestmentAccount(
                            rs.getString("iban"),
                            rs.getBigDecimal("balance").doubleValue(),
                            customer,
                            rs.getString("currency"),
                            rs.getBigDecimal("portfolio_value").doubleValue(),
                            rs.getBoolean("is_locked")
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
            String sqlSpecific = "DELETE FROM investment_accounts WHERE account_id = " +
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
