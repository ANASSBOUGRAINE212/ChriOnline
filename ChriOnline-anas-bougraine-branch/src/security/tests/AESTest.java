package security.tests;

import security.aes.*;
import javax.crypto.AEADBadTagException;
import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Base64;

public class AESTest {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Tests AES Dev 2 ===\n");
        test1_chiffrementDechiffrement();
        test2_ivUnique();
        test3_integrite();
        test4_streams();
        System.out.println("\nTous les tests sont passes !");
    }

    static void test1_chiffrementDechiffrement() throws Exception {
        System.out.print("Test 1 - Chiffrement/Dechiffrement... ");
        SecretKey key = AESKeyGenerator.generateAESKey();
        String message = "Commande #42 : 3x Produit A";

        byte[] encrypted = AESCipher.encryptGCM(message.getBytes("UTF-8"), key);
        byte[] decrypted = AESCipher.decryptGCM(encrypted, key);

        assert Arrays.equals(message.getBytes("UTF-8"), decrypted) : "ERREUR !";
        System.out.println("OK");
    }

    static void test2_ivUnique() throws Exception {
        System.out.print("Test 2 - Unicite des IV... ");
        SecretKey key = AESKeyGenerator.generateAESKey();
        byte[] data = "test".getBytes("UTF-8");

        byte[] enc1 = AESCipher.encryptGCM(data, key);
        byte[] enc2 = AESCipher.encryptGCM(data, key);

        byte[] iv1 = Arrays.copyOfRange(enc1, 0, AESCipher.IV_LENGTH);
        byte[] iv2 = Arrays.copyOfRange(enc2, 0, AESCipher.IV_LENGTH);

        assert !Arrays.equals(iv1, iv2) : "ERREUR : IV identiques !";
        System.out.println("OK  IV1=" + Base64.getEncoder().encodeToString(iv1)
                              + "  IV2=" + Base64.getEncoder().encodeToString(iv2));
    }

    static void test3_integrite() throws Exception {
        System.out.print("Test 3 - Integrite (alteration detectee)... ");
        SecretKey key = AESKeyGenerator.generateAESKey();
        byte[] encrypted = AESCipher.encryptGCM("donnees sensibles".getBytes("UTF-8"), key);

        encrypted[encrypted.length / 2] ^= 0xFF;

        try {
            AESCipher.decryptGCM(encrypted, key);
            System.out.println("ECHEC (aucune exception levee !)");
        } catch (AEADBadTagException e) {
            System.out.println("OK  AEADBadTagException levee correctement");
        }
    }

    static void test4_streams() throws Exception {
        System.out.print("Test 4 - SecureOutputStream/SecureInputStream... ");
        SecretKey key = AESKeyGenerator.generateAESKey();

        java.io.PipedOutputStream pipeOut = new java.io.PipedOutputStream();
        java.io.PipedInputStream  pipeIn  = new java.io.PipedInputStream(pipeOut);

        SecureOutputStream secOut = new SecureOutputStream(pipeOut, key);
        SecureInputStream  secIn  = new SecureInputStream(pipeIn, key);

        String original = "Test stream securise !";
        secOut.writeSecureString(original);
        String received = secIn.readSecureString();

        assert original.equals(received) : "ERREUR : message different !";
        System.out.println("OK");

        secOut.close();
        secIn.close();
    }
}