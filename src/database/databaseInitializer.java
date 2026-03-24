package database;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class databaseInitializer {
    public static void init() {
        String createUsers = """
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

        String createProducts = """
            CREATE TABLE IF NOT EXISTS products (
                id          INT AUTO_INCREMENT PRIMARY KEY,
                name        VARCHAR(100)   NOT NULL,
                description TEXT,
                price       DECIMAL(10,2)  NOT NULL,
                stock       INT            DEFAULT 0,
                category    VARCHAR(50),
                created_by  VARCHAR(36),
                created_at  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (created_by) REFERENCES users(userId)
            );
            """;

        // ✅ INSERT IGNORE prevents duplicates on restart
        String createCarts = """
            CREATE TABLE IF NOT EXISTS carts (
                cartId     VARCHAR(100) PRIMARY KEY,
                userId     VARCHAR(100) NOT NULL,
                createdAt  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE
            );
            """;

        String createCartItems = """
            CREATE TABLE IF NOT EXISTS cart_items (
                itemId     VARCHAR(100) PRIMARY KEY,
                cartId     VARCHAR(100) NOT NULL,
                productId  INT NOT NULL,
                quantity   INT NOT NULL DEFAULT 1,
                unitPrice  DECIMAL(10,2) NOT NULL,
                FOREIGN KEY (cartId) REFERENCES carts(cartId) ON DELETE CASCADE,
                FOREIGN KEY (productId) REFERENCES products(id) ON DELETE CASCADE
            );
            """;

        String insertSampleProducts = """
            INSERT IGNORE INTO products (id, name, description, price, stock, category) VALUES
            (3, 'iPhone 14',       'Apple smartphone latest generation',  999.99,  10, 'Smartphones'),
            (4, 'MacBook Pro',     'Apple laptop 16 inches',             2499.99,   5, 'Laptops'),
            (5, 'Sony Headset',    'Bluetooth audio headset',             199.99,  20, 'Audio'),
            (6, 'Gaming Keyboard', 'Mechanical RGB keyboard',              89.99,  15, 'Accessories'),
            (7, 'Logitech Mouse',  'Ergonomic gaming mouse',               49.99,  25, 'Accessories');
            """;

        try (Connection conn = databaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsers);
            System.out.println("📋 Users table ready");
            stmt.execute(createProducts);
            System.out.println("📦 Products table ready");
            stmt.execute(createCarts);
            System.out.println("🛒 Carts table ready");
            stmt.execute(createCartItems);
            System.out.println("📝 Cart items table ready");
            stmt.execute(insertSampleProducts);
            System.out.println("🛒 Sample products inserted");
            System.out.println("✅ Database initialized with MySQL");
        } catch (SQLException e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
            throw new RuntimeException("DB init failed", e);
        }
    }
}