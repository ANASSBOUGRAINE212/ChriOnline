package security.aes;

import javax.crypto.SecretKey;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SecureInputStream {

    private final DataInputStream in;
    private SecretKey sessionKey;

    public SecureInputStream(InputStream inputStream, SecretKey sessionKey) {
        this.in = new DataInputStream(inputStream);
        this.sessionKey = sessionKey;
    }

    public byte[] readSecure() throws Exception {
        int length = in.readInt();
        if (length <= 0 || length > 10_000_000) {
            throw new IOException("Longueur de message invalide : " + length);
        }
        byte[] encrypted = new byte[length];
        in.readFully(encrypted);
        return AESCipher.decryptGCM(encrypted, sessionKey);
    }

    public String readSecureString() throws Exception {
        return new String(readSecure(), "UTF-8");
    }

    public void updateKey(SecretKey newKey) {
        this.sessionKey = newKey;
    }

    public void close() throws IOException {
        in.close();
    }
}