package ChriOnline.server;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int PORT = 7016;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[Server] En ecoute sur le port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Server] Nouveau client connecte.");
                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
            }

        } catch (Exception e) {
            System.out.println("[Server] Erreur : " + e.getMessage());
        }
    }
}