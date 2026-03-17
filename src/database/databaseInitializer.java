
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
            System.out.println("✅ Database initialized with MySQL");
            System.out.println("📋 Users table ready");
        } catch (SQLException e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
            throw new RuntimeException("DB init failed", e);
        }
    }

}
