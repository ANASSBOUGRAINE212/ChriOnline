package ui.security;

import security.aes.AESCipher;
import security.aes.AESKeyGenerator;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.util.Base64;

public class CryptoDemoPanel extends JPanel {

    private final JTextArea txtPlain;
    private final JTextArea txtEncrypted;
    private final JTextArea txtDecrypted;
    private final JLabel lblStatus;
    private SecretKey demoKey;

    public CryptoDemoPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Demo AES-256-GCM"));

        demoKey = AESKeyGenerator.generateAESKey();

        txtPlain = new JTextArea(3, 40);
        txtPlain.setText("Message de test : commande #1234");

        txtEncrypted = new JTextArea(4, 40);
        txtEncrypted.setEditable(false);
        txtEncrypted.setBackground(new Color(255, 245, 220));
        txtEncrypted.setFont(new Font("Monospaced", Font.PLAIN, 11));

        txtDecrypted = new JTextArea(3, 40);
        txtDecrypted.setEditable(false);
        txtDecrypted.setBackground(new Color(220, 255, 220));

        lblStatus = new JLabel("Pret.");

        JButton btnEncrypt = new JButton("Chiffrer avec AES-256-GCM");
        btnEncrypt.addActionListener(e -> showEncryptDemo(txtPlain.getText()));

        JButton btnNewKey = new JButton("Nouvelle cle");
        btnNewKey.addActionListener(e -> {
            demoKey = AESKeyGenerator.generateAESKey();
            lblStatus.setText("Nouvelle cle AES-256 generee !");
            txtEncrypted.setText("");
            txtDecrypted.setText("");
        });

        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        centerPanel.add(createLabeled("Message original :", txtPlain));
        centerPanel.add(createLabeled("Chiffre (Base64) :", txtEncrypted));
        centerPanel.add(createLabeled("Dechiffre :", txtDecrypted));

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(btnEncrypt);
        bottomPanel.add(btnNewKey);
        bottomPanel.add(lblStatus);

        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void showEncryptDemo(String plaintext) {
        try {
            byte[] data = plaintext.getBytes("UTF-8");
            byte[] encrypted = AESCipher.encryptGCM(data, demoKey);

            byte[] iv = new byte[AESCipher.IV_LENGTH];
            System.arraycopy(encrypted, 0, iv, 0, AESCipher.IV_LENGTH);

            txtEncrypted.setText(
                "IV (12 bytes) : " + Base64.getEncoder().encodeToString(iv) + "\n" +
                "Donnees chiffrees :\n" + Base64.getEncoder().encodeToString(encrypted)
            );

            byte[] decrypted = AESCipher.decryptGCM(encrypted, demoKey);
            txtDecrypted.setText(new String(decrypted, "UTF-8"));

            lblStatus.setText("Chiffrement AES-256-GCM reussi !");

        } catch (Exception ex) {
            lblStatus.setText("Erreur : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private JPanel createLabeled(String label, JTextArea area) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(new JLabel(label), BorderLayout.NORTH);
        p.add(new JScrollPane(area), BorderLayout.CENTER);
        return p;
    }

    // Pour lancer la demo directement
    public static void main(String[] args) {
        JFrame frame = new JFrame("Demo Chiffrement AES");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new CryptoDemoPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}