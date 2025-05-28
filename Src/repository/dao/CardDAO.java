package repository.dao;

import Card.BankCard;
import Model.Customer;
import Account.BankAccount;
import repository.config.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class CardDAO {
    protected final Connection connection;

    public CardDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public abstract String getCardType();

    public abstract boolean createCard(BankCard card) throws SQLException;

    public abstract boolean updateCard(BankCard card) throws SQLException;

    public abstract boolean deleteCard(String cardNumber) throws SQLException;

    public abstract BankCard getCardByNumber(String cardNumber) throws SQLException;

    public abstract List<BankCard> getCardsByCustomerId(int customerId) throws SQLException;

    protected boolean createBaseCard(BankCard card) throws SQLException {
        String sql = "INSERT INTO bank_cards (card_number, card_holder_name, expiry_date, cvv, pin, is_active, account_id, card_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, card.getCardNumber());
            stmt.setString(2, card.getCardHolderName());
            stmt.setDate(3, Date.valueOf(card.getExpiryDate()));
            stmt.setString(4, card.getCvv());
            stmt.setString(5, card.getPin());
            stmt.setBoolean(6, card.isActive());
            stmt.setInt(7, card.getLinkedAccount() != null ? card.getLinkedAccount().getId() : null);
            stmt.setString(8, getCardType());

            return stmt.executeUpdate() > 0;
        }
    }

    protected boolean deleteBaseCard(String cardNumber) throws SQLException {
        String sql = "DELETE FROM bank_cards WHERE card_number = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cardNumber);
            return stmt.executeUpdate() > 0;
        }
    }

    protected boolean updateBaseCard(BankCard card) throws SQLException {
        String sql = "UPDATE bank_cards SET card_holder_name = ?, expiry_date = ?, cvv = ?, pin = ?, is_active = ?, account_id = ? " +
                "WHERE card_number = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, card.getCardHolderName());
            stmt.setDate(2, Date.valueOf(card.getExpiryDate()));
            stmt.setString(3, card.getCvv());
            stmt.setString(4, card.getPin());
            stmt.setBoolean(5, card.isActive());
            stmt.setInt(6, card.getLinkedAccount() != null ? card.getLinkedAccount().getId() : null);
            stmt.setString(7, card.getCardNumber());

            return stmt.executeUpdate() > 0;
        }
    }
}
