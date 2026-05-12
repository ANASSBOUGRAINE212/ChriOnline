package security.RSA;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Backend Réseau — Validation nonce anti-rejeu côté réseau.
 * Chaque nonce ne peut être utilisé qu'une seule fois.
 * Expiration automatique après TTL_MS millisecondes.
 */
public class NonceManager {

    private static final int    NONCE_BYTES = 32;
    private static final long   TTL_MS      = 5 * 60 * 1000; // 5 minutes
    private static final int    MAX_NONCES  = 10_000;

    // Singleton côté serveur
    private static final NonceManager INSTANCE = new NonceManager();
    public static NonceManager getInstance() { return INSTANCE; }

    // Map nonce → timestamp d'émission
    private final Map<String, Long> usedNonces = Collections.synchronizedMap(
        new LinkedHashMap<String, Long>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
                return size() > MAX_NONCES;
            }
        }
    );

    // ── Génération nonce aléatoire ────────────────────────────────
    public static String generateNonce() {
        byte[] bytes = new byte[NONCE_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    // ── Validation nonce (anti-rejeu) ────────────────────────────
    /**
     * Retourne true si le nonce est valide (jamais vu + non expiré).
     * Enregistre le nonce pour bloquer toute réutilisation.
     */
    public boolean validateAndConsume(String nonce) {
        if (nonce == null || nonce.isEmpty()) {
            System.out.println("❌ Nonce null ou vide");
            return false;
        }

        long now = System.currentTimeMillis();

        synchronized (usedNonces) {
            // Nettoyer les nonces expirés
            usedNonces.entrySet().removeIf(e -> now - e.getValue() > TTL_MS);

            if (usedNonces.containsKey(nonce)) {
                System.out.println("🚨 REPLAY ATTACK détecté — nonce déjà utilisé: " + nonce.substring(0, 8) + "...");
                return false;
            }

            usedNonces.put(nonce, now);
            System.out.println("✅ Nonce validé et consommé");
            return true;
        }
    }

    // ── Vérification expiration seule (sans consommer) ───────────
    public static boolean isExpired(long timestampMs) {
        return System.currentTimeMillis() - timestampMs > TTL_MS;
    }

    public int size() {
        return usedNonces.size();
    }
}