package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class databaseInitializer {
    public static void init() {
        try (Connection conn = databaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // users table
            String createUsersTable = 
                "CREATE TABLE IF NOT EXISTS users (" +
                "userId VARCHAR(36) PRIMARY KEY, " +
                "username VARCHAR(50) NOT NULL UNIQUE, " +
                "email VARCHAR(100) NOT NULL UNIQUE, " +
                "passwordHash VARCHAR(64) NOT NULL, " +
                "address VARCHAR(255), " +
                "phone VARCHAR(20), " +
                "role ENUM('CLIENT','ADMIN') NOT NULL DEFAULT 'CLIENT'" +
                ")";
            stmt.execute(createUsersTable);
            System.out.println("✅ Users table ready");
            
            // products table
            String createProductsTable = 
                "CREATE TABLE IF NOT EXISTS products (" +
                "productId VARCHAR(50) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "description TEXT, " +
                "price DECIMAL(10, 2) NOT NULL, " +
                "stock INT NOT NULL DEFAULT 0, " +
                "category VARCHAR(100)" +
                ")";
            stmt.execute(createProductsTable);
            System.out.println("✅ Products table ready");
            
            // Insert sample products (INSERT IGNORE prevents duplicates)
            String insertProducts = 
                "INSERT IGNORE INTO products (productId, name, description, price, stock, category) VALUES " +
                "('P001', 'Laptop Dell XPS 13', 'High-performance ultrabook with 13-inch display', 999.99, 15, 'Electronics'), " +
                "('P002', 'iPhone 15 Pro', 'Latest Apple smartphone with A17 Pro chip', 1199.00, 25, 'Electronics'), " +
                "('P003', 'Sony WH-1000XM5', 'Premium noise-cancelling headphones', 349.99, 30, 'Audio'), " +
                "('P004', 'Mechanical Keyboard', 'RGB mechanical gaming keyboard', 129.99, 40, 'Accessories'), " +
                "('P005', 'Wireless Mouse', 'Ergonomic wireless mouse with precision tracking', 49.99, 50, 'Accessories')";
            stmt.execute(insertProducts);
            System.out.println("✅ Sample products inserted");
            
            System.out.println("📦 Database initialized with MySQL");
            
        } catch (SQLException e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("DB init failed", e);
        }
    }
}
