package repository.dao;

import Account.BankAccount;
import Card.BankCard;
import Card.PrepaidCard;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrepaidCardDAO extends CardDAO {

    private static PrepaidCardDAO instance;

    private PrepaidCardDAO() {
        super();
    }

    public static PrepaidCardDAO getInstance() {
        if (instance == null) {
            synchronized (PrepaidCardDAO.class) {
                if (instance == null) {
                    instance = new PrepaidCardDAO();
                }
            }
        }
        return instance;
    }

    @Override
    public String getCardType() {
        return "PREPAID";
    }

    @Override
    public boolean createCard(BankCard card) throws SQLException {
        if (!(card instanceof PrepaidCard)) return false;
        PrepaidCard prepaid = (PrepaidCard) card;

        connection.setAutoCommit(false);

        try {
            int id = createBaseCard(prepaid);

            String sql = "INSERT INTO prepaid_cards (card_number, balance) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, prepaid.getCardNumber());
                stmt.setDouble(2, prepaid.getBalance());
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
        if (!(card instanceof PrepaidCard)) return false;
        PrepaidCard prepaid = (PrepaidCard) card;

        boolean baseUpdated = updateBaseCard(prepaid);

        String sql = "UPDATE prepaid_cards SET balance = ? WHERE card_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, prepaid.getBalance());
            stmt.setString(2, prepaid.getCardNumber());

            boolean specificUpdated = stmt.executeUpdate() > 0;
            return baseUpdated && specificUpdated;
        }
    }

    @Override
    public boolean deleteCard(String cardNumber) throws SQLException {
        connection.setAutoCommit(false);
        boolean specificDeleted = false;
        boolean baseDeleted = false;

        try{
            String sql = "DELETE FROM prepaid_cards WHERE card_number = ?";
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
    public PrepaidCard getCardByNumber(String cardNumber) throws SQLException {
        ResultSet baseRs = loadBaseCardByNumber(cardNumber);
        if (!baseRs.next()) return null;

        int id = baseRs.getInt("id");
        String holderName = baseRs.getString("card_holder_name");
        LocalDate expiry = baseRs.getDate("expiry_date").toLocalDate();
        String cvv = baseRs.getString("cvv");
        String pin = baseRs.getString("pin");
        boolean isActive = baseRs.getBoolean("is_active");

        baseRs.close();

        String sql = "SELECT * FROM prepaid_cards WHERE card_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cardNumber);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;

            double balance = rs.getDouble("balance");

            PrepaidCard card = new PrepaidCard(cardNumber, holderName, expiry, cvv, pin, balance);
            card.setCardId(id);
            card.setActive(isActive);

            rs.close();
            return card;
        }
    }

    @Override
    public List<BankCard> getCardsByCustomerId(int customerId) throws SQLException {
        List<BankCard> result = new ArrayList<>();
        String sql = "SELECT card_number FROM bank_cards WHERE account_id IN " +
                "(SELECT id FROM bank_accounts WHERE customer_id = ?) AND card_type = 'PREPAID'";

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
        String sql = "SELECT card_number FROM bank_cards WHERE id = ? AND card_type = 'PREPAID'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return getCardByNumber(rs.getString("card_number"));
            }
        }
        return null;
    }
}
