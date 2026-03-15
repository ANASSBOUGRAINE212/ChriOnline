package server;

import common.Protocol;
import database.UserDAO;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket  socket;
    private BufferedReader in;
    private PrintWriter    out;
    private final UserDAO  dao = new UserDAO();

    public ClientHandler(Socket socket) { this.socket = socket; }

    @Override
    public void run() {
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            String requete;
            while ((requete = in.readLine()) != null) {
                System.out.println("[SERVER] Reçu : " + requete);
                out.println(traiter(requete));
            }
        } catch (IOException e) {
            System.err.println("[SERVER] " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private String traiter(String requete) {
        String[] parts = requete.split("\\" + Protocol.SEP);
        return switch (parts[0].toUpperCase()) {
            case "REGISTER" -> traiterRegister(parts);
            case "GET_PROFILE" -> traiterGetProfile(parts);
            default -> Protocol.ERROR + Protocol.SEP + "Commande inconnue";
        };
    }

    // ── Étapes 3, 4, 5, 9 ─────────────────────
    private String traiterRegister(String[] parts) {

        // Étape 3 : vérifier le format
        if (parts.length != 4)
            return Protocol.ERROR + Protocol.SEP + "Format : REGISTER|username|email|password";

        String username = parts[1].trim();
        String email    = parts[2].trim();
        String password = parts[3].trim();

        // Étape 4 : validation serveur
        String erreur = valider(username, email, password);
        if (erreur != null)
            return Protocol.ERROR + Protocol.SEP + Protocol.ERR_INVALID_DATA + " : " + erreur;

        // Étape 5 : appel DAO
        String resultat = dao.registerUser(username, email, password);

        // Étape 9 : construire la réponse
        return switch (resultat) {
            case "OK"             -> Protocol.SUCCESS + Protocol.SEP + "Bienvenue " + username + " !";
            case "USERNAME_TAKEN" -> Protocol.ERROR   + Protocol.SEP + Protocol.ERR_USER_EXISTS;
            case "EMAIL_TAKEN"    -> Protocol.ERROR   + Protocol.SEP + Protocol.ERR_EMAIL_EXISTS;
            default               -> Protocol.ERROR   + Protocol.SEP + Protocol.ERR_SERVER;
        };
    }
    private String traiterGetProfile(String[] parts) {
        if (parts.length != 2)
            return Protocol.ERROR + Protocol.SEP + "Format : GET_PROFILE|username";

        String username = parts[1].trim();

        if (username.isEmpty())
            return Protocol.ERROR + Protocol.SEP + "Username vide";

        String resultat = dao.getProfile(username);

        if (resultat.startsWith("OK")) {
            String data = resultat.substring(3);
            return Protocol.SUCCESS + Protocol.SEP + data;
        } else if (resultat.equals("USER_NOT_FOUND")) {
            return Protocol.ERROR + Protocol.SEP + "Utilisateur introuvable";
        } else {
            return Protocol.ERROR + Protocol.SEP + Protocol.ERR_SERVER;
        }
    }
    private String valider(String username, String email, String password) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty())
            return "Champs vides";
        if (username.length() < 3 || username.length() > 20)
            return "Username : 3 à 20 caractères";
        if (!username.matches("[a-zA-Z0-9_]+"))
            return "Username : lettres, chiffres, _ uniquement";
        if (!email.contains("@") || !email.contains("."))
            return "Email invalide";
        if (password.length() < 6)
            return "Mot de passe : 6 caractères minimum";
        return null;
    }
}