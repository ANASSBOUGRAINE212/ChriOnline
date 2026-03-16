package ChriOnline.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("[ClientHandler] Reçu : " + message);
                String response = handleRequest(message);
                out.writeBytes(response + "\n");
            }

        } catch (IOException e) {
            System.out.println("[ClientHandler] Client déconnecté.");
        } finally {
            try { socket.close(); } catch (IOException e) {}
        }
    }

    // ─────────────────────────────────────────
    //  ROUTER — traite chaque commande
    // ─────────────────────────────────────────
    private String handleRequest(String message) {
        String[] parts = message.split(":");

        switch (parts[0]) {

            // ── LOGIN:email:password ──────────
            case "LOGIN":
                if (parts.length < 3)
                    return "ERROR:Format invalide → LOGIN:email:password";

                String token = UserService.login(parts[1], parts[2]);
                if (token != null)
                    return "SUCCESS:" + token;
                else
                    return "FAIL:Email ou mot de passe incorrect";

            // ── LOGOUT:token ──────────────────
            case "LOGOUT":
                if (parts.length < 2)
                    return "ERROR:Token manquant → LOGOUT:token";

                boolean ok = UserService.logout(parts[1]);
                return ok ? "SUCCESS:Deconnecte" : "FAIL:Session introuvable";

            // ── Commande inconnue ─────────────
            default:
                return "ERROR:Commande inconnue → " + parts[0];
        }
    }
}