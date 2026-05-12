package client;

import security.RSA.AESSessionKey;
import security.RSA.NonceManager;
import security.RSA.RSAKeyManager;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * Frontend — Chiffrement clé AES côté client + envoi au serveur.
 * Gère aussi la vérification d'intégrité de la clé publique reçue.
 */
public class ClientHandShake {

    private AESSessionKey sessionKey;
    private String        sessionId;
    private String        serverFingerprint;
    private boolean       handshakeComplete = false;

    // Listener pour mettre à jour l'UI (indicateur cadenas)
    public interface HandshakeListener {
        void onHandshakeSuccess(String sessionId, String fingerprint);
        void onHandshakeFailure(String reason);
        void onStatusUpdate(String status);
    }

    private HandshakeListener listener;

    public ClientHandShake(HandshakeListener listener) {
        this.listener = listener;
    }

    /**
     * Exécute le handshake RSA complet côté client.
     * Retourne true si succès.
     */
    public boolean performHandshake(ObjectInputStream in, ObjectOutputStream out) {
        try {
            if (listener != null) listener.onStatusUpdate("🔌 Démarrage du handshake RSA...");

            // ── Étape 1 : Envoyer HELLO + nonce client ────────────
            String nonceClient = NonceManager.generateNonce();
            Map<String, String> hello = new HashMap<>();
            hello.put("type",  "HELLO");
            hello.put("nonce", nonceClient);
            out.writeObject(hello);
            out.flush();
            if (listener != null) listener.onStatusUpdate("📤 HELLO envoyé...");

            // ── Étape 2 : Recevoir clé publique RSA du serveur ────
            @SuppressWarnings("unchecked")
            Map<String, String> keyMsg = (Map<String, String>) in.readObject();

            if (!"PUBLIC_KEY".equals(keyMsg.get("type"))) {
                String reason = "Réponse inattendue: " + keyMsg.get("type");
                if (listener != null) listener.onHandshakeFailure(reason);
                return false;
            }

            String publicKeyBase64   = keyMsg.get("publicKey");
            String receivedFingerprint = keyMsg.get("fingerprint");

            // Vérification intégrité clé publique
            String computedFingerprint = RSAKeyManager.fingerprintPublicKey(publicKeyBase64);
            if (!computedFingerprint.equals(receivedFingerprint)) {
                String reason = "⚠️ Intégrité clé publique échouée — possible attaque MITM!";
                System.err.println(reason);
                if (listener != null) listener.onHandshakeFailure(reason);
                return false;
            }
            this.serverFingerprint = receivedFingerprint;
            if (listener != null) listener.onStatusUpdate("✅ Clé publique vérifiée");

            // ── Étape 3 : Générer clé AES + chiffrer avec clé publique RSA ──
            this.sessionKey = new AESSessionKey();
            PublicKey serverPublicKey = RSAKeyManager.publicKeyFromBase64(publicKeyBase64);
            String encryptedAES = RSAKeyManager.encryptAESKey(sessionKey.getRawKey(), serverPublicKey);

            String nonceClient2 = NonceManager.generateNonce();
            Map<String, String> aesMsg = new HashMap<>();
            aesMsg.put("type",         "AES_KEY");
            aesMsg.put("encryptedAES", encryptedAES);
            aesMsg.put("nonce",        nonceClient2);
            out.writeObject(aesMsg);
            out.flush();
            if (listener != null) listener.onStatusUpdate("📤 Clé AES chiffrée envoyée...");

            // ── Étape 4 : Recevoir confirmation du serveur ────────
            @SuppressWarnings("unchecked")
            Map<String, String> ack = (Map<String, String>) in.readObject();

            if (!"HANDSHAKE_OK".equals(ack.get("type"))) {
                String reason = ack.getOrDefault("message", "Handshake refusé par le serveur");
                if (listener != null) listener.onHandshakeFailure(reason);
                return false;
            }

            this.sessionId       = ack.get("sessionId");
            this.handshakeComplete = true;

            System.out.println("🔒 Handshake RSA complet — session: " + sessionId);
            if (listener != null) listener.onHandshakeSuccess(sessionId, receivedFingerprint);
            return true;

        } catch (Exception e) {
            String reason = "Erreur handshake: " + e.getMessage();
            System.err.println("❌ " + reason);
            if (listener != null) listener.onHandshakeFailure(reason);
            return false;
        }
    }

    // ── Getters ──────────────────────────────────────────────────
    public AESSessionKey getSessionKey()    { return sessionKey; }
    public String        getSessionId()     { return sessionId; }
    public String        getFingerprint()   { return serverFingerprint; }
    public boolean       isComplete()       { return handshakeComplete; }
}