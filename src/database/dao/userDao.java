
package database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import database.databaseConnection;
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
            e.printStackTrace();
        }
        return null;
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
            e.printStackTrace();
        }
        return false;
    }

    public boolean changePassword(String userId, String oldHashedPassword, String newHashedPassword) {
        String check = "SELECT passwordHash FROM users WHERE userId = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false;
            if (!rs.getString("passwordHash").equals(oldHashedPassword)) return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        String sql = "UPDATE users SET passwordHash = ? WHERE userId = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHashedPassword);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
