package database.dao;

import database.databaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.user;
import model.user.Role;

public class userDao {

    public user getUserById(String userId) {
        String sql = "SELECT * FROM users WHERE userId = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user u = new user();
                u.setUserId(rs.getString("userId"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                u.setPasswordHash(rs.getString("passwordHash"));
                u.setAddress(rs.getString("address"));
                u.setPhone(rs.getString("phone"));
                u.setRole(Role.valueOf(rs.getString("role")));
                return u;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
        }
        return null;
    }

    public user getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user u = new user();
                u.setUserId(rs.getString("userId"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                u.setPasswordHash(rs.getString("passwordHash"));
                u.setAddress(rs.getString("address"));
                u.setPhone(rs.getString("phone"));
                u.setRole(Role.valueOf(rs.getString("role")));
                return u;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by username: " + e.getMessage());
        }
        return null;
    }

    public user getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user u = new user();
                u.setUserId(rs.getString("userId"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                u.setPasswordHash(rs.getString("passwordHash"));
                u.setAddress(rs.getString("address"));
                u.setPhone(rs.getString("phone"));
                u.setRole(Role.valueOf(rs.getString("role")));
                return u;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by email: " + e.getMessage());
        }
        return null;
    }

    public boolean createUser(user user) {
        String sql = "INSERT INTO users (userId, username, email, passwordHash, address, phone, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUserId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getAddress());
            ps.setString(6, user.getPhone());
            ps.setString(7, user.getRole().toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
        }
        return false;
    }

    public boolean updateProfile(String userId, String address, String phone) {
        String sql = "UPDATE users SET address = ?, phone = ? WHERE userId = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, address);
            ps.setString(2, phone);
            ps.setString(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating profile: " + e.getMessage());
        }
        return false;
    }

    public boolean changePassword(String userId, String oldHashedPassword, String newHashedPassword) {
        String checkSql = "SELECT passwordHash FROM users WHERE userId = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false;
            if (!rs.getString("passwordHash").equals(oldHashedPassword)) return false;
        } catch (SQLException e) {
            System.err.println("Error verifying old password: " + e.getMessage());
            return false;
        }

        String updateSql = "UPDATE users SET passwordHash = ? WHERE userId = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, newHashedPassword);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteUser(String userId) {
        String sql = "DELETE FROM users WHERE userId = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
        return false;
    }

    public boolean updateUserRole(String userId, Role newRole) {
        String sql = "UPDATE users SET role = ? WHERE userId = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newRole.toString());
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user role: " + e.getMessage());
        }
        return false;
    }

    public boolean updateUserRoleByUsername(String username, Role newRole) {
        String sql = "UPDATE users SET role = ? WHERE username = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newRole.toString());
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user role by username: " + e.getMessage());
        }
        return false;
    }

    // ✅ Added from person 1 — formatted profile display
    public String getProfileFormatted(String username) {
        String sql = "SELECT username, email, role FROM users WHERE username = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String uname = rs.getString("username");
                String email = rs.getString("email");
                String role  = rs.getString("role");

                int width = 50;
                String line = "═".repeat(width);

                return String.format(
                    "%n╔%s╗%n" +
                    "║%s║%n" +
                    "╠%s╣%n" +
                    "║  🔹 Username : %-31s║%n" +
                    "║  🔹 Email    : %-31s║%n" +
                    "║  🔹 Role     : %-31s║%n" +
                    "╚%s╝",
                    line,
                    centrer("👤  MY PROFILE", width),
                    line,
                    uname,
                    email,
                    role,
                    line
                );
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error getProfileFormatted: " + e.getMessage());
            return null;
        }
    }

    private String centrer(String text, int width) {
        int spaces = (width - text.length()) / 2;
        String pad = " ".repeat(Math.max(0, spaces));
        String result = pad + text + pad;
        while (result.length() < width) result += " ";
        return result;
    }
}