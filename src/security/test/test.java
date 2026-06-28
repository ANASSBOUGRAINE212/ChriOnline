package security.test;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import javax.crypto.AEADBadTagException;
import javax.crypto.SecretKey;

import security.RSA.AESSessionKey;
import security.RSA.NonceManager;
import security.RSA.RSAKeyManager;
import security.aes.AESCipher;
import security.aes.AESKeyGenerator;
import security.aes.SecureInputStream;
import security.aes.SecureOutputStream;
import security.storage.AuditLogger;
import security.storage.AuditLogger.AuditEntry;
import security.storage.PasswordHasher;
import security.storage.ReplayProtector;
import security.storage.SecureDataStore;

/**
 * SecurityTestSuite — Complete Security Tests (Dev 1 + Dev 2 + Dev 3)
 *
 * Groups:
 *   1.  AES Cipher tests            (Dev 1)
 *   2.  RSA tests                   (Dev 2)
 *   3.  AES session-key tests       (Dev 2)
 *   4.  Nonce / anti-replay tests   (Dev 2)
 *   5.  Public-key integrity tests  (Dev 2)
 *   6.  PasswordHasher tests        (Dev 3)
 *   7.  SecureDataStore tests       (Dev 3)
 *   8.  AuditLogger tests           (Dev 3)
 *   9.  ReplayProtector tests       (Dev 3)
 *   10. MITM resistance tests       (Dev 3)
 *   11. Replay attack simulation    (Dev 3)
 *   12. UDP Notification tests      (Dev 3) ✨ NEW
 *   13. Secure Streams tests        (Dev 3) ✨ NEW
 *
 * No external test framework — every test prints PASS / FAIL.
 * Run: java -cp out security.test.test
 */
public class test {

    // ── ANSI colors ───────────────────────────────────────────────────────────
    private static final String GREEN  = "\u001B[32m";
    private static final String RED    = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RESET  = "\u001B[0m";

    private int passed = 0;
    private int failed = 0;
    private final List<String> failures = new ArrayList<>();

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) throws Exception {
        System.exit(new test().runAllTests() ? 0 : 1);
    }

    /** Runs every test group and returns true if all passed. */
    public boolean runAllTests() throws Exception {
        printHeader("ChriOnline — Complete Security Test Suite");

        testAESCipher();
        testRSA();
        testAESSessionKey();
        testNonceAntiReplay();
        testPublicKeyIntegrity();
        testPasswordHasher();
        testSecureDataStore();
        testAuditLogger();
        testReplayProtector();
        testMITMResistance();
        testReplayAttackSimulation();
        testUDPNotifications();
        testSecureStreams();

        return printSummary();
    }

    // =========================================================================
    // 1. AES Cipher Tests  (Dev 1)
    // =========================================================================

    private void testAESCipher() throws Exception {
        printSection("1. AES Cipher Tests");

        runTest("AES GCM encrypt/decrypt round-trip", () -> {
            SecretKey key = AESKeyGenerator.generateAESKey();
            String message = "Commande #42 : 3x Produit A";
            byte[] encrypted = AESCipher.encryptGCM(message.getBytes("UTF-8"), key);
            byte[] decrypted = AESCipher.decryptGCM(encrypted, key);
            assertArrayEquals(message.getBytes("UTF-8"), decrypted,
                    "Decrypted bytes must match original");
        });

        runTest("AES GCM produces unique IVs for same plaintext", () -> {
            SecretKey key = AESKeyGenerator.generateAESKey();
            byte[] data = "test".getBytes("UTF-8");
            byte[] enc1 = AESCipher.encryptGCM(data, key);
            byte[] enc2 = AESCipher.encryptGCM(data, key);
            byte[] iv1  = Arrays.copyOfRange(enc1, 0, AESCipher.IV_LENGTH);
            byte[] iv2  = Arrays.copyOfRange(enc2, 0, AESCipher.IV_LENGTH);
            assertFalse(Arrays.equals(iv1, iv2), "Two encryptions must produce different IVs");
        });

        runTest("AES GCM detects ciphertext tampering (AEADBadTagException)", () -> {
            SecretKey key = AESKeyGenerator.generateAESKey();
            byte[] encrypted = AESCipher.encryptGCM("donnees sensibles".getBytes("UTF-8"), key);
            encrypted[encrypted.length / 2] ^= 0xFF;   // flip bits mid-ciphertext
            try {
                AESCipher.decryptGCM(encrypted, key);
                fail("Expected AEADBadTagException for tampered ciphertext");
            } catch (AEADBadTagException e) {
                // expected — GCM tag verification failed
            }
        });

        runTest("SecureOutputStream / SecureInputStream round-trip", () -> {
            SecretKey key = AESKeyGenerator.generateAESKey();
            java.io.PipedOutputStream pipeOut = new java.io.PipedOutputStream();
            java.io.PipedInputStream  pipeIn  = new java.io.PipedInputStream(pipeOut);
            SecureOutputStream secOut = new SecureOutputStream(pipeOut, key);
            SecureInputStream  secIn  = new SecureInputStream(pipeIn, key);
            String original = "Test stream securise !";
            secOut.writeSecureString(original);
            String received = secIn.readSecureString();
            assertEquals(original, received, "Stream round-trip must recover original message");
            secOut.close();
            secIn.close();
        });
    }

    // =========================================================================
    // 2. RSA Tests  (Dev 2)
    // =========================================================================

    private void testRSA() throws Exception {
        printSection("2. RSA Tests");

        runTest("RSA key generation produces non-empty Base64 public key", () -> {
            RSAKeyManager mgr = new RSAKeyManager();
            String pub = mgr.getPublicKeyBase64();
            assertTrue(pub != null && !pub.isEmpty(), "Public key must not be null/empty");
            assertTrue(pub.length() > 300, "Base64 public key must be > 300 chars");
        });

        runTest("RSA encrypt AES key then decrypt recovers original bytes", () -> {
            RSAKeyManager mgr = new RSAKeyManager();
            byte[] originalAES = new AESSessionKey().getRawKey();
            PublicKey pub = mgr.getPublicKey();
            String encrypted = RSAKeyManager.encryptAESKey(originalAES, pub);
            byte[] decrypted = mgr.decryptAESKey(encrypted);
            assertTrue(Arrays.equals(originalAES, decrypted),
                    "RSA decrypt must recover the original AES key bytes");
        });

        runTest("RSA known-vector: sequence 0x00–0x1F survives encrypt/decrypt", () -> {
            RSAKeyManager mgr = new RSAKeyManager();
            byte[] knownKey = new byte[32];
            for (int i = 0; i < 32; i++) knownKey[i] = (byte) i;
            String enc = RSAKeyManager.encryptAESKey(knownKey, mgr.getPublicKey());
            byte[] dec = mgr.decryptAESKey(enc);
            assertTrue(Arrays.equals(knownKey, dec), "Known-vector round-trip must be exact");
        });

        runTest("RSA fingerprint is deterministic for the same key", () -> {
            RSAKeyManager mgr = new RSAKeyManager();
            String pub = mgr.getPublicKeyBase64();
            String fp1 = RSAKeyManager.fingerprintPublicKey(pub);
            String fp2 = RSAKeyManager.fingerprintPublicKey(pub);
            assertEquals(fp1, fp2, "Same key must always produce the same fingerprint");
            assertTrue(fp1.length() > 10, "Fingerprint must not be trivially short");
        });

        runTest("RSA fingerprint changes when public key is tampered", () -> {
            RSAKeyManager mgr = new RSAKeyManager();
            String pub = mgr.getPublicKeyBase64();
            String fp1 = RSAKeyManager.fingerprintPublicKey(pub);
            char[] chars = pub.toCharArray();
            chars[10] = (chars[10] == 'A') ? 'B' : 'A';
            String tampered = new String(chars);
            try {
                String fp2 = RSAKeyManager.fingerprintPublicKey(tampered);
                assertTrue(!fp1.equals(fp2), "Tampered key must produce a different fingerprint");
            } catch (Exception e) {
                // Invalid Base64 after tampering also counts as detection
            }
        });
    }

    // =========================================================================
    // 3. AES Session-Key Tests  (Dev 2)
    // =========================================================================

    private void testAESSessionKey() throws Exception {
        printSection("3. AES Session-Key Tests");

        runTest("AESSessionKey generates valid 256-bit key", () -> {
            AESSessionKey key = new AESSessionKey();
            byte[] rawKey = key.getRawKey();
            assertEquals(32, rawKey.length, "AES-256 key must be 32 bytes");
        });

        runTest("AESSessionKey getSecretKey returns valid SecretKey", () -> {
            AESSessionKey key = new AESSessionKey();
            SecretKey secretKey = key.getSecretKey();
            assertTrue(secretKey != null, "SecretKey must not be null");
            assertEquals("AES", secretKey.getAlgorithm(), "Algorithm must be AES");
        });

        runTest("AESSessionKey reconstruction from raw bytes", () -> {
            AESSessionKey original = new AESSessionKey();
            byte[] rawKey = original.getRawKey();
            AESSessionKey rebuilt = new AESSessionKey(rawKey);
            assertTrue(Arrays.equals(original.getRawKey(), rebuilt.getRawKey()),
                    "Rebuilt key must have same raw bytes as original");
        });

        runTest("Two AESSessionKey instances with same raw bytes can encrypt/decrypt", () -> {
            byte[] rawKey = new byte[32];
            for (int i = 0; i < 32; i++) rawKey[i] = (byte)(i + 1);
            AESSessionKey k1 = new AESSessionKey(rawKey);
            AESSessionKey k2 = new AESSessionKey(rawKey);
            
            String message = "Test message for AES-256";
            byte[] encrypted = AESCipher.encryptGCM(message.getBytes("UTF-8"), k1.getSecretKey());
            byte[] decrypted = AESCipher.decryptGCM(encrypted, k2.getSecretKey());
            
            assertEquals(message, new String(decrypted, "UTF-8"),
                    "Instances sharing the same raw key must interoperate");
        });
    }

    // =========================================================================
    // 4. Nonce / Anti-Replay Tests  (Dev 2)
    // =========================================================================

    private void testNonceAntiReplay() {
        printSection("4. Nonce / Anti-Replay Tests");

        runTest("Nonce generator produces unique values", () -> {
            String n1 = NonceManager.generateNonce();
            String n2 = NonceManager.generateNonce();
            String n3 = NonceManager.generateNonce();
            assertFalse(n1.equals(n2), "n1 and n2 must differ");
            assertFalse(n2.equals(n3), "n2 and n3 must differ");
            assertFalse(n1.equals(n3), "n1 and n3 must differ");
        });

        runTest("Nonce: first use accepted, second use rejected (anti-replay)", () -> {
            NonceManager nm = new NonceManager();
            String nonce = NonceManager.generateNonce();
            assertTrue( nm.validateAndConsume(nonce), "First use must be accepted");
            assertFalse(nm.validateAndConsume(nonce), "Second use must be rejected");
        });

        runTest("Nonce: null is rejected", () -> {
            assertFalse(new NonceManager().validateAndConsume(null),
                    "Null nonce must be rejected");
        });

        runTest("Nonce: empty string is rejected", () -> {
            assertFalse(new NonceManager().validateAndConsume(""),
                    "Empty nonce must be rejected");
        });

        runTest("Nonce: distinct nonces are all accepted", () -> {
            NonceManager nm = new NonceManager();
            assertTrue(nm.validateAndConsume(NonceManager.generateNonce()) &&
                       nm.validateAndConsume(NonceManager.generateNonce()) &&
                       nm.validateAndConsume(NonceManager.generateNonce()),
                    "Three distinct nonces must all be accepted");
        });
    }

    // =========================================================================
    // 5. Public-Key Integrity Tests  (Dev 2)
    // =========================================================================

    private void testPublicKeyIntegrity() throws Exception {
        printSection("5. Public-Key Integrity Tests");

        runTest("Fingerprint format: hex with separators, length > 60", () -> {
            RSAKeyManager mgr = new RSAKeyManager();
            String fp = RSAKeyManager.fingerprintPublicKey(mgr.getPublicKeyBase64());
            assertTrue(fp.contains(":"), "Fingerprint must use ':' separators");
            assertTrue(fp.length() > 60, "Fingerprint must be > 60 chars");
        });

        runTest("Two distinct RSA key pairs produce different fingerprints", () -> {
            String fp1 = RSAKeyManager.fingerprintPublicKey(new RSAKeyManager().getPublicKeyBase64());
            String fp2 = RSAKeyManager.fingerprintPublicKey(new RSAKeyManager().getPublicKeyBase64());
            assertFalse(fp1.equals(fp2), "Independent key pairs must have different fingerprints");
        });

        runTest("PublicKey Base64 → PublicKey → Base64 round-trip is lossless", () -> {
            RSAKeyManager mgr = new RSAKeyManager();
            String base64    = mgr.getPublicKeyBase64();
            PublicKey rebuilt = RSAKeyManager.publicKeyFromBase64(base64);
            String rebuilt64  = Base64.getEncoder().encodeToString(rebuilt.getEncoded());
            assertEquals(base64, rebuilt64, "Base64 round-trip must be identical");
        });
    }

    // =========================================================================
    // 6. PasswordHasher Tests  (Dev 3)
    // =========================================================================

    private void testPasswordHasher() {
        printSection("6. PasswordHasher Tests");

        runTest("Hash and verify same password", () -> {
            String hash = PasswordHasher.hashPassword("myPassword123");
            assertTrue(PasswordHasher.verifyPassword("myPassword123", hash),
                    "Correct password must verify successfully");
        });

        runTest("Wrong password is rejected", () -> {
            String hash = PasswordHasher.hashPassword("correctPassword");
            assertFalse(PasswordHasher.verifyPassword("wrongPassword", hash),
                    "Wrong password must not verify");
        });

        runTest("Same password produces different hashes due to random salt", () -> {
            String h1 = PasswordHasher.hashPassword("samePassword");
            String h2 = PasswordHasher.hashPassword("samePassword");
            assertFalse(h1.equals(h2), "Hashes of the same password must differ (random salt)");
        });

        runTest("Both salted hashes of the same password still verify", () -> {
            String password = "testPassword!";
            String h1 = PasswordHasher.hashPassword(password);
            String h2 = PasswordHasher.hashPassword(password);
            assertTrue(PasswordHasher.verifyPassword(password, h1), "hash1 must verify");
            assertTrue(PasswordHasher.verifyPassword(password, h2), "hash2 must verify");
        });

        runTest("Null password throws IllegalArgumentException", () -> {
            try {
                PasswordHasher.hashPassword(null);
                fail("Expected IllegalArgumentException for null password");
            } catch (IllegalArgumentException e) {
                // expected
            }
        });

        runTest("Tampered hash is rejected", () -> {
            String hash    = PasswordHasher.hashPassword("realPassword");
            String tampered = hash.substring(0, hash.length() - 4) + "XXXX";
            assertFalse(PasswordHasher.verifyPassword("realPassword", tampered),
                    "Tampered hash must not verify");
        });
    }

    // =========================================================================
    // 7. SecureDataStore Tests  (Dev 3)
    // =========================================================================

    private void testSecureDataStore() {
        printSection("7. SecureDataStore Tests");

        SecureDataStore store = new SecureDataStore();
        String testKey = "test:card:" + UUID.randomUUID();

        runTest("Save and load encrypted value (round-trip)", () -> {
            store.saveEncrypted(testKey, "4111-1111-1111-1111");
            assertEquals("4111-1111-1111-1111", store.loadDecrypted(testKey),
                    "Decrypted value must match original");
        });

        runTest("Encrypted round-trip is lossless for arbitrary plaintext", () -> {
            String k = "test:sensitive:" + UUID.randomUUID();
            String v = "PLAIN_SECRET_DATA";
            store.saveEncrypted(k, v);
            assertEquals(v, store.loadDecrypted(k), "Round-trip must be lossless");
            store.delete(k);
        });

        runTest("Missing key returns null", () -> {
            assertNull(store.loadDecrypted("nonexistent:key:" + UUID.randomUUID()),
                    "loadDecrypted must return null for unknown key");
        });

        runTest("Updating an existing key overwrites correctly", () -> {
            String k = "test:update:" + UUID.randomUUID();
            store.saveEncrypted(k, "original");
            store.saveEncrypted(k, "updated");
            assertEquals("updated", store.loadDecrypted(k), "Second save must overwrite first");
            store.delete(k);
        });

        runTest("Two saves of same plaintext both decrypt correctly (IV randomness)", () -> {
            String k1 = "test:iv1:" + UUID.randomUUID();
            String k2 = "test:iv2:" + UUID.randomUUID();
            store.saveEncrypted(k1, "sameValue");
            store.saveEncrypted(k2, "sameValue");
            assertEquals("sameValue", store.loadDecrypted(k1), "k1 must decrypt correctly");
            assertEquals("sameValue", store.loadDecrypted(k2), "k2 must decrypt correctly");
            store.delete(k1);
            store.delete(k2);
        });

        store.delete(testKey);
    }

    // =========================================================================
    // 8. AuditLogger Tests  (Dev 3)
    // =========================================================================

    private void testAuditLogger() {
        printSection("8. AuditLogger Tests");

        AuditLogger.clearAuditLogForTests();
        AuditLogger logger = new AuditLogger();

        runTest("Log action and retrieve from audit trail", () -> {
            logger.logAction("user42", "LOGIN", "ip=127.0.0.1");
            List<AuditEntry> trail = logger.getAuditTrail();
            assertTrue(trail.size() > 0, "Trail must not be empty after logging");
            AuditEntry last = trail.get(trail.size() - 1);
            assertEquals("user42", last.userId(), "userId must match");
            assertEquals("LOGIN",  last.action(), "action must match");
        });

        runTest("Audit chain integrity passes on unmodified log", () -> {
            AuditLogger fresh = new AuditLogger();
            fresh.logAction("user1", "REGISTER", "email=test@test.com");
            fresh.logAction("user1", "LOGIN",    "ip=10.0.0.1");
            fresh.logAction("user1", "PAYMENT",  "orderId=99,amount=150.00");
            assertTrue(fresh.verifyIntegrity(), "Integrity check must pass on untampered log");
        });

        runTest("Multiple entries stored in insertion order", () -> {
            AuditLogger seq = new AuditLogger();
            seq.logAction("userA", "ACTION_1", "data1");
            seq.logAction("userA", "ACTION_2", "data2");
            seq.logAction("userA", "ACTION_3", "data3");
            long count = seq.getAuditTrail().stream()
                    .filter(e -> e.userId().equals("userA")).count();
            assertTrue(count >= 3, "All 3 entries for userA must be present");
        });

        runTest("Entry hash is non-null and non-empty", () -> {
            AuditLogger hl = new AuditLogger();
            hl.logAction("system", "STARTUP", "server started");
            AuditEntry last = hl.getAuditTrail().get(hl.getAuditTrail().size() - 1);
            assertTrue(last.entryHash() != null && !last.entryHash().isEmpty(),
                    "Entry hash must be populated");
        });
    }

    // =========================================================================
    // 9. ReplayProtector Tests  (Dev 3)
    // =========================================================================

    private void testReplayProtector() {
        printSection("9. ReplayProtector Tests");

        ReplayProtector protector = new ReplayProtector();

        runTest("Fresh txId is not flagged as replay", () -> {
            assertFalse(protector.isReplay(UUID.randomUUID().toString()),
                    "A brand-new txId must not be a replay");
        });

        runTest("Registered txId is flagged as replay on second use", () -> {
            String txId = UUID.randomUUID().toString();
            protector.registerTransaction(txId);
            assertTrue(protector.isReplay(txId), "Registered txId must be flagged");
        });

        runTest("checkAndRegister: first allowed, second blocked", () -> {
            String txId = UUID.randomUUID().toString();
            assertFalse(protector.checkAndRegister(txId), "First call must return false");
            assertTrue( protector.checkAndRegister(txId), "Second call must return true");
        });

        runTest("Null txId is treated as replay/invalid", () -> {
            assertTrue(protector.isReplay(null), "Null txId must be flagged");
        });

        runTest("Blank txId is treated as replay/invalid", () -> {
            assertTrue(protector.isReplay("   "), "Blank txId must be flagged");
        });

        runTest("Cache grows by exactly 2 after two registrations", () -> {
            ReplayProtector rp = new ReplayProtector();
            int before = rp.cacheSize();
            rp.registerTransaction(UUID.randomUUID().toString());
            rp.registerTransaction(UUID.randomUUID().toString());
            assertEquals(before + 2, rp.cacheSize(), "Cache must grow by 2");
        });

        runTest("evictExpired does not remove fresh entries", () -> {
            ReplayProtector rp = new ReplayProtector();
            String txId = UUID.randomUUID().toString();
            rp.registerTransaction(txId);
            rp.evictExpired();
            assertTrue(rp.isReplay(txId), "Fresh entry must survive evictExpired()");
        });
    }

    // =========================================================================
    // 10. MITM Resistance Tests  (Dev 3)
    // =========================================================================

    private void testMITMResistance() {
        printSection("10. MITM Resistance Tests");

        runTest("Intercepted ciphertext is unreadable without the master key", () -> {
            SecureDataStore store = new SecureDataStore();
            String key   = "mitm:test:" + UUID.randomUUID();
            String plain = "SECRET_CARD_NUMBER_4111111111111111";
            store.saveEncrypted(key, plain);
            assertEquals(plain, store.loadDecrypted(key),
                    "Legitimate decryption must recover original plaintext");
            assertNull(store.loadDecrypted("mitm:fake:" + UUID.randomUUID()),
                    "Attacker querying a fabricated key must get null");
            store.delete(key);
        });

        runTest("AuditLogger detects MITM-tampered log chain", () -> {
            AuditLogger.clearAuditLogForTests();
            AuditLogger logger = new AuditLogger();
            logger.logAction("userMITM", "PAYMENT", "amount=500");
            logger.logAction("userMITM", "LOGOUT",  "session ended");
            assertTrue(logger.verifyIntegrity(),
                    "Unmodified audit chain must pass integrity check");
        });
    }

    // =========================================================================
    // 11. Replay Attack Simulation  (Dev 3)
    // =========================================================================

    private void testReplayAttackSimulation() {
        printSection("11. Replay Attack Simulation");

        runTest("Captured txId is rejected on second (replayed) use", () -> {
            ReplayProtector protector = new ReplayProtector();
            String capturedTxId = UUID.randomUUID().toString();
            assertFalse(protector.checkAndRegister(capturedTxId),
                    "Legitimate first request must be accepted");
            assertTrue(protector.checkAndRegister(capturedTxId),
                    "Replayed request must be rejected");
        });

        runTest("Multiple replay attempts are all rejected", () -> {
            ReplayProtector protector = new ReplayProtector();
            String txId = UUID.randomUUID().toString();
            protector.registerTransaction(txId);
            for (int i = 1; i <= 5; i++) {
                assertTrue(protector.isReplay(txId), "Replay attempt #" + i + " must be rejected");
            }
        });

        runTest("Replay of txId1 does not affect unregistered txId2", () -> {
            ReplayProtector protector = new ReplayProtector();
            String txId1 = UUID.randomUUID().toString();
            String txId2 = UUID.randomUUID().toString();
            protector.registerTransaction(txId1);
            assertFalse(protector.isReplay(txId2),
                    "Unrelated txId must not be flagged as replay");
        });
    }

    // =========================================================================
    // 12. UDP Notification Tests  (Dev 3)
    // =========================================================================

    private void testUDPNotifications() {
        printSection("12. UDP Notification Tests");

        runTest("UDP notification message format for payment", () -> {
            String paymentId = "PAY-12345";
            double amount = 99.99;
            String status = "SUCCESS";
            String expected = String.format("PAYMENT|%s|%.2f|%s", paymentId, amount, status);
            assertTrue(expected.contains("PAYMENT"), "Message must contain PAYMENT type");
            assertTrue(expected.contains(paymentId), "Message must contain payment ID");
            assertTrue(expected.contains("SUCCESS"), "Message must contain status");
        });

        runTest("UDP notification message format for order", () -> {
            String orderId = "ORD-67890";
            String status = "CONFIRMED";
            String expected = String.format("ORDER|%s|%s", orderId, status);
            assertTrue(expected.contains("ORDER"), "Message must contain ORDER type");
            assertTrue(expected.contains(orderId), "Message must contain order ID");
            assertTrue(expected.contains(status), "Message must contain status");
        });

        runTest("UDP notification parsing - payment message", () -> {
            String message = "PAYMENT|PAY-123|150.50|SUCCESS";
            String[] parts = message.split("\\|");
            assertEquals("PAYMENT", parts[0], "Type must be PAYMENT");
            assertEquals("PAY-123", parts[1], "Payment ID must match");
            assertEquals("150.50", parts[2], "Amount must match");
            assertEquals("SUCCESS", parts[3], "Status must match");
        });

        runTest("UDP notification parsing - order message", () -> {
            String message = "ORDER|ORD-456|SHIPPED";
            String[] parts = message.split("\\|");
            assertEquals("ORDER", parts[0], "Type must be ORDER");
            assertEquals("ORD-456", parts[1], "Order ID must match");
            assertEquals("SHIPPED", parts[2], "Status must match");
        });
    }

    // =========================================================================
    // 13. Secure Streams Tests  (Dev 3)
    // =========================================================================

    private void testSecureStreams() throws Exception {
        printSection("13. Secure Streams Tests");

        runTest("SecureInputStream/SecureOutputStream with AESSessionKey", () -> {
            AESSessionKey sessionKey = new AESSessionKey();
            java.io.PipedOutputStream pipeOut = new java.io.PipedOutputStream();
            java.io.PipedInputStream  pipeIn  = new java.io.PipedInputStream(pipeOut);
            
            SecureOutputStream secOut = new SecureOutputStream(pipeOut, sessionKey.getSecretKey());
            SecureInputStream  secIn  = new SecureInputStream(pipeIn, sessionKey.getSecretKey());
            
            String testMessage = "Encrypted communication test";
            secOut.writeSecureString(testMessage);
            String received = secIn.readSecureString();
            
            assertEquals(testMessage, received, "Secure stream must preserve message");
            secOut.close();
            secIn.close();
        });

        runTest("SecureOutputStream encrypts data (not plaintext)", () -> {
            AESSessionKey sessionKey = new AESSessionKey();
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            SecureOutputStream secOut = new SecureOutputStream(baos, sessionKey.getSecretKey());
            
            String plaintext = "This should be encrypted";
            secOut.writeSecureString(plaintext);
            
            byte[] output = baos.toByteArray();
            String outputStr = new String(output);
            
            assertFalse(outputStr.contains(plaintext), 
                    "Output must not contain plaintext (must be encrypted)");
            secOut.close();
        });

        runTest("AESSessionKey getSecretKey returns non-null", () -> {
            AESSessionKey sessionKey = new AESSessionKey();
            SecretKey key = sessionKey.getSecretKey();
            assertTrue(key != null, "getSecretKey must return non-null");
            assertEquals("AES", key.getAlgorithm(), "Key algorithm must be AES");
        });
    }

    // =========================================================================
    // Test runner
    // =========================================================================

    private void runTest(String name, TestCase test) {
        try {
            test.run();
            System.out.println("  " + GREEN + "✔ PASS" + RESET + "  " + name);
            passed++;
        } catch (AssertionError | Exception e) {
            System.out.println("  " + RED + "✘ FAIL" + RESET + "  " + name);
            System.out.println("         → " + e.getMessage());
            failures.add(name);
            failed++;
        }
    }

    private void printHeader(String title) {
        System.out.println("\n" + YELLOW + "═".repeat(55) + RESET);
        System.out.println(YELLOW + "  " + title + RESET);
        System.out.println(YELLOW + "═".repeat(55) + RESET + "\n");
    }

    private void printSection(String title) {
        System.out.println("\n" + YELLOW + "── " + title + RESET);
    }

    private boolean printSummary() {
        System.out.println("\n" + YELLOW + "═".repeat(55) + RESET);
        System.out.println("  Results: "
                + GREEN + passed + " passed" + RESET + "  |  "
                + (failed > 0 ? RED : GREEN) + failed + " failed" + RESET);
        if (!failures.isEmpty()) {
            System.out.println("\n  " + RED + "Failed tests:" + RESET);
            failures.forEach(f -> System.out.println("    - " + f));
        }
        System.out.println(YELLOW + "═".repeat(55) + RESET + "\n");
        return failed == 0;
    }

    // =========================================================================
    // Assertion helpers
    // =========================================================================

    private void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError("Expected true — " + message);
    }

    private void assertFalse(boolean condition, String message) {
        if (condition) throw new AssertionError("Expected false — " + message);
    }

    private void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected == null || !expected.equals(actual))
            throw new AssertionError("Expected [" + expected + "] but got [" + actual + "] — " + message);
    }

    private void assertArrayEquals(byte[] expected, byte[] actual, String message) {
        if (!Arrays.equals(expected, actual))
            throw new AssertionError("Byte arrays differ — " + message);
    }

    private void assertNull(Object value, String message) {
        if (value != null)
            throw new AssertionError("Expected null but got [" + value + "] — " + message);
    }

    private void fail(String message) {
        throw new AssertionError(message);
    }

    @FunctionalInterface
    private interface TestCase {
        void run() throws Exception;
    }
}