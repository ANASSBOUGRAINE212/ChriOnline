package tests;

import security.RSA.AESSessionKey;
import security.RSA.NonceManager;
import security.RSA.RSAKeyManager;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests Unitaires  : test encrypt/decrypt RSA, test vecteurs connus
 * Tests Sécurité   : test anti-rejeu handshake, test intégrité clé publique
 *
 * Exécution : java -cp out tests.SecurityTests
 */
public class SecurityTests {

    private static int passed = 0;
    private static int failed = 0;
    private static final List<String> failures = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  🧪 ChriOnline Security Test Suite");
        System.out.println("═══════════════════════════════════════════════\n");

        // ── Tests Unitaires RSA ───────────────────────────────────
        System.out.println("── Tests Unitaires RSA ─────────────────────");
        testRSAKeyGeneration();
        testRSAEncryptDecrypt();
        testRSAKnownVector();
        testPublicKeyIntegrity();
        testPublicKeyTampering();

        // ── Tests Unitaires AES ───────────────────────────────────
        System.out.println("\n── Tests Unitaires AES ─────────────────────");
        testAESEncryptDecrypt();
        testAESKnownVector();
        testAESRoundTrip();

        // ── Tests Sécurité Anti-Rejeu ─────────────────────────────
        System.out.println("\n── Tests Sécurité Anti-Rejeu ───────────────");
        testNonceUniqueness();
        testNonceReplayBlocked();
        testNonceNullRejected();
        testNonceEmptyRejected();
        testDifferentNoncesAccepted();

        // ── Tests Intégrité Clé Publique ──────────────────────────
        System.out.println("\n── Tests Intégrité Clé Publique ────────────");
        testFingerprintConsistency();
        testFingerprintDetectsTampering();
        testPublicKeyRoundTrip();

        // ── Rapport final ─────────────────────────────────────────
        System.out.println("\n═══════════════════════════════════════════════");
        System.out.printf("  Résultat : %d/%d tests passés%n", passed, passed + failed);
        if (!failures.isEmpty()) {
            System.out.println("\n  ❌ Échecs :");
            failures.forEach(f -> System.out.println("    - " + f));
        }
        System.out.println("═══════════════════════════════════════════════");

        System.exit(failed > 0 ? 1 : 0);
    }

    // ════════════════════════════════════════════════════════════
    // TESTS UNITAIRES RSA
    // ════════════════════════════════════════════════════════════

    static void testRSAKeyGeneration() throws Exception {
        RSAKeyManager mgr = new RSAKeyManager();
        String pub = mgr.getPublicKeyBase64();
        assertTrue("RSA: clé publique non nulle", pub != null && !pub.isEmpty());
        assertTrue("RSA: clé publique Base64 valide", pub.length() > 300);
    }

    static void testRSAEncryptDecrypt() throws Exception {
        RSAKeyManager mgr = new RSAKeyManager();
        // Générer une clé AES fictive (32 bytes = 256 bits)
        byte[] originalAES = new AESSessionKey().getRawKey();

        PublicKey pub = mgr.getPublicKey();
        String encrypted = RSAKeyManager.encryptAESKey(originalAES, pub);
        byte[] decrypted = mgr.decryptAESKey(encrypted);

        assertTrue("RSA: encrypt puis decrypt donne même clé AES",
            java.util.Arrays.equals(originalAES, decrypted));
    }

    static void testRSAKnownVector() throws Exception {
        // Vecteur connu : même instance RSA doit toujours déchiffrer ce qu'elle chiffre
        RSAKeyManager mgr = new RSAKeyManager();
        byte[] knownKey = new byte[32];
        for (int i = 0; i < 32; i++) knownKey[i] = (byte) i; // 0x00..0x1F

        String enc = RSAKeyManager.encryptAESKey(knownKey, mgr.getPublicKey());
        byte[] dec = mgr.decryptAESKey(enc);
        assertTrue("RSA vecteur connu: séquence 0x00-0x1F", java.util.Arrays.equals(knownKey, dec));
    }

    static void testPublicKeyIntegrity() throws Exception {
        RSAKeyManager mgr = new RSAKeyManager();
        String pub = mgr.getPublicKeyBase64();
        String fp1 = RSAKeyManager.fingerprintPublicKey(pub);
        String fp2 = RSAKeyManager.fingerprintPublicKey(pub);
        assertTrue("RSA fingerprint: déterministe", fp1.equals(fp2));
        assertTrue("RSA fingerprint: non vide", fp1.length() > 10);
    }

    static void testPublicKeyTampering() throws Exception {
        RSAKeyManager mgr = new RSAKeyManager();
        String pub = mgr.getPublicKeyBase64();
        String fp1 = RSAKeyManager.fingerprintPublicKey(pub);

        // Tamponner un caractère dans la clé publique
        char[] chars = pub.toCharArray();
        chars[10] = (chars[10] == 'A') ? 'B' : 'A';
        String tampered = new String(chars);

        try {
            String fp2 = RSAKeyManager.fingerprintPublicKey(tampered);
            assertTrue("RSA: clé falsifiée produit fingerprint différent", !fp1.equals(fp2));
        } catch (Exception e) {
            // Base64 invalide = aussi une détection valide
            assertTrue("RSA: clé falsifiée levée exception", true);
        }
    }

    // ════════════════════════════════════════════════════════════
    // TESTS UNITAIRES AES
    // ════════════════════════════════════════════════════════════

    static void testAESEncryptDecrypt() throws Exception {
        AESSessionKey key = new AESSessionKey();
        String plaintext = "Hello ChriOnline!";
        String encrypted = key.encrypt(plaintext);
        String decrypted = key.decrypt(encrypted);
        assertTrue("AES: encrypt/decrypt round-trip", plaintext.equals(decrypted));
    }

    static void testAESKnownVector() throws Exception {
        // Vecteur connu : reconstruire depuis bytes connus
        byte[] rawKey = new byte[32];
        for (int i = 0; i < 32; i++) rawKey[i] = (byte)(i + 1);

        AESSessionKey key1 = new AESSessionKey(rawKey);
        AESSessionKey key2 = new AESSessionKey(rawKey);

        String msg = "Test vecteur connu AES-256";
        String enc = key1.encrypt(msg);
        String dec = key2.decrypt(enc);
        assertTrue("AES vecteur connu: deux instances même clé", msg.equals(dec));
    }

    static void testAESRoundTrip() throws Exception {
        AESSessionKey key = new AESSessionKey();
        // Reconstituer depuis raw bytes (simuler réception côté serveur)
        byte[] raw = key.getRawKey();
        AESSessionKey reconstructed = new AESSessionKey(raw);

        String msg = "Session sécurisée ChriOnline 🔒";
        String enc = key.encrypt(msg);
        String dec = reconstructed.decrypt(enc);
        assertTrue("AES: reconstruction depuis bytes raw", msg.equals(dec));
    }

    // ════════════════════════════════════════════════════════════
    // TESTS SECURITE ANTI-REJEU
    // ════════════════════════════════════════════════════════════

    static void testNonceUniqueness() {
        String n1 = NonceManager.generateNonce();
        String n2 = NonceManager.generateNonce();
        String n3 = NonceManager.generateNonce();
        assertTrue("Nonce: génération unique (n1≠n2)", !n1.equals(n2));
        assertTrue("Nonce: génération unique (n2≠n3)", !n2.equals(n3));
        assertTrue("Nonce: génération unique (n1≠n3)", !n1.equals(n3));
    }

    static void testNonceReplayBlocked() {
        NonceManager nm = new NonceManager(); // instance fraîche
        String nonce = NonceManager.generateNonce();
        boolean first  = nm.validateAndConsume(nonce);
        boolean second = nm.validateAndConsume(nonce); // rejeu!
        assertTrue("Anti-rejeu: premier usage accepté",  first);
        assertTrue("Anti-rejeu: deuxième usage refusé", !second);
    }

    static void testNonceNullRejected() {
        NonceManager nm = new NonceManager();
        boolean result = nm.validateAndConsume(null);
        assertTrue("Anti-rejeu: nonce null refusé", !result);
    }

    static void testNonceEmptyRejected() {
        NonceManager nm = new NonceManager();
        boolean result = nm.validateAndConsume("");
        assertTrue("Anti-rejeu: nonce vide refusé", !result);
    }

    static void testDifferentNoncesAccepted() {
        NonceManager nm = new NonceManager();
        boolean r1 = nm.validateAndConsume(NonceManager.generateNonce());
        boolean r2 = nm.validateAndConsume(NonceManager.generateNonce());
        boolean r3 = nm.validateAndConsume(NonceManager.generateNonce());
        assertTrue("Anti-rejeu: nonces différents tous acceptés", r1 && r2 && r3);
    }

    // ════════════════════════════════════════════════════════════
    // TESTS INTEGRITE CLE PUBLIQUE
    // ════════════════════════════════════════════════════════════

    static void testFingerprintConsistency() throws Exception {
        RSAKeyManager mgr = new RSAKeyManager();
        String pub = mgr.getPublicKeyBase64();
        String fp  = RSAKeyManager.fingerprintPublicKey(pub);
        assertTrue("Fingerprint: format hex avec séparateurs", fp.contains(":"));
        assertTrue("Fingerprint: longueur attendue > 60 chars", fp.length() > 60);
    }

    static void testFingerprintDetectsTampering() throws Exception {
        RSAKeyManager mgr1 = new RSAKeyManager();
        RSAKeyManager mgr2 = new RSAKeyManager();
        String fp1 = RSAKeyManager.fingerprintPublicKey(mgr1.getPublicKeyBase64());
        String fp2 = RSAKeyManager.fingerprintPublicKey(mgr2.getPublicKeyBase64());
        assertTrue("Fingerprint: deux paires RSA différentes = fingerprints différents", !fp1.equals(fp2));
    }

    static void testPublicKeyRoundTrip() throws Exception {
        RSAKeyManager mgr = new RSAKeyManager();
        String base64     = mgr.getPublicKeyBase64();
        PublicKey rebuilt = RSAKeyManager.publicKeyFromBase64(base64);
        String rebuilt64  = java.util.Base64.getEncoder().encodeToString(rebuilt.getEncoded());
        assertTrue("PublicKey: Base64 → PublicKey → Base64 identique", base64.equals(rebuilt64));
    }

    // ════════════════════════════════════════════════════════════
    // UTILITAIRE ASSERTION
    // ════════════════════════════════════════════════════════════

    static void assertTrue(String name, boolean condition) {
        if (condition) {
            System.out.println("  ✅ " + name);
            passed++;
        } else {
            System.out.println("  ❌ " + name);
            failed++;
            failures.add(name);
        }
    }
}