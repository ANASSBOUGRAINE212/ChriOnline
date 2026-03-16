package ChriOnline.clients;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Clients {

    private static final String HOST = "localhost";
    private static final int PORT = 7016;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════╗");
        System.out.println("║   ChriOnline Client v1.0     ║");
        System.out.println("╚══════════════════════════════╝");

        try {
            Socket socket = new Socket(HOST, PORT);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            Scanner scanner = new Scanner(System.in);

            String sessionToken = null;

            while (true) {
                System.out.println("\n┌─────────────────────┐");
                System.out.println("│       MENU          │");
                System.out.println("├─────────────────────┤");
                System.out.println("│  1. Login           │");
                System.out.println("│  2. Logout          │");
                System.out.println("│  3. Quitter         │");
                System.out.println("└─────────────────────┘");

                if (sessionToken != null)
                    System.out.println("  [Connecté] token=" + sessionToken);
                else
                    System.out.println("  [Non connecté]");

                System.out.print("Choix : ");
                String choix = scanner.nextLine().trim();

                switch (choix) {

                    // ── LOGIN ─────────────────────────
                    case "1":
                        System.out.print("Email    : ");
                        String email = scanner.nextLine().trim();
                        System.out.print("Password : ");
                        String password = scanner.nextLine().trim();

                        out.writeBytes("LOGIN:" + email + ":" + password + "\n");
                        String loginResp = in.readLine();
                        System.out.println("[Réponse] " + loginResp);

                        if (loginResp != null && loginResp.startsWith("SUCCESS:")) {
                            sessionToken = loginResp.substring(8); // après "SUCCESS:"
                            System.out.println("✔ Connecté ! Token sauvegardé.");
                        } else {
                            System.out.println("✘ Échec du login.");
                        }
                        break;

                    // ── LOGOUT ────────────────────────
                    case "2":
                        if (sessionToken == null) {
                            System.out.println("✘ Vous n'êtes pas connecté.");
                            break;
                        }
                        out.writeBytes("LOGOUT:" + sessionToken + "\n");
                        String logoutResp = in.readLine();
                        System.out.println("[Réponse] " + logoutResp);

                        if (logoutResp != null && logoutResp.startsWith("SUCCESS:")) {
                            sessionToken = null;
                            System.out.println("✔ Déconnecté avec succès.");
                        } else {
                            System.out.println("✘ Échec du logout.");
                        }
                        break;

                    // ── QUITTER ───────────────────────
                    case "3":
                        System.out.println("Au revoir !");
                        socket.close();
                        System.exit(0);
                        break;

                    default:
                        System.out.println("Choix invalide.");
                }
            }

        } catch (Exception e) {
            System.out.println("[Client] Erreur : " + e.getMessage());
        }
    }
}