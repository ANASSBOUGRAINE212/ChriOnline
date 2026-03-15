package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int PORT = 6666;

    public static void main(String[] args) {

        // ── SOLUTION : option SO_REUSEADDR ──────────────
        // Permet de réutiliser le port immédiatement
        // même si un ancien processus l'occupe encore
        try (ServerSocket ss = new ServerSocket()) {

            ss.setReuseAddress(true);  // ← la ligne magique
            ss.bind(new java.net.InetSocketAddress(PORT));

            System.out.println("[SERVER] Démarrage port " + PORT + "...");

            while (true) {
                Socket client = ss.accept();
                new Thread(new ClientHandler(client)).start();
                System.out.println("[SERVER] Nouveau client connecté.");
            }

        } catch (IOException e) {
            System.err.println("[SERVER] Erreur : " + e.getMessage());
        }
    }
}