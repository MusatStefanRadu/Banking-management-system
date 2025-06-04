package repository.dao;

import Model.TransactionCard;
import repository.config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionCardDAO {
    private static TransactionCardDAO instance;
    private final Connection connection;

    public TransactionCardDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public static TransactionCardDAO getInstance() {
        if (instance == null) {
            instance = new TransactionCardDAO();
        }
        return instance;
    }

    public boolean saveTransaction(TransactionCard tx) throws SQLException {
        String sql = "INSERT INTO card_transactions (merchant, amount, timestamp, card_number) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tx.getMerchant());
            stmt.setDouble(2, tx.getAmount());
            stmt.setDate(3, Date.valueOf(tx.getTimestamp()));
            stmt.setString(4, tx.getCardNumber());
            return stmt.executeUpdate() > 0;
        }
    }

    public List<TransactionCard> getTransactionsByCard(String cardNumber) throws SQLException {
        String sql = "SELECT * FROM card_transactions WHERE card_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cardNumber);
            ResultSet rs = stmt.executeQuery();
            List<TransactionCard> transactions = new ArrayList<>();
            while (rs.next()) {
                TransactionCard tx = new TransactionCard(
                        rs.getString("merchant"),
                        rs.getDouble("amount"),
                        rs.getString("card_number"),
                        rs.getDate("timestamp").toLocalDate()
                );
                tx.setId(rs.getInt("id"));
                transactions.add(tx);
            }
            return transactions;
        }
    }
}
