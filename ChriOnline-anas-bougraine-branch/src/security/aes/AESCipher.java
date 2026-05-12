package security.aes;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;

public class AESCipher {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    public static final int IV_LENGTH = 12;

    public static byte[] encryptGCM(byte[] data, SecretKey key) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] ciphertext = cipher.doFinal(data);

        byte[] result = new byte[IV_LENGTH + ciphertext.length];
        System.arraycopy(iv, 0, result, 0, IV_LENGTH);
        System.arraycopy(ciphertext, 0, result, IV_LENGTH, ciphertext.length);
        return result;
    }

    public static byte[] decryptGCM(byte[] encryptedData, SecretKey key) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(encryptedData, 0, iv, 0, IV_LENGTH);

        byte[] ciphertext = new byte[encryptedData.length - IV_LENGTH];
        System.arraycopy(encryptedData, IV_LENGTH, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        return cipher.doFinal(ciphertext);
    }
}