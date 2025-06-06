package repository.dao;

import Account.BankAccount;
import Account.BusinessAccount;
import Account.CurrentAccount;
import Card.CreditCard;
import Card.BankCard;
import repository.config.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreditCardDAO extends CardDAO {

    private static CreditCardDAO instance;

    private CreditCardDAO() {
        super();
    }

    public static CreditCardDAO getInstance() {
        if (instance == null) {
            synchronized (CreditCardDAO.class) {
                if (instance == null) {
                    instance = new CreditCardDAO();
                }
            }
        }
        return instance;
    }

    @Override
    public String getCardType() { return "CREDIT"; }

    @Override
    public boolean createCard(BankCard card) throws SQLException {
        if (!(card instanceof CreditCard)) {
            throw new IllegalArgumentException("Card is not a CreditCard");
        }

        CreditCard creditCard = (CreditCard) card;

        connection.setAutoCommit(false);  // tranzacție

        try {
            int id = createBaseCard(creditCard);

            String sql = "INSERT INTO credit_cards (card_number, credit_limit, amount_owed) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, creditCard.getCardNumber());
                stmt.setDouble(2, creditCard.getCreditLimit());
                stmt.setDouble(3, creditCard.getAmountOwed());

                int rowInserted = stmt.executeUpdate();

                if (rowInserted > 0) {
                    connection.commit();
                    return true;
                } else {
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
    public boolean updateCard(BankCard card) throws SQLException {
        if (!(card instanceof CreditCard)) {
            throw new IllegalArgumentException("Card is not a CreditCard");
        }

        CreditCard creditCard = (CreditCard) card;
        boolean baseUpdated = updateBaseCard(creditCard);

        String sql = "UPDATE credit_cards SET credit_limit = ?, amount_owed = ? WHERE card_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, creditCard.getCreditLimit());
            stmt.setDouble(2, creditCard.getAmountOwed());
            stmt.setString(3, creditCard.getCardNumber());

            boolean specificUpdated = stmt.executeUpdate() > 0;
            return baseUpdated && specificUpdated;
        }
    }

    @Override
    public boolean deleteCard(String cardNumber) throws SQLException {
        connection.setAutoCommit(false);
        boolean specificDeleted = false;
        boolean baseDeleted = false;

        try {
            String sql = "DELETE FROM credit_cards WHERE card_number = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, cardNumber);
                specificDeleted = stmt.executeUpdate() > 0;
            }

            if (specificDeleted) {
                baseDeleted = deleteBaseCard(cardNumber);
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


    @Override
    public CreditCard getCardByNumber(String cardNumber) throws SQLException {
        ResultSet baseRs = loadBaseCardByNumber(cardNumber);
        if (!baseRs.next()) return null;

        // Recuperează date comune
        int id = baseRs.getInt("id");
        String holderName = baseRs.getString("card_holder_name");
        LocalDate expiry = baseRs.getDate("expiry_date").toLocalDate();
        String cvv = baseRs.getString("cvv");
        String pin = baseRs.getString("pin");
        boolean isActive = baseRs.getBoolean("is_active");

        BankAccount linkedAccount = null;
        int accountId = baseRs.getInt("account_id");
        if (!baseRs.wasNull()) {
            linkedAccount = findAccountById(accountId);

            if (!(linkedAccount instanceof CurrentAccount || linkedAccount instanceof BusinessAccount)) {
                throw new IllegalArgumentException("CreditCard must be linked to a compatible account (Current or Business).");
            }
        }

        baseRs.close();

        // Recuperează credit info
        String sql = "SELECT * FROM credit_cards WHERE card_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cardNumber);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;

            double creditLimit = rs.getDouble("credit_limit");
            double amountOwed = rs.getDouble("amount_owed");

            CreditCard card = new CreditCard(cardNumber, holderName, expiry, linkedAccount, cvv, pin, creditLimit);
            card.setCardId(id);
            card.setActive(isActive);
            card.setAmountOwed(amountOwed);
            rs.close();
            return card;
        }
    }

    @Override
    public List<BankCard> getCardsByCustomerId(int customerId) throws SQLException {
        List<BankCard> result = new ArrayList<>();
        String sql = "SELECT card_number FROM bank_cards WHERE account_id IN " +
                "(SELECT account_id  FROM bank_accounts WHERE customer_id = ?) AND card_type = 'credit'";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String number = rs.getString("card_number");
                BankCard card = getCardByNumber(number);
                if (card != null) result.add(card);
            }
        }
        return result;
    }

    @Override
    public BankCard getCardById(int id) throws SQLException {
        String cardNumber = null;

        // caută card number-ul după id
        String sql = "SELECT card_number FROM bank_cards WHERE id = ? AND card_type = 'CREDIT'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                cardNumber = rs.getString("card_number");
            } else {
                return null;
            }
        }

        // folosește metoda deja existentă
        return getCardByNumber(cardNumber);
    }

}
