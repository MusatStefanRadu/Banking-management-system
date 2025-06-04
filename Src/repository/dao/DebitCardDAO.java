package repository.dao;

import Account.BankAccount;
import Account.BusinessAccount;
import Account.CurrentAccount;
import Card.BankCard;
import Card.DebitCard;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DebitCardDAO extends CardDAO {

    private static DebitCardDAO instance;

    private DebitCardDAO() {
        super();
    }

    public static DebitCardDAO getInstance() {
        if (instance == null) {
            synchronized (DebitCardDAO.class) {
                if (instance == null) {
                    instance = new DebitCardDAO();
                }
            }
        }
        return instance;
    }

    @Override
    public String getCardType() {
        return "DEBIT";
    }

    @Override
    public boolean createCard(BankCard card) throws SQLException {
        if (!(card instanceof DebitCard)) {
            throw new IllegalArgumentException("Card is not a DebitCard");
        }

        DebitCard debitCard = (DebitCard) card;

        connection.setAutoCommit(false);

        try {
            int id = createBaseCard(debitCard);

            String sql = "INSERT INTO debit_cards (card_number) VALUES (?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, debitCard.getCardNumber());

                int rows = stmt.executeUpdate();
                if (rows > 0) {
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
        if (!(card instanceof DebitCard)) {
            throw new IllegalArgumentException("Card is not a DebitCard");
        }

        // Nu există date specifice pentru debit_card de actualizat, doar cele de bază
        return updateBaseCard(card);
    }

    @Override
    public boolean deleteCard(String cardNumber) throws SQLException {
        connection.setAutoCommit(false);
        boolean specificDeleted = false;
        boolean baseDeleted = false;

        try{
            String sql = "DELETE FROM debit_cards WHERE card_number = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, cardNumber);
                specificDeleted = stmt.executeUpdate() > 0;
            }

            if (specificDeleted) {
                baseDeleted = deleteBaseCard(cardNumber);
            }

            if(specificDeleted && baseDeleted){
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
    public DebitCard getCardByNumber(String cardNumber) throws SQLException {
        ResultSet baseRs = loadBaseCardByNumber(cardNumber);
        if (!baseRs.next()) return null;

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
                throw new IllegalArgumentException("DebitCard must be linked to a compatible account (Current or Business).");
            }
        }

        baseRs.close();

        DebitCard card = new DebitCard(cardNumber, holderName, expiry, linkedAccount, cvv, pin);
        card.setCardId(id);
        card.setActive(isActive);
        return card;
    }

    @Override
    public List<BankCard> getCardsByCustomerId(int customerId) throws SQLException {
        List<BankCard> result = new ArrayList<>();
        String sql = "SELECT card_number FROM bank_cards WHERE account_id IN " +
                "(SELECT id FROM bank_accounts WHERE customer_id = ?) AND card_type = 'DEBIT'";

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
        String sql = "SELECT card_number FROM bank_cards WHERE id = ? AND card_type = 'DEBIT'";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String cardNumber = rs.getString("card_number");
                return getCardByNumber(cardNumber);  // metoda deja implementată
            }
        }

        return null;
    }

}
