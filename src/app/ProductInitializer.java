package app;

import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProductInitializer {

    public static void main(String[] args) {

        String insertProductSQL = "INSERT INTO Products (name, description, price, stock) VALUES (?, ?, ?, ?)";

        Object[][] products = {
            {"iPhone 14", "Smartphone Apple dernière génération", 999.99, 10},
            {"MacBook Pro", "Ordinateur portable Apple 16 pouces", 2499.99, 5},
            {"Casque Sony", "Casque audio Bluetooth", 199.99, 20},
            {"Clavier Gaming", "Clavier mécanique RGB", 89.99, 15},
            {"Souris Logitech", "Souris gaming ergonomique", 49.99, 25}
        };

        try (Connection conn = DatabaseConnection.getConnection()) {

            PreparedStatement stmt = conn.prepareStatement(insertProductSQL);

            for (Object[] p : products) {
                stmt.setString(1, (String) p[0]);
                stmt.setString(2, (String) p[1]);
                stmt.setDouble(3, (Double) p[2]);
                stmt.setInt(4, (Integer) p[3]);
                stmt.executeUpdate();
            }

            System.out.println("Produits insérés avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}