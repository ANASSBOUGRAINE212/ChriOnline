package server;

import database.databaseInitializer;
import security.RSA.RSAKeyManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class serverApp {
    public static void main(String[] args) {
        System.out.println("Starting ChriOnline Server...");

        System.out.println("Initializing database");
        databaseInitializer.init();
        sessionManager sm = sessionManager.getInstance();
        System.out.println("Database ready!");

        // ── Initialisation RSA (une seule paire pour tout le serveur) ──
        RSAKeyManager rsaKeyManager;
        try {
            rsaKeyManager = new RSAKeyManager();
            System.out.println("🔑 RSA ready — clé publique distribuée aux clients");
        } catch (Exception e) {
            System.err.println("❌ Impossible d'initialiser RSA: " + e.getMessage());
            return;
        }
        HandshakeHandler handshakeHandler = new HandshakeHandler(rsaKeyManager);

        // ✅ SO_REUSEADDR — prevents "Address already in use" on restart
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(5000));
            System.out.println("Listening on port 5000");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                // ── Passer handshakeHandler à chaque clientHandler ──
                new Thread(new clientHandler(clientSocket, handshakeHandler)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}