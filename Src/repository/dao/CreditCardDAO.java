package repository.dao;

import Account.BankAccount;
import Card.CreditCard;
import Card.BankCard;
import Model.Customer;
import Service.BankService;

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
    public String getCardType() {
        return "CREDIT";
    }

    @Override
    public boolean createCard(BankCard card) throws SQLException {
        if (!(card instanceof CreditCard)) throw new IllegalArgumentException("Invalid card type");

        CreditCard credit = (CreditCard) card;
        connection.setAutoCommit(false);

        try {
            boolean base = createBaseCard(credit);

            String sql = "INSERT INTO credit_cards (card_number, credit_limit, amount_owed) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, credit.getCardNumber());
                stmt.setBigDecimal(2, new java.math.BigDecimal(credit.getCreditLimit()));
                stmt.setBigDecimal(3, new java.math.BigDecimal(credit.getAmountOwed()));
                int inserted = stmt.executeUpdate();

                if (base && inserted > 0) {
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
        if (!(card instanceof CreditCard)) throw new IllegalArgumentException("Invalid card type");

        CreditCard credit = (CreditCard) card;

        boolean baseUpdated = updateBaseCard(credit);

        String sql = "UPDATE credit_cards SET credit_limit = ?, amount_owed = ? WHERE card_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBigDecimal(1, new java.math.BigDecimal(credit.getCreditLimit()));
            stmt.setBigDecimal(2, new java.math.BigDecimal(credit.getAmountOwed()));
            stmt.setString(3, credit.getCardNumber());

            return baseUpdated && stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteCard(String cardNumber) throws SQLException {
        String sql = "DELETE FROM credit_cards WHERE card_number = ?";
        boolean deleted;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cardNumber);
            deleted = stmt.executeUpdate() > 0;
        }

        return deleted && deleteBaseCard(cardNumber);
    }

    @Override
    public BankCard getCardByNumber(String cardNumber) throws SQLException {
        String sql = "SELECT bc.*, cc.credit_limit, cc.amount_owed, ba.iban FROM bank_cards bc " +
                "JOIN credit_cards cc ON bc.card_number = cc.card_number " +
                "LEFT JOIN bank_accounts ba ON bc.account_id = ba.account_id " +
                "WHERE bc.card_number = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cardNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BankAccount account = BankService.getInstance().getAccountByIban(rs.getString("iban"));

                    return new CreditCard(
                            rs.getString("card_number"),
                            rs.getString("card_holder_name"),
                            rs.getDate("expiry_date").toLocalDate(),
                            account,
                            rs.getString("cvv"),
                            rs.getString("pin"),
                            rs.getBigDecimal("credit_limit").doubleValue()
                    );
                }
            }
        }
        return null;
    }

    @Override
    public List<BankCard> getCardsByCustomerId(int customerId) throws SQLException {
        List<BankCard> cards = new ArrayList<>();

        String sql = "SELECT bc.card_number FROM bank_cards bc " +
                "JOIN bank_accounts ba ON bc.account_id = ba.account_id " +
                "WHERE ba.customer_id = ? AND bc.card_type = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setString(2, getCardType());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BankCard card = getCardByNumber(rs.getString("card_number"));
                    if (card != null) cards.add(card);
                }
            }
        }

        return cards;
    }
}
