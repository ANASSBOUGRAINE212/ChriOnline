package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // ⚠️ Vérifie que ces valeurs correspondent à ta config
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/chrionline";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root"; // ← ton vrai mot de passe

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                System.out.println("[DB] Connexion MySQL OK ✅");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver introuvable : " + e.getMessage());
            }
        }
        return connection;
    }
}