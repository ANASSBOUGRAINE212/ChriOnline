package ChriOnline.server;

import ChriOnline.Model.User;
import java.util.*;

public class UserService {

    // Sessions actives : token -> User
    private static Map<String, User> activeSessions = new HashMap<>();

    // Base de données simulée en mémoire
    private static List<User> fakeDB = new ArrayList<>(Arrays.asList(
        new User(1, "alice", "alice@mail.com", "1234"),
        new User(2, "bob",   "bob@mail.com",   "abcd")
    ));

    // ─────────────────────────────────────────
    //  LOGIN
    // ─────────────────────────────────────────
    public static String login(String email, String password) {
        for (User u : fakeDB) {
            if (u.getEmail().equals(email) && u.getPassword().equals(password)) {

                // Vérifier si déjà connecté
                if (u.getSessionToken() != null) {
                    System.out.println("[UserService] Déjà connecté : " + email);
                    return u.getSessionToken(); // retourne le token existant
                }

                // Générer un token unique
                String token = UUID.randomUUID().toString();
                u.setSessionToken(token);
                activeSessions.put(token, u);
                System.out.println("[UserService] Login OK → " + email + " | token=" + token);
                return token;
            }
        }
        System.out.println("[UserService] Login FAIL → " + email);
        return null;
    }

    // ─────────────────────────────────────────
    //  LOGOUT
    // ─────────────────────────────────────────
    public static boolean logout(String token) {
        if (activeSessions.containsKey(token)) {
            User u = activeSessions.remove(token);
            u.setSessionToken(null);
            System.out.println("[UserService] Logout OK → " + u.getEmail());
            return true;
        }
        System.out.println("[UserService] Logout FAIL → token introuvable");
        return false;
    }

    // ─────────────────────────────────────────
    //  UTILITAIRES
    // ─────────────────────────────────────────
    public static boolean isAuthenticated(String token) {
        return activeSessions.containsKey(token);
    }

    public static User getUserByToken(String token) {
        return activeSessions.get(token);
    }
}