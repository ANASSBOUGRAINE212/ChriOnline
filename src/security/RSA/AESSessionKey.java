package security.RSA;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import security.aes.AESKeyGenerator;

/**
 * AES session key for encrypted communication after RSA handshake.
 * This class manages the AES-256 key that is exchanged via RSA.
 * 
 * For actual encryption/decryption, use AESCipher from security.aes package
 * via SecureInputStream/SecureOutputStream.
 */
public class AESSessionKey {

    private static final String ALGORITHM = "AES";
    private final SecretKey secretKey;

    // ── Génération clé AES aléatoire (côté client) ───────────────
    public AESSessionKey() throws Exception {
        this.secretKey = AESKeyGenerator.generateAESKey();
        System.out.println("🔐 AES-256 session key generated");
    }

    // ── Reconstruction depuis bytes (côté serveur après déchiffrement RSA) ──
    public AESSessionKey(byte[] keyBytes) {
        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
        System.out.println("🔐 AES session key restored");
    }

    public byte[] getRawKey() {
        return secretKey.getEncoded();
    }

    /**
     * Returns the SecretKey for use with SecureInputStream/SecureOutputStream.
     */
    public SecretKey getSecretKey() {
        return secretKey;
    }
}