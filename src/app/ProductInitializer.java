package app;

import database.databaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProductInitializer {

    public static void main(String[] args) {
        // Run this ONCE to insert sample products into the DB
        String sql = "INSERT INTO products (name, description, price, stock, category) VALUES (?, ?, ?, ?, ?)";

        Object[][] products = {
            {"iPhone 14",      "Apple smartphone latest generation",  999.99,  10, "Smartphones"},
            {"MacBook Pro",    "Apple laptop 16 inches",             2499.99,   5, "Laptops"},
            {"Sony Headset",   "Bluetooth audio headset",             199.99,  20, "Audio"},
            {"Gaming Keyboard","Mechanical RGB keyboard",              89.99,  15, "Accessories"},
            {"Logitech Mouse", "Ergonomic gaming mouse",               49.99,  25, "Accessories"}
        };

        try (Connection conn = databaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            for (Object[] p : products) {
                stmt.setString(1, (String)  p[0]);
                stmt.setString(2, (String)  p[1]);
                stmt.setDouble(3, (Double)  p[2]);
                stmt.setInt(4,    (Integer) p[3]);
                stmt.setString(5, (String)  p[4]);
                stmt.executeUpdate();
            }
            System.out.println("✅ Sample products inserted successfully!");
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}