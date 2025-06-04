package repository.dao;

import Account.BankAccount;
import Account.BusinessAccount;
import Account.CurrentAccount;
import Card.BankCard;
import Card.VirtualCard;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VirtualCardDAO extends CardDAO {

    private static VirtualCardDAO instance;

    private VirtualCardDAO() {
        super();
    }

    public static VirtualCardDAO getInstance() {
        if (instance == null) {
            synchronized (VirtualCardDAO.class) {
                if (instance == null) {
                    instance = new VirtualCardDAO();
                }
            }
        }
        return instance;
    }

    @Override
    public String getCardType() {
        return "VIRTUAL";
    }

    @Override
    public boolean createCard(BankCard card) throws SQLException {
        if (!(card instanceof VirtualCard)) return false;
        VirtualCard virtual = (VirtualCard) card;

        connection.setAutoCommit(false);

        try {
            int id = createBaseCard(virtual);

            String sql = "INSERT INTO virtual_cards (card_number, usage_limit) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, virtual.getCardNumber());
                stmt.setInt(2, virtual.getUsageLimit());

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
        if (!(card instanceof VirtualCard)) return false;
        VirtualCard virtual = (VirtualCard) card;

        boolean baseUpdated = updateBaseCard(virtual);

        String sql = "UPDATE virtual_cards SET usage_limit = ? WHERE card_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, virtual.getUsageLimit());
            stmt.setString(2, virtual.getCardNumber());

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
            String sql = "DELETE FROM virtual_cards WHERE card_number = ?";
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
    public VirtualCard getCardByNumber(String cardNumber) throws SQLException {
        ResultSet baseRs = loadBaseCardByNumber(cardNumber);
        if (!baseRs.next()) return null;

        int id = baseRs.getInt("id");
        String holderName = baseRs.getString("card_holder_name");
        LocalDate expiry = baseRs.getDate("expiry_date").toLocalDate();
        String cvv = baseRs.getString("cvv");
        boolean isActive = baseRs.getBoolean("is_active");

        BankAccount linkedAccount = null;
        int accountId = baseRs.getInt("account_id");
        if (!baseRs.wasNull()) {
            linkedAccount = findAccountById(accountId);

            if (!(linkedAccount instanceof CurrentAccount || linkedAccount instanceof BusinessAccount)) {
                throw new IllegalArgumentException("VirtualCard must be linked to a compatible account (Current or Business).");
            }        }

        baseRs.close();

        String sql = "SELECT * FROM virtual_cards WHERE card_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cardNumber);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;

            int usageLimit = rs.getInt("usage_limit");

            VirtualCard card = new VirtualCard(cardNumber, holderName, expiry, linkedAccount, cvv, usageLimit);
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
                "(SELECT id FROM bank_accounts WHERE customer_id = ?) AND card_type = 'VIRTUAL'";

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
        String sql = "SELECT card_number FROM bank_cards WHERE id = ? AND card_type = 'VIRTUAL'";
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
