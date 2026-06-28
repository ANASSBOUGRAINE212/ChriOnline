package security.storage;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import database.databaseConnection;
import security.aes.AESCipher;

/**
 * Abstracts encrypted storage of sensitive data (card numbers, order details, etc.)
 * in the database. Uses AES-256-GCM with a master key derived from a passphrase via PBKDF2.
 *
 * The DB table used is: secure_store (store_key VARCHAR PK, store_value TEXT)
 * The stored value format is: base64(salt) : base64(iv+ciphertext)
 */
public class SecureDataStore {

    private static final int    SALT_BYTES      = 16;
    private static final int    KEY_BITS        = 256;
    private static final int    PBKDF2_ITERS    = 310_000;
    private static final String SEPARATOR       = ":";

    // Master passphrase used to derive the AES encryption key.
    // In production this would come from an environment variable or a secrets manager,
    // never hard-coded. Here it is a placeholder for the academic demo.
    private static final char[] MASTER_PASSPHRASE = "ChriOnline_SecretMasterKey_2026!".toCharArray();

    private final SecureRandom random = new SecureRandom();

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Encrypts {@code data} and saves it under {@code key} in the secure_store table.
     * If the key already exists the row is updated.
     *
     * @param key  logical identifier (e.g. "payment:userId:42")
     * @param data plain-text sensitive value to protect
     */
    public void saveEncrypted(String key, String data) {
        String encryptedValue = encrypt(data);

        String sql = "INSERT INTO secure_store (store_key, store_value) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE store_value = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, encryptedValue);
            ps.setString(3, encryptedValue);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SecureDataStore: failed to save encrypted value", e);
        }
    }

    /**
     * Loads and decrypts the value stored under {@code key}.
     *
     * @param key logical identifier previously used in saveEncrypted()
     * @return the original plain-text value, or null if the key does not exist
     */
    public String loadDecrypted(String key) {
        String sql = "SELECT store_value FROM secure_store WHERE store_key = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return decrypt(rs.getString("store_value"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("SecureDataStore: failed to load value", e);
        }
        return null;
    }

    /**
     * Deletes the entry for {@code key} from the secure store.
     */
    public void delete(String key) {
        String sql = "DELETE FROM secure_store WHERE store_key = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SecureDataStore: failed to delete entry", e);
        }
    }

    // -------------------------------------------------------------------------
    // Crypto helpers
    // -------------------------------------------------------------------------

    /** Encrypts plain text → "base64(salt):base64(iv+ciphertext)" */
    private String encrypt(String plaintext) {
        try {
            byte[] salt = new byte[SALT_BYTES];
            random.nextBytes(salt);

            SecretKey key = deriveKey(salt);
            
            // Use AESCipher from security.aes package
            byte[] encrypted = AESCipher.encryptGCM(plaintext.getBytes("UTF-8"), key);

            return Base64.getEncoder().encodeToString(salt)
                    + SEPARATOR + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("SecureDataStore: encryption failed", e);
        }
    }

    /** Decrypts a value produced by encrypt() back to plain text. */
    private String decrypt(String stored) {
        try {
            String[] parts = stored.split(SEPARATOR);
            if (parts.length != 2) throw new IllegalArgumentException("Invalid stored format");

            byte[] salt       = Base64.getDecoder().decode(parts[0]);
            byte[] encrypted  = Base64.getDecoder().decode(parts[1]);

            SecretKey key = deriveKey(salt);
            
            // Use AESCipher from security.aes package
            byte[] plainBytes = AESCipher.decryptGCM(encrypted, key);
            
            return new String(plainBytes, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("SecureDataStore: decryption failed", e);
        }
    }

    /** Derives a 256-bit AES key from the master passphrase + a per-record salt. */
    private SecretKey deriveKey(byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(MASTER_PASSPHRASE, salt, PBKDF2_ITERS, KEY_BITS);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }
}