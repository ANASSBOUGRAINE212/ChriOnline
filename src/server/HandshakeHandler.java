package server;

import security.RSA.AESSessionKey;
import security.RSA.NonceManager;
import security.RSA.RSAKeyManager;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Backend Réseau — Gestion du handshake initial côté serveur.
 *
 * Protocole :
 *   1. CLIENT  → HELLO          { nonce_client }
 *   2. SERVER  → PUBLIC_KEY     { publicKey, nonce_server, fingerprint }
 *   3. CLIENT  → AES_KEY        { encryptedAES, nonce_client2 }
 *   4. SERVER  → HANDSHAKE_OK   { sessionId }
 */
public class HandshakeHandler {

    private final RSAKeyManager  rsaKeyManager;
    private final NonceManager   nonceManager;

    public HandshakeHandler(RSAKeyManager rsaKeyManager) {
        this.rsaKeyManager = rsaKeyManager;
        this.nonceManager  = NonceManager.getInstance();
    }

    /**
     * Exécute le handshake complet avec un client.
     * Retourne la clé AES de session si succès, null sinon.
     */
    public AESSessionKey performHandshake(ObjectInputStream in, ObjectOutputStream out) {
        try {
            System.out.println("🤝 Handshake RSA démarré...");

            // ── Étape 1 : Recevoir HELLO + nonce client ───────────
            @SuppressWarnings("unchecked")
            Map<String, String> hello = (Map<String, String>) in.readObject();

            if (!"HELLO".equals(hello.get("type"))) {
                sendError(out, "Message HELLO attendu");
                return null;
            }

            String nonceClient = hello.get("nonce");
            if (!nonceManager.validateAndConsume(nonceClient)) {
                sendError(out, "Nonce invalide ou rejoué");
                return null;
            }

            // ── Étape 2 : Envoyer clé publique RSA + nonce serveur ─
            String nonceServer  = NonceManager.generateNonce();
            String publicKey    = rsaKeyManager.getPublicKeyBase64();
            String fingerprint  = RSAKeyManager.fingerprintPublicKey(publicKey);

            Map<String, String> keyMsg = new HashMap<>();
            keyMsg.put("type",        "PUBLIC_KEY");
            keyMsg.put("publicKey",   publicKey);
            keyMsg.put("nonce",       nonceServer);
            keyMsg.put("fingerprint", fingerprint);
            out.writeObject(keyMsg);
            out.flush();
            System.out.println("📤 Clé publique RSA envoyée (fingerprint: " + fingerprint.substring(0, 19) + "...)");

            // ── Étape 3 : Recevoir clé AES chiffrée ──────────────
            @SuppressWarnings("unchecked")
            Map<String, String> aesMsg = (Map<String, String>) in.readObject();

            if (!"AES_KEY".equals(aesMsg.get("type"))) {
                sendError(out, "Message AES_KEY attendu");
                return null;
            }

            // Validation anti-rejeu du second nonce client
            String nonceClient2 = aesMsg.get("nonce");
            if (!nonceManager.validateAndConsume(nonceClient2)) {
                sendError(out, "Nonce AES invalide ou rejoué");
                return null;
            }

            // Déchiffrement clé AES avec clé privée RSA
            String encryptedAES = aesMsg.get("encryptedAES");
            byte[] aesKeyBytes  = rsaKeyManager.decryptAESKey(encryptedAES);
            AESSessionKey sessionKey = new AESSessionKey(aesKeyBytes);

            // ── Étape 4 : Confirmer le handshake ─────────────────
            String sessionId = NonceManager.generateNonce().substring(0, 16);
            Map<String, String> ack = new HashMap<>();
            ack.put("type",      "HANDSHAKE_OK");
            ack.put("sessionId", sessionId);
            out.writeObject(ack);
            out.flush();

            System.out.println("✅ Handshake RSA réussi — session: " + sessionId);
            return sessionKey;

        } catch (Exception e) {
            System.err.println("❌ Erreur handshake: " + e.getMessage());
            try { sendError(out, "Handshake échoué: " + e.getMessage()); } catch (Exception ignored) {}
            return null;
        }
    }

    private void sendError(ObjectOutputStream out, String msg) throws Exception {
        Map<String, String> err = new HashMap<>();
        err.put("type",    "ERROR");
        err.put("message", msg);
        out.writeObject(err);
        out.flush();
        System.err.println("❌ Handshake error envoyé: " + msg);
    }
}