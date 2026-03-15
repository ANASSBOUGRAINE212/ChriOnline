package client;

import common.Protocol;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket         socket;
    private BufferedReader in;
    private PrintWriter    out;
    private final Scanner  scanner = new Scanner(System.in);

    public void connecter() throws IOException {
        socket = new Socket("localhost", 6666);
        in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out    = new PrintWriter(socket.getOutputStream(), true);
        System.out.println("[CLIENT] Connecté ✅");
    }

    public void register() {

        // Étape 1 : saisie
        System.out.println("\n=== INSCRIPTION ===");
        System.out.print("Username  : "); String username = scanner.nextLine().trim();
        System.out.print("Email     : "); String email    = scanner.nextLine().trim();
        System.out.print("Password  : "); String password = scanner.nextLine().trim();
        System.out.print("Confirmer : "); String confirm  = scanner.nextLine().trim();

        // Étape 2 : validation locale
        if (!password.equals(confirm)) {
            System.out.println("❌ Mots de passe différents."); return;
        }
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            System.out.println("❌ Champs obligatoires."); return;
        }
        if (password.length() < 6) {
            System.out.println("❌ Mot de passe trop court."); return;
        }

        // Étape 3 : envoi TCP
        String requete = Protocol.REGISTER + Protocol.SEP + username
                       + Protocol.SEP + email + Protocol.SEP + password;
        out.println(requete);

        // Étape 9 : lire la réponse
        try {
            String reponse = in.readLine();
            String[] parts = reponse.split("\\" + Protocol.SEP);
            if (reponse.startsWith(Protocol.SUCCESS))
                System.out.println("✅ " + parts[1]);
            else
                System.out.println("❌ " + parts[1]);
        } catch (IOException e) {
            System.err.println("[CLIENT] Erreur : " + e.getMessage());
        }
    }
    public void getProfile() {

        // Étape 1 : saisir
        System.out.println("\n=== MON PROFIL ===");
        System.out.print("Username : ");
        String username = scanner.nextLine().trim();

        // Étape 2 : valider local
        if (username.isEmpty()) {
            System.out.println("❌ Username obligatoire.");
            return;
        }

        // Étape 3 : envoyer TCP
        String requete = "GET_PROFILE" + Protocol.SEP + username;
        out.println(requete);

        // Étape 7 : lire et afficher
        try {
            String reponse = in.readLine();
            if (reponse == null) { System.out.println("❌ Pas de réponse."); return; }

            if (reponse.startsWith(Protocol.SUCCESS)) {
                String[] parts = reponse.split("\\" + Protocol.SEP);

                 username  = parts[1];
                String email     = parts[2];
                String createdAt = parts[3];

                int largeur = 50;
                String ligne = "═".repeat(largeur);

                System.out.println("\n╔" + ligne + "╗");
                System.out.println("║" + centrer("👤  MON PROFIL", largeur) + "║");
                System.out.println("╠" + ligne + "╣");
                System.out.println("║" + "  🔹 Username   : " + padDroite(username, largeur - 18) + "║");
                System.out.println("║" + "  🔹 Email      : " + padDroite(email, largeur - 18) + "║");
                System.out.println("║" + "  🔹 Inscrit le : " + padDroite(createdAt, largeur - 18) + "║");
                System.out.println("╚" + ligne + "╝");
                System.out.println();
            } else {
                String[] parts = reponse.split("\\" + Protocol.SEP);
                System.out.println("❌ " + (parts.length > 1 ? parts[1] : reponse));
            }
        } catch (IOException e) {
            System.err.println("[CLIENT] Erreur : " + e.getMessage());
        }
    }
    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.connecter();
        Scanner sc = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.println("\n1. S'inscrire");
            System.out.println("2. Voir mon profil"); // ← nouveau
            System.out.println("0. Quitter");
            System.out.print("Choix : ");
            String choix = sc.nextLine();
            switch (choix) {
                case "1" -> client.register();
                case "2" -> client.getProfile(); // ← nouveau
                case "0" -> running = false;
                default  -> System.out.println("Choix invalide.");
            }
        }
    }
    private String centrer(String texte, int largeur) {
        int espaces = (largeur - texte.length()) / 2;
        String pad = " ".repeat(Math.max(0, espaces));
        String result = pad + texte + pad;
        while (result.length() < largeur) result += " ";
        return result;
    }

    private String padDroite(String texte, int largeur) {
        if (texte.length() >= largeur) return texte.substring(0, largeur);
        return texte + " ".repeat(largeur - texte.length());
    }
}