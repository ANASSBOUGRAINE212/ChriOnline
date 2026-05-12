package security.aes;

import javax.crypto.SecretKey;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SecureOutputStream {

    private final DataOutputStream out;
    private SecretKey sessionKey;

    public SecureOutputStream(OutputStream outputStream, SecretKey sessionKey) {
        this.out = new DataOutputStream(outputStream);
        this.sessionKey = sessionKey;
    }

    public void writeSecure(byte[] data) throws Exception {
        byte[] encrypted = AESCipher.encryptGCM(data, sessionKey);
        out.writeInt(encrypted.length);
        out.write(encrypted);
        out.flush();
    }

    public void writeSecureString(String text) throws Exception {
        writeSecure(text.getBytes("UTF-8"));
    }

    public void updateKey(SecretKey newKey) {
        this.sessionKey = newKey;
    }

    public void close() throws IOException {
        out.close();
    }
}