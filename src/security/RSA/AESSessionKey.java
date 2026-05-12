package security.RSA;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Chiffrement AES-GCM pour la session après handshake RSA.
 */
public class AESSessionKey {

    private static final String ALGORITHM  = "AES";
    private static final String CIPHER     = "AES/GCM/NoPadding";
    private static final int    KEY_SIZE   = 256;
    private static final int    GCM_IV_LEN = 12;   // 96 bits
    private static final int    GCM_TAG_LEN= 128;  // bits

    private final SecretKey secretKey;

    // ── Génération clé AES aléatoire (côté client) ───────────────
    public AESSessionKey() throws Exception {
        KeyGenerator gen = KeyGenerator.getInstance(ALGORITHM);
        gen.init(KEY_SIZE, new SecureRandom());
        this.secretKey = gen.generateKey();
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

    // ── Chiffrement AES-GCM ──────────────────────────────────────
    public String encrypt(String plaintext) throws Exception {
        byte[] iv = new byte[GCM_IV_LEN];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(CIPHER);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LEN, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

        byte[] encrypted = cipher.doFinal(plaintext.getBytes("UTF-8"));

        // Préfixe IV + données chiffrées
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    // ── Déchiffrement AES-GCM ────────────────────────────────────
    public String decrypt(String encryptedBase64) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedBase64);

        byte[] iv = new byte[GCM_IV_LEN];
        byte[] encrypted = new byte[combined.length - GCM_IV_LEN];
        System.arraycopy(combined, 0, iv, 0, GCM_IV_LEN);
        System.arraycopy(combined, GCM_IV_LEN, encrypted, 0, encrypted.length);

        Cipher cipher = Cipher.getInstance(CIPHER);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LEN, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, "UTF-8");
    }
}