package security.storage;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Handles secure password hashing using PBKDF2WithHmacSHA256 with a random salt.
 * Replaces any plain-text password storage in the database.
 *
 * Usage (registration):  String hash = PasswordHasher.hashPassword("mypassword");
 * Usage (login check):   boolean ok  = PasswordHasher.verifyPassword("mypassword", hash);
 */
public class PasswordHasher {

    private static final String ALGORITHM  = "PBKDF2WithHmacSHA256";
    private static final int    ITERATIONS = 310_000;   // NIST SP 800-132 recommended minimum
    private static final int    KEY_LENGTH = 256;        // bits
    private static final int    SALT_BYTES = 16;         // 128-bit salt

    // Separator used to store salt + hash together in one string
    private static final String SEPARATOR  = ":";

    private PasswordHasher() {}   // utility class — no instances

    /**
     * Hashes a plain-text password and returns a storable string in the format:
     *   base64(salt) : base64(hash)
     *
     * @param plainPassword the raw password entered by the user
     * @return a storable hash string (safe to put in DB)
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password must not be null or empty");
        }

        byte[] salt = generateSalt();
        byte[] hash = pbkdf2(plainPassword.toCharArray(), salt);

        return Base64.getEncoder().encodeToString(salt)
                + SEPARATOR
                + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verifies a plain-text password against a stored hash string.
     *
     * @param plainPassword the raw password to check
     * @param storedHash    the hash string previously returned by hashPassword()
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) return false;

        String[] parts = storedHash.split(SEPARATOR);
        if (parts.length != 2) return false;

        byte[] salt          = Base64.getDecoder().decode(parts[0]);
        byte[] expectedHash  = Base64.getDecoder().decode(parts[1]);
        byte[] actualHash    = pbkdf2(plainPassword.toCharArray(), salt);

        // Constant-time comparison to prevent timing attacks
        return slowEquals(expectedHash, actualHash);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private static byte[] pbkdf2(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();
            spec.clearPassword();   // wipe password chars from memory
            return hash;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    /** Constant-time byte array comparison to prevent timing side-channels. */
    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}