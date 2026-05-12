package security.RSA;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Backend Cryptographie — Génération paire RSA, distribution clé publique,
 * déchiffrement clé AES reçue du client.
 */
public class RSAKeyManager {

    private static final String ALGORITHM = "RSA";
    private static final String CIPHER     = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final int    KEY_SIZE   = 2048;

    private final KeyPair keyPair;

    // ── Génération paire RSA ──────────────────────────────────────
    public RSAKeyManager() throws NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance(ALGORITHM);
        gen.initialize(KEY_SIZE, new SecureRandom());
        this.keyPair = gen.generateKeyPair();
        System.out.println("🔑 RSA key pair generated (2048-bit)");
    }

    // ── Distribution clé publique (encodée Base64) ───────────────
    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    // ── Déchiffrement clé AES reçue du client ────────────────────
    public byte[] decryptAESKey(String encryptedAESBase64) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedAESBase64);
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        byte[] aesKey = cipher.doFinal(encryptedBytes);
        System.out.println("🔓 AES key decrypted successfully");
        return aesKey;
    }

    // ── Reconstruction clé publique depuis Base64 (côté client) ──
    public static PublicKey publicKeyFromBase64(String base64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance(ALGORITHM);
        return factory.generatePublic(spec);
    }

    // ── Chiffrement clé AES avec clé publique (côté client) ──────
    public static String encryptAESKey(byte[] aesKey, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encrypted = cipher.doFinal(aesKey);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // ── Vérification intégrité clé publique (hash SHA-256) ───────
    public static String fingerprintPublicKey(String publicKeyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(keyBytes);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            sb.append(String.format("%02x", hash[i]));
            if (i < hash.length - 1 && (i + 1) % 4 == 0) sb.append(":");
        }
        return sb.toString();
    }
}