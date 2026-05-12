package security.storage;

import database.databaseConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Records tamper-evident audit log entries for sensitive actions (login, payment, order, etc.).
 *
 * Each entry contains:
 *   - timestamp  : ISO-8601 UTC
 *   - userId     : who performed the action
 *   - action     : what was done (e.g. "LOGIN", "PLACE_ORDER", "PAYMENT")
 *   - data       : contextual detail (order id, amount, etc.) — NOT sensitive plaintext
 *   - entryHash  : SHA-256 of (timestamp + userId + action + data + previousHash)
 *                  chaining hashes makes tampering detectable
 *
 * DB table: audit_log (id BIGINT AUTO_INCREMENT PK, timestamp VARCHAR, user_id VARCHAR,
 *                       action VARCHAR, data VARCHAR, entry_hash VARCHAR)
 */
public class AuditLogger {

    // Singleton-style: one shared "last hash" for the chain per server run.
    // In a clustered deployment you would read the last hash from the DB instead.
    private String lastHash = null;   // will be loaded from DB on first use

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Logs an action performed by a user.
     *
     * @param userId identifier of the user (or "SYSTEM" for automated events)
     * @param action short label such as "LOGIN", "LOGOUT", "PLACE_ORDER", "PAYMENT"
     * @param data   contextual details — avoid raw sensitive values; use IDs or masked data
     */
    public synchronized void logAction(String userId, String action, String data) {
        // Lazy-load the last hash from DB on first use
        if (lastHash == null) {
            lastHash = loadLastHashFromDB();
        }

        String timestamp = Instant.now().toString();
        String entryHash = computeHash(timestamp, userId, action, data, lastHash);

        String sql = "INSERT INTO audit_log (timestamp, user_id, action, data, entry_hash) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, timestamp);
            ps.setString(2, userId);
            ps.setString(3, action);
            ps.setString(4, data);
            ps.setString(5, entryHash);
            ps.executeUpdate();
            lastHash = entryHash;   // advance the chain
        } catch (SQLException e) {
            // Log to stderr rather than recursively calling logAction
            System.err.println("[AuditLogger] Failed to write audit entry: " + e.getMessage());
        }
    }

    /**
     * Returns all audit log entries ordered by insertion time (most recent last).
     */
    public List<AuditEntry> getAuditTrail() {
        List<AuditEntry> trail = new ArrayList<>();
        String sql = "SELECT id, timestamp, user_id, action, data, entry_hash "
                   + "FROM audit_log ORDER BY id ASC";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                trail.add(new AuditEntry(
                        rs.getLong("id"),
                        rs.getString("timestamp"),
                        rs.getString("user_id"),
                        rs.getString("action"),
                        rs.getString("data"),
                        rs.getString("entry_hash")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("AuditLogger: failed to retrieve audit trail", e);
        }
        return trail;
    }

    /**
     * Verifies the integrity of the full audit chain.
     * Any modified entry will break the hash chain and be detected.
     *
     * @return true if the chain is intact, false if tampering is detected
     */
    public boolean verifyIntegrity() {
        List<AuditEntry> trail = getAuditTrail();
        String previousHash = "GENESIS";
        for (AuditEntry entry : trail) {
            String expected = computeHash(
                    entry.timestamp(), entry.userId(),
                    entry.action(), entry.data(), previousHash);
            if (!expected.equals(entry.entryHash())) {
                System.err.println("[AuditLogger] Integrity violation at entry id=" + entry.id());
                return false;
            }
            previousHash = entry.entryHash();
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Loads the most recent entry_hash from the database to continue the chain.
     * Returns "GENESIS" if the audit log is empty.
     */
    private String loadLastHashFromDB() {
        String sql = "SELECT entry_hash FROM audit_log ORDER BY id DESC LIMIT 1";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("entry_hash");
            }
        } catch (SQLException e) {
            System.err.println("[AuditLogger] Failed to load last hash: " + e.getMessage());
        }
        return "GENESIS";   // empty log — start fresh
    }

    private String computeHash(String timestamp, String userId,
                                String action, String data, String previousHash) {
        try {
            String raw = timestamp + "|" + userId + "|" + action + "|" + data + "|" + previousHash;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("AuditLogger: hash computation failed", e);
        }
    }

    // -------------------------------------------------------------------------
    // Inner record — represents one row from audit_log
    // -------------------------------------------------------------------------

    public record AuditEntry(
            long   id,
            String timestamp,
            String userId,
            String action,
            String data,
            String entryHash
    ) {
        @Override
        public String toString() {
            return "[" + timestamp + "] " + userId + " | " + action + " | " + data;
        }
    }

    /**
     * Clears the audit log table — FOR TESTING ONLY.
     * This allows tests to start with a clean slate.
     */
    public static void clearAuditLogForTests() {
        String sql = "DELETE FROM audit_log";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[AuditLogger] Failed to clear audit log: " + e.getMessage());
        }
    }
}