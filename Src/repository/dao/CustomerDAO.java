package repository.dao;

import Model.Customer;
import repository.config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAO {
    private static CustomerDAO instance;
    private final Connection connection;

    private CustomerDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public static CustomerDAO getInstance() {
        if (instance == null) {
            synchronized (CustomerDAO.class) {
                if (instance == null) {
                    instance = new CustomerDAO();
                }
            }
        }
        return instance;
    }

    // CREATE - Salvează un nou customer în baza de date
    public boolean addCustomer(Customer customer) throws SQLException {

        if (customerExists(customer.getPersonalIdentificationNumber())) {
            throw new SQLException("Customer with this CNP already exists.");
        }

        String sql = "INSERT INTO customers (first_name, last_name, age, personal_identification_number, " +
                "address, is_company, registration_date) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, customer.getFirstName());
            stmt.setString(2, customer.getLastName());
            stmt.setInt(3, customer.getAge());
            stmt.setString(4, customer.getPersonalIdentificationNumber());
            stmt.setString(5, customer.getAddress());
            stmt.setBoolean(6, customer.getIsCompany());
            stmt.setTimestamp(7, Timestamp.valueOf(customer.getRegistrationDate()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        customer.setId(rs.getInt(1));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // READ - Obține customer după ID
    public Optional<Customer> getCustomerById(int id) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        }
        return Optional.empty();
    }

    // READ - Obține customer după CNP
    public Optional<Customer> getCustomerByCNP(String cnp) throws SQLException {
        String sql = "SELECT * FROM customers WHERE personal_identification_number = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cnp);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        }
        return Optional.empty();
    }

    // READ - Obține toți customerii
    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        }
        return customers;
    }

    // UPDATE - Actualizează un customer existent
    public boolean updateCustomer(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET first_name = ?, last_name = ?, age = ?, address = ?, " +
                "is_company = ?, registration_date = ? WHERE customer_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, customer.getFirstName());
            stmt.setString(2, customer.getLastName());
            stmt.setInt(3, customer.getAge());
            stmt.setString(4, customer.getAddress());
            stmt.setBoolean(5, customer.getIsCompany());
            stmt.setTimestamp(6, Timestamp.valueOf(customer.getRegistrationDate()));
            stmt.setInt(7, customer.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    // DELETE - Șterge un customer
    public boolean deleteCustomer(int id) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // DELETE - Stergere dupa CNP
    public boolean deleteCustomerByCNP(String cnp) throws SQLException {
        if (!customerExists(cnp)) return false;

        String sql = "DELETE FROM customers WHERE personal_identification_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cnp);
            return stmt.executeUpdate() > 0;
        }
    }


    // Verifică existența unui customer după CNP
    public boolean customerExists(String cnp) throws SQLException {
        String sql = "SELECT 1 FROM customers WHERE personal_identification_number = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cnp);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Helper method - Transformă ResultSet în Customer
    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer(
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getInt("age"),
                rs.getString("personal_identification_number"),
                rs.getString("address"),
                rs.getBoolean("is_company"),
                rs.getTimestamp("registration_date").toLocalDateTime()
        );
        customer.setId(rs.getInt("customer_id"));
        return customer;
    }
}