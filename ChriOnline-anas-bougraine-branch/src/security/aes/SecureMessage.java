package security.aes;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.util.Base64;

public class SecureMessage implements Serializable {

    private static final long serialVersionUID = 1L;
    private final byte[] encryptedData;

    private SecureMessage(byte[] encryptedData) {
        this.encryptedData = encryptedData;
    }

    public static SecureMessage encrypt(byte[] payload, SecretKey key) throws Exception {
        return new SecureMessage(AESCipher.encryptGCM(payload, key));
    }

    public byte[] decrypt(SecretKey key) throws Exception {
        return AESCipher.decryptGCM(encryptedData, key);
    }

    public byte[] getIV() {
        byte[] iv = new byte[AESCipher.IV_LENGTH];
        System.arraycopy(encryptedData, 0, iv, 0, AESCipher.IV_LENGTH);
        return iv;
    }

    public String toBase64() {
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    public byte[] getEncryptedData() {
        return encryptedData;
    }
}