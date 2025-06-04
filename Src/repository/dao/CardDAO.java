package repository.dao;

import Card.BankCard;
import Account.BankAccount;
import Card.PrepaidCard;
import repository.config.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public abstract class CardDAO {
    protected final Connection connection;

    public CardDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // Fiecare subclasă trebuie să specifice tipul de card (ex: "credit", "debit", etc.)
    public abstract String getCardType();

    // ============================= CRUD generice ==============================

    // Creează o intrare în tabela "bank_cards" cu datele comune pentru orice card
    protected int createBaseCard(BankCard card) throws SQLException {
        String sql = "INSERT INTO bank_cards (card_number, card_holder_name, expiry_date, cvv, pin, is_active, account_id, card_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, card.getCardNumber());
            stmt.setString(2, card.getCardHolderName());
            stmt.setDate(3, Date.valueOf(card.getExpiryDate()));
            stmt.setString(4, card.getCvv());
            stmt.setString(5, card.getPin());
            stmt.setBoolean(6, card.isActive());

            try {
                BankAccount linkedAccount = card.getLinkedAccount();
                if (linkedAccount != null) {
                    stmt.setInt(7, linkedAccount.getId());
                } else {
                    stmt.setNull(7, Types.INTEGER);
                }
            } catch (UnsupportedOperationException e) {
                // Dacă e un card fără cont (ex: PrepaidCard)
                stmt.setNull(7, Types.INTEGER);
            }

            stmt.setString(8, getCardType());

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int generatedId = rs.getInt(1);
                        card.setCardId(generatedId);
                        return generatedId;
                    }
                }
            }
        }

        throw new SQLException("Failed to create base card.");
    }

    // Actualizează o intrare existentă în tabela "bank_cards"
    protected boolean updateBaseCard(BankCard card) throws SQLException {
        String sql = "UPDATE bank_cards SET card_holder_name = ?, expiry_date = ?, cvv = ?, pin = ?, is_active = ?, account_id = ? " +
                "WHERE card_number = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, card.getCardHolderName());
            stmt.setDate(2, Date.valueOf(card.getExpiryDate()));
            stmt.setString(3, card.getCvv());
            stmt.setString(4, card.getPin());
            stmt.setBoolean(5, card.isActive());

            // Tratare corectă pentru account_id
            if (!(card instanceof PrepaidCard) && card.getLinkedAccount() != null) {
                stmt.setInt(6, card.getLinkedAccount().getId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.setString(7, card.getCardNumber());

            return stmt.executeUpdate() > 0;
        }
    }

    // Șterge un card din tabela "bank_cards" (tabela specifică trebuie ștearsă întâi)
    protected boolean deleteBaseCard(String cardNumber) throws SQLException {
        String sql = "DELETE FROM bank_cards WHERE card_number = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cardNumber);
            return stmt.executeUpdate() > 0;
        }
    }

    // Încarcă datele de bază pentru un card după număr (echivalent cu loadBaseAccountByIban)
    protected ResultSet loadBaseCardByNumber(String cardNumber) throws SQLException {
        String sql = "SELECT * FROM bank_cards WHERE card_number = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, cardNumber);
        return stmt.executeQuery(); // Atenție: apelantul trebuie să închidă ResultSet-ul!
    }


    protected BankAccount findAccountById(int id) {
        for (AccountDAO dao : List.of(
                CurrentAccountDAO.getInstance(),
                BusinessAccountDAO.getInstance(),
                SavingsAccountDAO.getInstance(),
                InvestmentAccountDAO.getInstance()
        )) {
            try {
                BankAccount acc = dao.loadAccountDetails(id);
                if (acc != null) return acc;
            } catch (SQLException ignored) {}
        }
        return null;
    }



    // ============================= Abstracte specifice ==============================

    // Creează un card complet (cu insert și în tabela specifică)
    public abstract boolean createCard(BankCard card) throws SQLException;

    // Actualizează un card complet (și în tabela de tip specific)
    public abstract boolean updateCard(BankCard card) throws SQLException;

    // Șterge un card complet (din ambele tabele)
    public abstract boolean deleteCard(String cardNumber) throws SQLException;

    // Găsește un card complet după numărul său
    public abstract BankCard getCardByNumber(String cardNumber) throws SQLException;

    // Returnează toate cardurile deținute de un client
    public abstract List<BankCard> getCardsByCustomerId(int customerId) throws SQLException;

    public abstract BankCard getCardById(int id) throws SQLException;

}
