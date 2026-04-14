package database;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class databaseConnection {
    private static final Properties props = new Properties();

    static {
        try (InputStream in = databaseConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                // Fallback to default MySQL settings
                props.setProperty("db.url", "jdbc:mysql://localhost:3306/chrionline?useSSL=false&serverTimezone=UTC");
                props.setProperty("db.username", "root");
                props.setProperty("db.password", "");
                props.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            }
            Class.forName(props.getProperty("db.driver"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load DB config", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            props.getProperty("db.url"),
            props.getProperty("db.username"),
            props.getProperty("db.password")
        );
    }
}