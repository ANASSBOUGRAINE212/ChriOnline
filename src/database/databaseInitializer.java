
package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class databaseInitializer {
    public static void init() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                userId       VARCHAR(36)  PRIMARY KEY,
                username     VARCHAR(50)  NOT NULL UNIQUE,
                email        VARCHAR(100) NOT NULL UNIQUE,
                passwordHash VARCHAR(64)  NOT NULL,
                address      VARCHAR(255),
                phone        VARCHAR(20),
                role         ENUM('CLIENT','ADMIN') NOT NULL DEFAULT 'CLIENT'
            );
            """;

        try (Connection conn = databaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            
            // Insert some test data if not exists
            String insertSql = """
                INSERT IGNORE INTO users (userId, username, email, passwordHash, address, phone, role) 
                VALUES 
                ('u-001', 'alice', 'alice@test.com', SHA2('hashedpass', 256), '123 Main St', '555-1234', 'CLIENT'),
                ('u-003', 'admin', 'admin@test.com', SHA2('hashedpass', 256), '456 Admin Ave', '555-5678', 'ADMIN');
                """;
            stmt.execute(insertSql);
            
            System.out.println("✅ Database initialized with MySQL");
            System.out.println("👥 Test users created: alice, admin");
        } catch (SQLException e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
            throw new RuntimeException("DB init failed", e);
        }
    }

}
