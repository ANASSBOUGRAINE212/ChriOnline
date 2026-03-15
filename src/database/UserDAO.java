package database;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class UserDAO {

    public String registerUser(String username, String email, String password) {
        if (usernameExiste(username)) return "USERNAME_TAKEN";
        if (emailExiste(email))       return "EMAIL_TAKEN";

        String hash = hasherMotDePasse(password);
        if (hash == null) return "DB_ERROR:Hashage impossible";

        return insererUtilisateur(username, email, hash);
    }

    private boolean usernameExiste(String username) {
        return compter("SELECT COUNT(*) FROM users WHERE username = ?", username) > 0;
    }

    private boolean emailExiste(String email) {
        return compter("SELECT COUNT(*) FROM users WHERE email = ?", email) > 0;
    }

    private int compter(String sql, String param) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, param);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DAO] Erreur compter : " + e.getMessage());
        }
        return 0;
    }

    private String hasherMotDePasse(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private String insererUtilisateur(String username, String email, String hash) {
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hash);
            int lignes = stmt.executeUpdate();
            if (lignes > 0) {
                System.out.println("[DAO] Utilisateur '" + username + "' créé ✅");
                return "OK";
            }
        } catch (SQLException e) {
            System.err.println("[DAO] Erreur insertion : " + e.getMessage());
            return "DB_ERROR:" + e.getMessage();
        }
        return "DB_ERROR:Aucune ligne inseree";  // ← cette ligne manquait !
    }
    public String getProfile(String username) {
        String sql = "SELECT username, email, created_at FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String user      = rs.getString("username");
                String email     = rs.getString("email");
                String createdAt = rs.getString("created_at");
                return "OK|" + user + "|" + email + "|" + createdAt;
            } else {
                return "USER_NOT_FOUND";
            }
        } catch (SQLException e) {
            System.err.println("[DAO] Erreur getProfile : " + e.getMessage());
            return "DB_ERROR:" + e.getMessage();
        }
    }
}  // ← accolade fermante de la classe